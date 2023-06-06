package com.web.jwtauth.repository;

import com.web.jwtauth.models.RefreshToken;
import com.web.jwtauth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteAllByUser(User user);

    @Modifying
    int deleteByUser(User user);
}
