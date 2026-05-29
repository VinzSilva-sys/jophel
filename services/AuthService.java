package com.org.PisofMind.services;

import com.org.PisofMind.dtos.*;
import com.org.PisofMind.entities.User;
import com.org.PisofMind.repositories.UserRepository;
import com.org.PisofMind.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * AuthService - Authentication Service Layer.
 * 
 * Single Responsibility: Handles all authentication-related business logic.
 * Implements:
 * - User registration with validation
 * - User login with password verification
 * - JWT token generation
 * - Password hashing using BCrypt
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * Register a new user.
     * Validates input, checks if user exists, hashes password, and saves to database.
     * 
     * @param request - RegisterRequest with email, password, gender, age
     * @return AuthResponse with success status and message
     * @throws IllegalArgumentException if validation fails
     */
    public AuthResponse registerUser(RegisterRequest request) {
        // Input validation
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        if (request.getAge() < 12 || request.getAge() > 35) {
            throw new IllegalArgumentException("Age must be between 12 and 35");
        }

        if (request.getGender() == null || request.getGender().trim().isEmpty()) {
            throw new IllegalArgumentException("Gender cannot be empty");
        }

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        // Create new user with hashed password and initial budget
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setGender(request.getGender());
        user.setAge(request.getAge());
        user.setBudget(BigDecimal.ZERO);  // User sets budget later

        userRepository.save(user);

        return new AuthResponse("User registered successfully", true);
    }

    /**
     * Authenticate user and generate JWT token.
     * Validates email and password, then generates a JWT token for subsequent requests.
     * 
     * @param request - LoginRequest with email and password
     * @return LoginResponse with JWT token and user ID
     * @throws IllegalArgumentException if credentials are invalid
     */
    public LoginResponse loginUser(LoginRequest request) {
        // Input validation
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        User user = userOptional.get();

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtUtils.generateToken(user.getId());

        return new LoginResponse(token, user.getId(), "Login successful");
    }

    /**
     * Retrieve user by ID - used for authorization checks.
     */
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Verify if provided token is valid and return associated user.
     */
    public Optional<User> getUserFromToken(String token) {
        if (!jwtUtils.validateToken(token)) {
            return Optional.empty();
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        if (userId == null) {
            return Optional.empty();
        }

        return userRepository.findById(userId);
    }
}
