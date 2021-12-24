package com.web.jwtauth.repository;


import com.web.jwtauth.models.Author;
import com.web.jwtauth.models.BookCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.security.DenyAll;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<BookCategory, Long> {
    public List<BookCategory> findAll();

    @DenyAll
    Boolean existsByTitle(String title);
}