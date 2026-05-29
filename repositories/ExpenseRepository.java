package com.org.PisofMind.repositories;

import com.org.PisofMind.entities.Expense;
import com.org.PisofMind.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ExpenseRepository - Repository Layer for Expense entity.
 * 
 * Single Responsibility: Handles all database operations for Expense entity.
 * Provides custom query methods for expense filtering.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * Find all expenses for a specific user.
     * Used by expense service to retrieve user's financial history.
     */
    List<Expense> findByUserId(Long userId);

    /**
     * Find all expenses for a user filtered by category.
     * Used for category-based expense breakdown in summary.
     */
    List<Expense> findByUserIdAndCategory(Long userId, ExpenseCategory category);

    /**
     * Count expenses for a user.
     * Used for analytics and dashboard.
     */
    long countByUserId(Long userId);
}
