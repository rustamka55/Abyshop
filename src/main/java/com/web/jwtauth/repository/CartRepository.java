package com.web.jwtauth.repository;

import com.web.jwtauth.models.Cart;
import com.web.jwtauth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {
    @Override
    Optional<Cart> findById(Long aLong);

    Optional<Cart> findByUser(User user);
}
