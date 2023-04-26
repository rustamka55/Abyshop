package com.web.jwtauth.repository;


import com.web.jwtauth.models.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.security.DenyAll;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<ProductCategory, Long> {
    public List<ProductCategory> findAll();

    @DenyAll
    Boolean existsByTitle(String title);
}