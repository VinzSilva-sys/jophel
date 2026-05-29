package com.org.PisofMind.entities;

import com.org.PisofMind.enums.ExpenseCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Expense Entity - Represents a student expense.
 * 
 * Single Responsibility: Represents an expense record with amount, category, and metadata.
 * Keeps both auditing fields (createdAt) and belongs to a User (Low Coupling).
 */
@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Constructor for creating expense without ID (for new records).
     */
    public Expense(BigDecimal amount, ExpenseCategory category, String description, User user) {
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }
}
