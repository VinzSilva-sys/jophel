package com.org.PisofMind.services;

import com.org.PisofMind.dtos.AddExpenseRequest;
import com.org.PisofMind.dtos.ExpenseResponse;
import com.org.PisofMind.entities.Expense;
import com.org.PisofMind.entities.User;
import com.org.PisofMind.enums.ExpenseCategory;
import com.org.PisofMind.repositories.ExpenseRepository;
import com.org.PisofMind.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ExpenseService - Expense Management Service Layer.
 * 
 * Single Responsibility: Handles all expense-related business logic.
 * Implements:
 * - Adding expenses with validation
 * - Retrieving user expenses
 * - Filtering expenses by category
 * - Triggering rank evaluation after expense addition
 */
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final RankService rankService;

    /**
     * Add a new expense for a user.
     * Validates input, creates expense, saves it, and triggers rank evaluation.
     * 
     * @param request - AddExpenseRequest with userId, amount, category, description
     * @return ExpenseResponse with saved expense details
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException if user not found
     */
    @Transactional
    public ExpenseResponse addExpense(AddExpenseRequest request) {
        // Input validation
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }

        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }

        // Retrieve user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Convert category string to enum
        ExpenseCategory category;
        try {
            category = ExpenseCategory.valueOf(request.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + request.getCategory());
        }

        // Create and save expense (Using Information Expert pattern - User manages its expenses)
        Expense expense = new Expense(
                request.getAmount(),
                category,
                request.getDescription(),
                user
        );

        Expense savedExpense = expenseRepository.save(expense);

        // Trigger rank evaluation - user behavior changed
        rankService.evaluateAndUpdateRank(user.getId());

        return new ExpenseResponse(
                savedExpense.getId(),
                savedExpense.getAmount(),
                savedExpense.getCategory(),
                savedExpense.getDescription(),
                savedExpense.getCreatedAt()
        );
    }

    /**
     * Get all expenses for a user, ordered by most recent first.
     * 
     * @param userId - User ID
     * @return List of ExpenseResponse objects
     */
    public List<ExpenseResponse> getUserExpenses(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        return user.getExpenses().stream()
                .sorted((e1, e2) -> e2.getCreatedAt().compareTo(e1.getCreatedAt()))
                .map(expense -> new ExpenseResponse(
                        expense.getId(),
                        expense.getAmount(),
                        expense.getCategory(),
                        expense.getDescription(),
                        expense.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get expenses filtered by category.
     * 
     * @param userId - User ID
     * @param category - ExpenseCategory to filter by
     * @return List of ExpenseResponse objects for that category
     */
    public List<ExpenseResponse> getExpensesByCategory(Long userId, ExpenseCategory category) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        return expenseRepository.findByUserIdAndCategory(userId, category).stream()
                .sorted((e1, e2) -> e2.getCreatedAt().compareTo(e1.getCreatedAt()))
                .map(expense -> new ExpenseResponse(
                        expense.getId(),
                        expense.getAmount(),
                        expense.getCategory(),
                        expense.getDescription(),
                        expense.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Delete an expense (with authorization check in controller).
     */
    @Transactional
    public void deleteExpense(Long expenseId) {
        if (expenseId == null) {
            throw new IllegalArgumentException("Expense ID cannot be null");
        }

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalStateException("Expense not found"));

        Long userId = expense.getUser().getId();
        expenseRepository.deleteById(expenseId);

        // Trigger rank re-evaluation after deletion
        rankService.evaluateAndUpdateRank(userId);
    }
}
