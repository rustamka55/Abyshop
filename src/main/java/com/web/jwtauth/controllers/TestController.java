package com.web.jwtauth.controllers;

import com.web.jwtauth.jms.Message;
import com.web.jwtauth.models.*;
import com.web.jwtauth.payload.request.AddAuthorRequest;
import com.web.jwtauth.payload.request.AddBookRequest;
import com.web.jwtauth.payload.request.AddCategoryRequest;
import com.web.jwtauth.payload.request.AddGenreRequest;
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
    BookRepository bookRepository;

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    GenreRepository genreRepository;

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
                .body("Message " + receiveMessage + " get from queue");
    }

    @PostMapping("/addBook")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> addBook(@RequestBody AddBookRequest addBookRequest) {
        if(!(addBookRequest.getTitle().isPresent() || addBookRequest.getDescription().isPresent()
                || addBookRequest.getCount().isPresent() || addBookRequest.getGenres().isPresent()
                || addBookRequest.getAuthors().isPresent()
                || addBookRequest.getBookCategory().isPresent() || addBookRequest.getCost().isPresent())){
            return ResponseEntity.badRequest().body(new MessageResponse("Please Enter Valid Information"));
        }

        if (bookRepository.existsByTitle(addBookRequest.getTitle().get())){
            return ResponseEntity.badRequest().body(new MessageResponse("book with this title already exists"));
        }

        Set<Genre> genreSet = new HashSet<>();
        for (Genre g : addBookRequest.getGenres().get()){
            Optional<Genre> genre = genreRepository.findById(g.getId());
            genre.ifPresent(genreSet::add);
        }

        Set<Author> authorSet = new HashSet<>();
        for (Author a : addBookRequest.getAuthors().get()){
            Optional<Author> author = authorRepository.findById(a.getId());
            author.ifPresent(authorSet::add);
        }

        Optional<BookCategory> bc = categoryRepository.findById(addBookRequest.getBookCategory().get().getId());
        BookCategory bookCategory = null;
        if(bc.isPresent()){
            bookCategory = bc.get();
        }

        if(authorSet.isEmpty() || genreSet.isEmpty() || bookCategory == null){
            return ResponseEntity.badRequest().body(new MessageResponse("Please Enter Valid Genres, Authors and bookCategory"));
        }

        Book book = new Book(addBookRequest.getTitle().get(), addBookRequest.getDescription().get(),
                addBookRequest.getImageURL().get(), addBookRequest.getPublication().get(),
                addBookRequest.getCount().get(),addBookRequest.getBinding().get(),
                genreSet, authorSet, bookCategory, addBookRequest.getCost().get());

        bookRepository.save(book);
        return ResponseEntity.ok().body(book);
    }

    @PutMapping("/updateBook/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> editBook(@PathVariable Long id, @RequestBody AddBookRequest addBookRequest) {

        Optional<Book> book = bookRepository.findById(id);

        if(!book.isPresent()){
            return ResponseEntity.badRequest().body("there is no book with this id");
        }

        if(addBookRequest.getTitle().isPresent()){
            book.get().setTitle(addBookRequest.getTitle().get());
        }
        if(addBookRequest.getDescription().isPresent()){
            book.get().setDescription(addBookRequest.getDescription().get());
        }
        if(addBookRequest.getImageURL().isPresent()){
            book.get().setImageURL(addBookRequest.getImageURL().get());
        }
        if(addBookRequest.getPublication().isPresent()){
            book.get().setPublication(addBookRequest.getPublication().get());
        }
        if(addBookRequest.getCount().isPresent()){
            book.get().setCount(addBookRequest.getCount().get());
        }
        if(addBookRequest.getBinding().isPresent()){
            book.get().setBinding(addBookRequest.getBinding().get());
        }
        if(addBookRequest.getCost().isPresent()){
            book.get().setCost(addBookRequest.getCost().get());
        }


        if(addBookRequest.getGenres().isPresent()){
            Set<Genre> genreSet = new HashSet<>();
            for (Genre g : addBookRequest.getGenres().get()){
                Optional<Genre> genre = genreRepository.findById(g.getId());
                genre.ifPresent(genreSet::add);
            }
            book.get().setGenres(genreSet);
        }

        if(addBookRequest.getAuthors().isPresent()){
            Set<Author> authorSet = new HashSet<>();
            for (Author a : addBookRequest.getAuthors().get()){
                Optional<Author> author = authorRepository.findById(a.getId());
                author.ifPresent(authorSet::add);
            }
            book.get().setAuthors(authorSet);
        }


        if (addBookRequest.getBookCategory().isPresent()){
            Optional<BookCategory> bc = categoryRepository.findById(addBookRequest.getBookCategory().get().getId());
            BookCategory bookCategory = null;
            if(bc.isPresent()){
                bookCategory = bc.get();
            }
            book.get().setBookCategory(bookCategory);
        }

        bookRepository.save(book.get());
        return ResponseEntity.ok().body(book.get());
    }

    @GetMapping("getBooks")
    @PermitAll
    public ResponseEntity<?> getBooks(){
        return ResponseEntity.ok(bookRepository.findAll());
    }

    @GetMapping("getBook/{id}")
    @PermitAll
    public ResponseEntity<?> getBook(@PathVariable Long id){
        return ResponseEntity.ok(bookRepository.findById(id));
    }

    @DeleteMapping("deleteBook/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteBook(@PathVariable Long id){
        Optional<Book> book = bookRepository.findById(id);
        book.ifPresent(value -> bookRepository.delete(value));
        return ResponseEntity.ok().body("Successfully deleted:" + book);
    }

    @DeleteMapping("deleteBooks")
    @DenyAll
    public ResponseEntity<?> deleteAllBooks(){
        bookRepository.deleteAll();
        return ResponseEntity.ok().body("Successfully deleted all books, thanks <3");
    }

    @PostMapping("/addAuthor")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> addAuthor(@RequestBody AddAuthorRequest addAuthorRequest ) {
        if(!(addAuthorRequest.getFirstName().isPresent() || addAuthorRequest.getLastName().isPresent())){
            return ResponseEntity.badRequest().body(new MessageResponse("pls enter fname and lname"));
        }
        if (authorRepository.existsByFirstNameAndLastName(addAuthorRequest.getFirstName().get(), addAuthorRequest.getLastName().get())){
            return ResponseEntity.badRequest().body(new MessageResponse("this author already exists"));
        }
        Author author = new Author(addAuthorRequest.getFirstName().get(),addAuthorRequest.getLastName().get());
        authorRepository.save(author);
        return ResponseEntity.ok().body(author);
    }

    @PutMapping("/updateAuthor/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateAuthor(@PathVariable Long id,@RequestBody AddAuthorRequest addAuthorRequest ) {
        Optional<Author> author = authorRepository.findById(id);
        if(author.isPresent()){
            if (addAuthorRequest.getFirstName().isPresent()){
                author.get().setFirstName(addAuthorRequest.getFirstName().get());
            }
            if (addAuthorRequest.getLastName().isPresent()){
                author.get().setLastName(addAuthorRequest.getLastName().get());
            }
            authorRepository.save(author.get());
            return ResponseEntity.ok().body(author);
        }
        else{
            return ResponseEntity.badRequest().body("no such author present");
        }
    }

    @GetMapping("getAuthors")
    @PermitAll
    public ResponseEntity<?> getAuthors(){
        return ResponseEntity.ok(authorRepository.findAll());
    }

    @GetMapping("getAuthor/{id}")
    @PermitAll
    public ResponseEntity<?> getAuthor(@PathVariable Long id){
        return ResponseEntity.ok(authorRepository.findById(id));
    }

    @DeleteMapping("deleteAuthor/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteAuthor(@PathVariable Long id){
        Optional<Author> author = authorRepository.findById(id);
        author.ifPresent(value -> authorRepository.delete(value));
        return ResponseEntity.ok().body("Successfully deleted:" + author);
    }

    @DeleteMapping("deleteAuthors")
    @DenyAll
    public ResponseEntity<?> deleteAllAuthors(){
        authorRepository.deleteAll();
        return ResponseEntity.ok().body("Successfully deleted all authors, thanks <3");
    }






    @PostMapping("/addGenre")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> addGenre(@RequestBody AddGenreRequest addGenreRequest ) {
        if(!(addGenreRequest.getTitle().isPresent() || addGenreRequest.getDescription().isPresent())){
            return ResponseEntity.badRequest().body(new MessageResponse("Please Enter Valid Information"));
        }
        if (genreRepository.existsByTitle(addGenreRequest.getTitle().get())){
            return ResponseEntity.badRequest().body(new MessageResponse("This Category is already exist"));
        }
        Genre genre = new Genre(addGenreRequest.getTitle().get(),addGenreRequest.getDescription().get());
        genreRepository.save(genre);
        return ResponseEntity.ok().body(genre);
    }

    @PutMapping("/updateGenre/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateGenre(@PathVariable Long id,@RequestBody AddGenreRequest addGenreRequest ) {
        Optional<Genre> genre = genreRepository.findById(id);
        if(genre.isPresent()){
            if (addGenreRequest.getTitle().isPresent()){
                genre.get().setTitle(addGenreRequest.getTitle().get());
            }
            if (addGenreRequest.getDescription().isPresent()){
                genre.get().setDescription(addGenreRequest.getDescription().get());
            }
            genreRepository.save(genre.get());
            return ResponseEntity.ok().body(genre);
        }
        else{
            return ResponseEntity.badRequest().body("no such genre present");
        }
    }

    @GetMapping("getGenres")
    @PermitAll
    public ResponseEntity<?> getGenres(){
        return ResponseEntity.ok(genreRepository.findAll());
    }

    @GetMapping("getGenre/{id}")
    @PermitAll
    public ResponseEntity<?> getGenre(@PathVariable Long id){
        return ResponseEntity.ok(genreRepository.findById(id));
    }

    @DeleteMapping("deleteGenre/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteGenre(@PathVariable Long id){
        Optional<Genre> genre = genreRepository.findById(id);
        genre.ifPresent(value -> genreRepository.delete(value));
        return ResponseEntity.ok().body("Successfully deleted:" + genre);
    }

    @DeleteMapping("deleteGenres")
    @DenyAll
    public ResponseEntity<?> deleteAllGenres(){
        genreRepository.deleteAll();
        return ResponseEntity.ok().body("Successfully deleted all genres, thanks <3");
    }




    @PostMapping("/addCategory")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> addAuthor(@RequestBody AddCategoryRequest addCategoryRequest ) {
        if(!(addCategoryRequest.getTitle().isPresent() || addCategoryRequest.getDescription().isPresent())){
            return ResponseEntity.badRequest().body(new MessageResponse("Please Enter Valid Information"));
        }
        if (categoryRepository.existsByTitle(addCategoryRequest.getTitle().get())){
            return ResponseEntity.badRequest().body(new MessageResponse("This Category already exists"));
        }
        BookCategory bookCategory = new BookCategory(addCategoryRequest.getTitle().get(),addCategoryRequest.getDescription().get());
        categoryRepository.save(bookCategory);
        return ResponseEntity.ok().body(bookCategory);
    }

    @PutMapping("/updateCategory/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateCategory(@PathVariable Long id,@RequestBody AddCategoryRequest addCategoryRequest) {
        Optional<BookCategory> bookCategory = categoryRepository.findById(id);
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
            return ResponseEntity.badRequest().body("no such category present");
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
        Optional<BookCategory> bookCategory= categoryRepository.findById(id);
        bookCategory.ifPresent(value -> categoryRepository.delete(value));
        return ResponseEntity.ok().body("Successfully deleted:" + bookCategory);
    }

    @DeleteMapping("deleteCategories")
    @DenyAll
    public ResponseEntity<?> deleteAllCategories(){
        categoryRepository.deleteAll();
        return ResponseEntity.ok().body("Successfully deleted all categories, thanks <3");
    }

    @DeleteMapping("deleteUser/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
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
            return ResponseEntity.ok("User " + user + " was successfully deleted");
        }
        return ResponseEntity.badRequest().body("Such user does not exist");
    }
}
