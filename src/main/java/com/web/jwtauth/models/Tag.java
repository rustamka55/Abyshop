package com.web.jwtauth.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "tags")
@Data
@NoArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private @NotBlank String title;

    @Column(columnDefinition = "LONGTEXT")
    private @NotBlank String description;

    public Tag(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
