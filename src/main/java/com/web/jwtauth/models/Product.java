package com.web.jwtauth.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@ToString
public class Product {

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
    private @NotNull Integer count;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "tag",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))

    private Set<Tag> tags = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "productCategory_id")
    private ProductCategory productCategory;

    private Double cost;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    public Product(String title, String description, String imageURL, Integer count, Set<Tag> tags, ProductCategory productCategory, Double cost, User user) {
        this.title = title;
        this.description = description;
        this.imageURL = imageURL;
        this.count = count;
        this.tags = tags;
        this.productCategory = productCategory;
        this.cost = cost;
        this.user = user;
    }

    //getters and setters
}
