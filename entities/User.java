package com.org.PisofMind.entities;

import com.org.PisofMind.enums.UserRank;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User Entity - Information Expert Pattern
 * Responsible for storing user information, budget, and managing user-owned expenses.
 * 
 * Single Responsibility: Represents a user in the system with financial attributes.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal budget;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRank rank = UserRank.ROOKIE_SAVER;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Calculate total amount spent by user across all expenses.
     * Using Information Expert pattern - User knows about its own expenses.
     */
    public BigDecimal getTotalSpent() {
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate remaining budget after all expenses.
     */
    public BigDecimal getRemainingBudget() {
        return budget.subtract(getTotalSpent());
    }

    /**
     * Check if user has exceeded budget.
     */
    public boolean isOverBudget() {
        return getRemainingBudget().compareTo(BigDecimal.ZERO) < 0;
    }
}
