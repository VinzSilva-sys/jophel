package com.org.PisofMind.repositories;

import com.org.PisofMind.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository - Repository Layer for User entity.
 * 
 * Single Responsibility: Handles all database operations for User entity.
 * Extends JpaRepository for built-in CRUD operations (findAll, save, delete, etc).
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email - used for login and registration validation.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user with given email already exists.
     */
    boolean existsByEmail(String email);
}
