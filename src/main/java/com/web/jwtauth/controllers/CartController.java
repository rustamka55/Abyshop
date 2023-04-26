package com.web.jwtauth.controllers;

import com.web.jwtauth.jms.Message;
import com.web.jwtauth.models.Product;
import com.web.jwtauth.models.Cart;
import com.web.jwtauth.models.CartItem;
import com.web.jwtauth.models.User;
import com.web.jwtauth.payload.request.AddCartItemRequest;
import com.web.jwtauth.payload.response.MessageResponse;
import com.web.jwtauth.repository.*;
import com.web.jwtauth.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.jms.JMSException;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/cart")
@EnableJms
public class CartController {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    TagsRepository tagsRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserRepository userRepository;


    @PostMapping("/addCartItem")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('USER')")
    public ResponseEntity<?> addCartItem(@RequestBody AddCartItemRequest addCartItemRequest, HttpServletRequest httpServletRequest){
        Long id = addCartItemRequest.getId();
        Long quantity = addCartItemRequest.getQuantity();
        if(quantity==null) quantity=1L;

        String headerAuth = httpServletRequest.getHeader("Authorization");
        String jwt = null;
        String username = null;
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            jwt =  headerAuth.substring(7, headerAuth.length());
        }
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            username = jwtUtils.getUserNameFromJwtToken(jwt);
        }
        Optional<User> user = userRepository.findByEmail(username);
        Optional<Product> product = productRepository.findById(id);
        if(product.isPresent()) {
            if (user.isPresent()) {
                if (!(quantity < 1) && !(quantity > product.get().getCount())) {
                    Optional<Cart> cart = cartRepository.findByUser(user.get());
                    if (!cart.isPresent()) {
                        Cart newCart = new Cart(user.get(), new HashSet<>());
                        cartRepository.save(newCart);
                    }
                    cart = cartRepository.findByUser(user.get());
                    Set<CartItem> cartItems = cart.get().getCartItems();
                    for (CartItem i : cartItems) {
                        if (Objects.equals(i.getProduct().getId(), product.get().getId())) {
                            i.setQuantity(i.getQuantity() + quantity);
                            cartItemRepository.save(i);
                            cartRepository.save(cart.get());
                            return ResponseEntity.ok(new MessageResponse("Product " + i.getProduct().getTitle() + " is added to your cart"));
                        }
                    }
                    CartItem newCartItem = new CartItem(product.get(), quantity);
                    cartItems.add(newCartItem);
                    cartItemRepository.save(newCartItem);
                    cartRepository.save(cart.get());
                    return ResponseEntity.ok(new MessageResponse("Product " + newCartItem.getProduct().getTitle() + " is added to your cart"));
                }
                return ResponseEntity.badRequest().body(new MessageResponse("Enter valid quantity"));
            }
            return ResponseEntity.badRequest().body(new MessageResponse("Not authorized"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("No such product"));
    }

    @GetMapping("/getCartItems")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getCartItems(HttpServletRequest httpServletRequest){
        String headerAuth = httpServletRequest.getHeader("Authorization");
        String jwt = null;
        String username = null;
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            jwt =  headerAuth.substring(7, headerAuth.length());
        }
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            username = jwtUtils.getUserNameFromJwtToken(jwt);
        }
        Optional<User> user = userRepository.findByEmail(username);
        if(user.isPresent()){
            Optional<Cart> cart = cartRepository.findByUser(user.get());
            if (cart.isPresent()) {
                return ResponseEntity.ok().body(cart.get().getCartItems());
            }
            return ResponseEntity.ok().body(new MessageResponse("Empty cart"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Not authorized"));
    }


    @PutMapping("/updateCartItemQuanity")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateCartItemQuantity(@RequestBody AddCartItemRequest addCartItemRequest,HttpServletRequest httpServletRequest){
        Long id = addCartItemRequest.getId();
        Long quantity = addCartItemRequest.getQuantity();

        String headerAuth = httpServletRequest.getHeader("Authorization");
        String jwt = null;
        String username = null;
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            jwt =  headerAuth.substring(7, headerAuth.length());
        }
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            username = jwtUtils.getUserNameFromJwtToken(jwt);
        }
        Optional<User> user = userRepository.findByEmail(username);
        Optional<Product> product = productRepository.findById(id);
        if(product.isPresent()) {
            if (user.isPresent()) {
                if (quantity>=0 && quantity<=product.get().getCount()) {
                    Optional<Cart> cart = cartRepository.findByUser(user.get());
                    if (!cart.isPresent()) {
                        return ResponseEntity.badRequest().body(new MessageResponse("No cart"));
                    }
                    Set<CartItem> cartItems = cart.get().getCartItems();
                    for (CartItem i : cartItems) {
                        if (Objects.equals(i.getProduct().getId(), product.get().getId())) {
                            if(quantity==0){
                                Set<CartItem> cartItemSet = cart.get().getCartItems();
                                cartItemSet.remove(i);
                                cart.get().setCartItems(cartItemSet);
                                cartItemRepository.delete(i);
                                cartRepository.save(cart.get());
                                return ResponseEntity.ok(new MessageResponse("Product " + i.getProduct().getTitle() + " was deleted"));
                            }
                            else {
                                i.setQuantity(quantity);
                                cartItemRepository.save(i);
                                return ResponseEntity.ok(new MessageResponse("Product " + i.getProduct().getTitle() + " quantity updated to " + quantity));
                            }
                        }
                    }
                    return ResponseEntity.badRequest().body(new MessageResponse("No such item in the cart"));
                }
                return ResponseEntity.badRequest().body(new MessageResponse("Enter valid quantity"));
            }
            return ResponseEntity.badRequest().body(new MessageResponse("Not authorized"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("No such product"));
    }


    @DeleteMapping("/deleteCartItem/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteCartItem(HttpServletRequest httpServletRequest,@PathVariable Long id){
        return updateCartItemQuantity(new AddCartItemRequest(id,0L),httpServletRequest);
    }


    @Autowired
    private Message message;

    @PostMapping("/sendCartMessage")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> sendMessage(HttpServletRequest httpServletRequest) throws JMSException {
        String headerAuth = httpServletRequest.getHeader("Authorization");
        String jwt = null;
        String username = null;
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            jwt =  headerAuth.substring(7, headerAuth.length());
        }
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            username = jwtUtils.getUserNameFromJwtToken(jwt);
        }
        Optional<User> user = userRepository.findByEmail(username);
        if(user.isPresent()){
            Optional<Cart> cart = cartRepository.findByUser(user.get());
            if (cart.isPresent()) {
                message.sendMessage(cart.get());
                return  ResponseEntity.ok().body(new MessageResponse("Message " + cart + " was sent to admin"));
            }
            return ResponseEntity.ok().body(new MessageResponse("Empty cart"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Not authorized"));
    }



    //TODO
    //change deleteUser(deleteCartItems and deleteCart)
}
