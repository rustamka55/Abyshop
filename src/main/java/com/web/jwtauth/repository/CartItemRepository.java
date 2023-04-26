package com.web.jwtauth.repository;

import com.web.jwtauth.models.Product;
import com.web.jwtauth.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Long> {
    Optional<CartItem> findByProduct(Product product);
}
