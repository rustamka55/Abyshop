package com.web.jwtauth.controllers;

import com.web.jwtauth.jms.Message;
import com.web.jwtauth.models.Book;
import com.web.jwtauth.models.Cart;
import com.web.jwtauth.models.CartItem;
import com.web.jwtauth.models.User;
import com.web.jwtauth.payload.request.AddCartItemRequest;
import com.web.jwtauth.repository.*;
import com.web.jwtauth.security.jwt.JwtUtils;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.jms.JMSException;
import javax.servlet.http.HttpServletRequest;
import javax.swing.text.html.Option;
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
    BookRepository bookRepository;

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    GenreRepository genreRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserRepository userRepository;


    @PostMapping("/addCartItem")
    @PreAuthorize("hasRole('USER')")
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
        Optional<User> user = userRepository.findByUsername(username);
        Optional<Book> book = bookRepository.findById(id);
        if(book.isPresent()) {
            if (user.isPresent()) {
                if (!(quantity < 1) && !(quantity > book.get().getCount())) {
                    Optional<Cart> cart = cartRepository.findByUser(user.get());
                    if (!cart.isPresent()) {
                        Cart newCart = new Cart(user.get(), new HashSet<>());
                        cartRepository.save(newCart);
                    }
                    cart = cartRepository.findByUser(user.get());
                    Set<CartItem> cartItems = cart.get().getCartItems();
                    for (CartItem i : cartItems) {
                        if (Objects.equals(i.getBook().getId(), book.get().getId())) {
                            i.setQuantity(i.getQuantity() + quantity);
                            cartItemRepository.save(i);
                            cartRepository.save(cart.get());
                            return ResponseEntity.ok("book " + i.getBook().getTitle() + " is added to your cart");
                        }
                    }
                    CartItem newCartItem = new CartItem(book.get(), quantity);
                    cartItems.add(newCartItem);
                    cartItemRepository.save(newCartItem);
                    cartRepository.save(cart.get());
                    return ResponseEntity.ok("book " + newCartItem.getBook().getTitle() + " is added to your cart");
                }
                return ResponseEntity.badRequest().body("enter valid quantity");
            }
            return ResponseEntity.badRequest().body("not authorized");
        }
        return ResponseEntity.badRequest().body("no such book");
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
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isPresent()){
            Optional<Cart> cart = cartRepository.findByUser(user.get());
            if (cart.isPresent()) {
                return ResponseEntity.ok().body(cart.get().getCartItems());
            }
            return ResponseEntity.ok().body("empty cart");
        }
        return ResponseEntity.badRequest().body("not authorized");
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
        Optional<User> user = userRepository.findByUsername(username);
        Optional<Book> book = bookRepository.findById(id);
        if(book.isPresent()) {
            if (user.isPresent()) {
                if (quantity>=0 && quantity<=book.get().getCount()) {
                    Optional<Cart> cart = cartRepository.findByUser(user.get());
                    if (!cart.isPresent()) {
                        return ResponseEntity.badRequest().body("no cart");
                    }
                    Set<CartItem> cartItems = cart.get().getCartItems();
                    for (CartItem i : cartItems) {
                        if (Objects.equals(i.getBook().getId(), book.get().getId())) {
                            if(quantity==0){
                                Set<CartItem> cartItemSet = cart.get().getCartItems();
                                cartItemSet.remove(i);
                                cart.get().setCartItems(cartItemSet);
                                cartItemRepository.delete(i);
                                cartRepository.save(cart.get());
                                return ResponseEntity.ok("book " + i.getBook().getTitle() + " was deleted");
                            }
                            else {
                                i.setQuantity(quantity);
                                cartItemRepository.save(i);
                                return ResponseEntity.ok("book " + i.getBook().getTitle() + " quantity updated to " + quantity);
                            }
                        }
                    }
                    return ResponseEntity.badRequest().body("no such item in the cart");
                }
                return ResponseEntity.badRequest().body("enter valid quantity");
            }
            return ResponseEntity.badRequest().body("not authorized");
        }
        return ResponseEntity.badRequest().body("no such book");
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
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isPresent()){
            Optional<Cart> cart = cartRepository.findByUser(user.get());
            if (cart.isPresent()) {
                message.sendMessage(cart.get());
                return  ResponseEntity.ok().body("Message " + cart + " was sent to admin");
            }
            return ResponseEntity.ok().body("empty cart");
        }
        return ResponseEntity.badRequest().body("not authorized");
    }



    //TODO
    //change deleteUser(deleteCartItems and deleteCart)
}
