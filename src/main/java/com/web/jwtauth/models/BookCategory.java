package com.web.jwtauth.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Set;

@Entity
@Table(name = "booksCategory")
@Data
@NoArgsConstructor
public class BookCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private @NotBlank String title;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private @NotBlank String description;

    public BookCategory(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
