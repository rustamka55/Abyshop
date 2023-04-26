package com.web.jwtauth.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "productCategory")
@Data
@NoArgsConstructor
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private @NotBlank String title;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private @NotBlank String description;

    public ProductCategory(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
