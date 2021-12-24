package com.web.jwtauth.payload.request;

import com.web.jwtauth.models.Author;
import com.web.jwtauth.models.BookCategory;
import com.web.jwtauth.models.Genre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddBookRequest {


    private Optional<String> title = Optional.empty();


    private Optional<String> description = Optional.empty();


    private Optional<String> imageURL = Optional.of("static/asd.jpg");


    private Optional<String> publication = Optional.of("2020");

    private Optional<Integer> count = Optional.empty();


    private Optional<String> binding = Optional.of("soft");

    private Optional<Set<Genre>> genres = Optional.empty();

    private Optional<Set<Author>> authors = Optional.empty();

    private Optional<BookCategory> bookCategory = Optional.empty();

    private Optional<Double> cost = Optional.empty();
}
