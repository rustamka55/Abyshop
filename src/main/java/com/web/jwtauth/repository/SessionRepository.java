package com.web.jwtauth.repository;

import com.web.jwtauth.models.Session;
import com.web.jwtauth.models.Tag;
import com.web.jwtauth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session,Long> {
    public List<Session> findAll();

    public void deleteAllByUser(User user);
    public List<Session> findSessionsByUser(User user);
}
