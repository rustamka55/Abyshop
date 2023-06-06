package com.web.jwtauth.repository;


import com.web.jwtauth.models.Product;
import com.web.jwtauth.models.ProductCategory;
import com.web.jwtauth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByTitle(String title);

    Boolean existsByTitle(String title);

    List<Product> findByProductCategory(ProductCategory productCategory);

}