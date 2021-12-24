package com.web.jwtauth.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@ToString
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private @NotBlank String title;

    @Column(columnDefinition = "LONGTEXT")
    private @NotBlank String description;

    @Column
    private @NotBlank String imageURL;

    @Column
    private @NotBlank String publication;

    @Column
    private @NotNull Integer count;

    @Column
    @NotBlank
    private String binding;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "genre",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))

    private Set<Genre> genres = new HashSet<Genre>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id"))

    private Set<Author> authors = new HashSet<Author>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bookCategory_id")
    private BookCategory bookCategory;

    private Double cost;

    public Book(String title, String description, String imageURL, String publication, Integer count, String binding, Set<Genre> genres, Set<Author> authors, BookCategory bookCategory, Double cost) {
        this.title = title;
        this.description = description;
        this.imageURL = imageURL;
        this.publication = publication;
        this.count = count;
        this.binding = binding;
        this.genres = genres;
        this.authors = authors;
        this.bookCategory = bookCategory;
        this.cost = cost;
    }

    //getters and setters
}
