package com.web.jwtauth.repository;

import com.web.jwtauth.models.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.security.DenyAll;
import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    public List<Author> findAll();
    @DenyAll
    Boolean existsByFirstNameAndLastName(String firstname,String lastname);
}
