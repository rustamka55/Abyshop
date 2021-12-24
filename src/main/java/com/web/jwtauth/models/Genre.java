package com.web.jwtauth.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "genres")
@Data
@NoArgsConstructor
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private @NotBlank String title;

    @Column(columnDefinition = "LONGTEXT")
    private @NotBlank String description;

    public Genre(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
