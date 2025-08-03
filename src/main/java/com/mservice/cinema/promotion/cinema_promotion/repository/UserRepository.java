package com.mservice.cinema.promotion.cinema_promotion.repository;

import com.mservice.cinema.promotion.cinema_promotion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Find user by name
    Optional<User> findByName(String name);
    
    // Check if user exists by name
    boolean existsByName(String name);
} 