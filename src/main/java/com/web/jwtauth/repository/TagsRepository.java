package com.web.jwtauth.repository;


import com.web.jwtauth.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.security.DenyAll;
import java.util.List;


@Repository
public interface TagsRepository extends JpaRepository<Tag, Long> {
    public List<Tag> findAll();

    @DenyAll
    Boolean existsByTitle(String title);
}