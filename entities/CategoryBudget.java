package com.org.PisofMind.entities;

import com.org.PisofMind.enums.ExpenseCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * CategoryBudget Entity - Stores a budget allocation for a specific expense category.
 *
 * Single Responsibility:
 * - Associates a budget amount with a user and expense category.
 */
@Entity
@Table(name = "category_budgets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "category"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal budget;
}
