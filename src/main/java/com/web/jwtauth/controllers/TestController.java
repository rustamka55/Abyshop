package com.web.jwtauth.controllers;

import com.web.jwtauth.jms.Message;
import com.web.jwtauth.models.*;
import com.web.jwtauth.payload.request.AddProductRequest;
import com.web.jwtauth.payload.request.AddCategoryRequest;
import com.web.jwtauth.payload.request.AddTagRequest;
import com.web.jwtauth.payload.response.MessageResponse;
import com.web.jwtauth.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.jms.JMSException;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/")
public class TestController {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    TagsRepository tagsRepository;

    @Autowired
    Message message;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    RoleRepository roleRepository;

    @GetMapping("/receiveMessage")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<?> getMessage() throws JMSException {
        String receiveMessage = message.receiveMessage();
        return  ResponseEntity.ok()
                .body(new MessageResponse("Message " + receiveMessage + " get from queue"));
    }
    @PostMapping("/addProduct")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> addProduct(@RequestBody AddProductRequest addProductRequest) {
        if(!(addProductRequest.getTitle().isPresent() || addProductRequest.getDescription().isPresent()
                || addProductRequest.getCount().isPresent() || addProductRequest.getTags().isPresent()
                || addProductRequest.getProductCategory().isPresent() || addProductRequest.getCost().isPresent()
                || addProductRequest.getUser().isPresent())){
            return ResponseEntity.badRequest().body(new MessageResponse("Please Enter Valid Information"));
        }

        /*if (productRepository.existsByTitle(addProductRequest.getTitle().get())){
            return ResponseEntity.badRequest().body(new MessageResponse("book with this title already exists"));
        }*/

        Set<Tag> tagSet = new HashSet<>();
        for (Tag g : addProductRequest.getTags().get()){
            Optional<Tag> genre = tagsRepository.findById(g.getId());
            genre.ifPresent(tagSet::add);
        }

        Optional<ProductCategory> pc = categoryRepository.findById(addProductRequest.getProductCategory().get().getId());
        ProductCategory productCategory = null;
        if(pc.isPresent()){
            productCategory = pc.get();
        }

        if(tagSet.isEmpty() || productCategory == null){
            return ResponseEntity.badRequest().body(new MessageResponse("Please Enter Valid Tags and Category"));
        }

        Product product = new Product(addProductRequest.getTitle().get(),addProductRequest.getDescription().get(),
                addProductRequest.getImageURL().get(),addProductRequest.getCount().get(),addProductRequest.getTags().get(),
                addProductRequest.getProductCategory().get(),addProductRequest.getCost().get(),addProductRequest.getUser().get()
                );

        productRepository.save(product);
        return ResponseEntity.ok().body(product);
    }

    @PutMapping("/updateProduct/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> editProduct(@PathVariable Long id, @RequestBody AddProductRequest addProductRequest) {

        Optional<Product> product = productRepository.findById(id);

        if(!product.isPresent()){
            return ResponseEntity.badRequest().body(new MessageResponse("There is no product with this id"));
        }

        if(addProductRequest.getTitle().isPresent()){
            product.get().setTitle(addProductRequest.getTitle().get());
        }
        if(addProductRequest.getDescription().isPresent()){
            product.get().setDescription(addProductRequest.getDescription().get());
        }
        if(addProductRequest.getImageURL().isPresent()){
            product.get().setImageURL(addProductRequest.getImageURL().get());
        }
        if(addProductRequest.getCount().isPresent()){
            product.get().setCount(addProductRequest.getCount().get());
        }

        if(addProductRequest.getCost().isPresent()){
            product.get().setCost(addProductRequest.getCost().get());
        }



        if(addProductRequest.getTags().isPresent()){
            Set<Tag> tagSet = new HashSet<>();
            for (Tag g : addProductRequest.getTags().get()){
                Optional<Tag> tags = tagsRepository.findById(g.getId());
                tags.ifPresent(tagSet::add);
            }
            product.get().setTags(tagSet);
        }

        if (addProductRequest.getProductCategory().isPresent()){
            Optional<ProductCategory> pc = categoryRepository.findById(addProductRequest.getProductCategory().get().getId());
            ProductCategory productCategory = null;
            if(pc.isPresent()){
                productCategory = pc.get();
            }
            product.get().setProductCategory(productCategory);
        }

        if (addProductRequest.getUser().isPresent()){
            Optional<User> user = userRepository.findById(addProductRequest.getUser().get().getId());
            User userObj = null;
            if(user.isPresent()){
                userObj = user.get();
            }
            product.get().setUser(userObj);
        }

        productRepository.save(product.get());
        return ResponseEntity.ok().body(product.get());
    }

    @GetMapping("getProducts")
    @PermitAll
    public ResponseEntity<?> getProducts(){
        return ResponseEntity.ok(productRepository.findAll());
    }

    @GetMapping("getProduct/{id}")
    @PermitAll
    public ResponseEntity<?> getProduct(@PathVariable Long id){
        return ResponseEntity.ok(productRepository.findById(id));
    }

    @DeleteMapping("deleteProduct/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id){
        Optional<Product> product = productRepository.findById(id);
        product.ifPresent(value -> productRepository.delete(value));
        return ResponseEntity.ok().body(new MessageResponse("Successfully deleted:" + product));
    }

    @DeleteMapping("deleteProduct")
    @DenyAll
    public ResponseEntity<?> deleteAllProduct(){
        productRepository.deleteAll();
        return ResponseEntity.ok().body(new MessageResponse("Successfully deleted all products, thanks <3"));
    }

    @PostMapping("/addTags")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> addTags(@RequestBody AddTagRequest addTagRequest) {
        if(!(addTagRequest.getTitle().isPresent() || addTagRequest.getDescription().isPresent())){
            return ResponseEntity.badRequest().body(new MessageResponse("Please Enter Valid Information"));
        }
        if (tagsRepository.existsByTitle(addTagRequest.getTitle().get())){
            return ResponseEntity.badRequest().body(new MessageResponse("This Tag is already exist"));
        }
        Tag tag = new Tag(addTagRequest.getTitle().get(), addTagRequest.getDescription().get());
        tagsRepository.save(tag);
        return ResponseEntity.ok().body(tag);
    }

    @PutMapping("/updateTags/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateTags(@PathVariable Long id,@RequestBody AddTagRequest addTagRequest) {
        Optional<Tag> tags = tagsRepository.findById(id);
        if(tags.isPresent()){
            if (addTagRequest.getTitle().isPresent()){
                tags.get().setTitle(addTagRequest.getTitle().get());
            }
            if (addTagRequest.getDescription().isPresent()){
                tags.get().setDescription(addTagRequest.getDescription().get());
            }
            tagsRepository.save(tags.get());
            return ResponseEntity.ok().body(tags);
        }
        else{
            return ResponseEntity.badRequest().body(new MessageResponse("No such tags present"));
        }
    }

    @GetMapping("getTags")
    @PermitAll
    public ResponseEntity<?> getTags(){
        return ResponseEntity.ok(tagsRepository.findAll());
    }

    @GetMapping("getTags/{id}")
    @PermitAll
    public ResponseEntity<?> getTags(@PathVariable Long id){
        return ResponseEntity.ok(tagsRepository.findById(id));
    }

    @DeleteMapping("deleteTags/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteTags(@PathVariable Long id){
        Optional<Tag> tags = tagsRepository.findById(id);
        tags.ifPresent(value -> tagsRepository.delete(value));
        return ResponseEntity.ok().body(new MessageResponse("Successfully deleted:" + tags));
    }

    @DeleteMapping("deleteTags")
    @DenyAll
    public ResponseEntity<?> deleteAllTags(){
        tagsRepository.deleteAll();
        return ResponseEntity.ok().body(new MessageResponse("Successfully deleted all genres, thanks <3"));
    }




    @PostMapping("/addCategory")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> addCategory(@RequestBody AddCategoryRequest addCategoryRequest ) {
        if(!(addCategoryRequest.getTitle().isPresent() || addCategoryRequest.getDescription().isPresent())){
            return ResponseEntity.badRequest().body(new MessageResponse("Please Enter Valid Information"));
        }
        if (categoryRepository.existsByTitle(addCategoryRequest.getTitle().get())){
            return ResponseEntity.badRequest().body(new MessageResponse("This Category already exists"));
        }
        ProductCategory productCategory = new ProductCategory(addCategoryRequest.getTitle().get(),addCategoryRequest.getDescription().get());
        categoryRepository.save(productCategory);
        return ResponseEntity.ok().body(productCategory);
    }

    @PutMapping("/updateCategory/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateCategory(@PathVariable Long id,@RequestBody AddCategoryRequest addCategoryRequest) {
        Optional<ProductCategory> bookCategory = categoryRepository.findById(id);
        if(bookCategory.isPresent()){
            if (addCategoryRequest.getTitle().isPresent()){
                bookCategory.get().setTitle(addCategoryRequest.getTitle().get());
            }
            if (addCategoryRequest.getDescription().isPresent()){
                bookCategory.get().setDescription(addCategoryRequest.getDescription().get());
            }
            categoryRepository.save(bookCategory.get());
            return ResponseEntity.ok().body(bookCategory);
        }
        else{
            return ResponseEntity.badRequest().body(new MessageResponse("no such category present"));
        }
    }

    @GetMapping("getCategories")
    @PermitAll
    public ResponseEntity<?> getCategories(){
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @GetMapping("getCategory/{id}")
    @PermitAll
    public ResponseEntity<?> getCategory(@PathVariable Long id){
        return ResponseEntity.ok(categoryRepository.findById(id));
    }

    @DeleteMapping("deleteCategory/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id){
        Optional<ProductCategory> productCategory= categoryRepository.findById(id);
        productCategory.ifPresent(value -> categoryRepository.delete(value));
        return ResponseEntity.ok().body(new MessageResponse("Successfully deleted:" + productCategory));
    }

    @DeleteMapping("deleteCategories")
    @DenyAll
    public ResponseEntity<?> deleteAllCategories(){
        categoryRepository.deleteAll();
        return ResponseEntity.ok().body("Successfully deleted all categories, thanks <3");
    }

    @DeleteMapping("deleteUser/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            Optional<Cart> cart = cartRepository.findByUser(user.get());
            if (cart.isPresent()) {
                Set<CartItem> cartItems = cart.get().getCartItems();
                Set<CartItem> cartItemSet = new HashSet<>();
                cartItemRepository.deleteAll(cartItems);
                cart.get().setCartItems(cartItemSet);
                cartRepository.delete(cart.get());
            }
            Set<Role> roleSet = new HashSet<>();
            user.get().setRoles(roleSet);
            userRepository.delete(user.get());
            return ResponseEntity.ok(new MessageResponse("User " + user + " was successfully deleted"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Such user does not exist"));
    }
}
