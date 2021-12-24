package com.web.jwtauth.repository;


import com.web.jwtauth.models.Author;
import com.web.jwtauth.models.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.security.DenyAll;
import java.util.List;


@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    public List<Genre> findAll();

    @DenyAll
    Boolean existsByTitle(String title);
}