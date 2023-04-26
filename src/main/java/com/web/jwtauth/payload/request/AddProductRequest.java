package com.web.jwtauth.payload.request;

import com.web.jwtauth.models.User;
import com.web.jwtauth.models.ProductCategory;
import com.web.jwtauth.models.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddProductRequest {

    private Optional<String> title = Optional.empty();


    private Optional<String> description = Optional.empty();


    private Optional<String> imageURL = Optional.of("static/asd.jpg");


    private Optional<Integer> count = Optional.empty();


    private Optional<Set<Tag>> tags = Optional.empty();

    private Optional<ProductCategory> productCategory = Optional.empty();

    private Optional<Double> cost = Optional.empty();

    private Optional<User> user = Optional.empty();
}
