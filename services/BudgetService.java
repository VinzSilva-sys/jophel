package com.org.PisofMind.services;

import com.org.PisofMind.dtos.BudgetResponse;
import com.org.PisofMind.dtos.CategoryBudgetResponse;
import com.org.PisofMind.dtos.SetBudgetRequest;
import com.org.PisofMind.entities.CategoryBudget;
import com.org.PisofMind.entities.User;
import com.org.PisofMind.enums.ExpenseCategory;
import com.org.PisofMind.repositories.CategoryBudgetRepository;
import com.org.PisofMind.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BudgetService - Budget Management Service Layer.
 *
 * Single Responsibility: Handles all budget-related business logic.
 * Implements:
 * - Setting user category budgets
 * - Calculating total and per-category remaining budgets
 * - Providing budget overview
 */
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final UserRepository userRepository;
    private final CategoryBudgetRepository categoryBudgetRepository;

    /**
     * Set category budget for a user.
     * Validates that budget is positive and user exists.
     *
     * @param request - SetBudgetRequest with userId, category, and budget amount
     * @return BudgetResponse with updated budget information
     * @throws IllegalArgumentException if budget is invalid
     * @throws IllegalStateException if user not found
     */
    @Transactional
    public BudgetResponse setBudget(SetBudgetRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (request.getCategory() == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        if (request.getBudget() == null || request.getBudget().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget must be greater than 0");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        CategoryBudget categoryBudget = categoryBudgetRepository
                .findByUserIdAndCategory(user.getId(), request.getCategory())
                .orElseGet(() -> new CategoryBudget(null, user, request.getCategory(), request.getBudget()));

        categoryBudget.setBudget(request.getBudget());
        categoryBudgetRepository.save(categoryBudget);

        return buildBudgetResponse(user);
    }

    /**
     * Get current budget status for a user.
     * Includes total budget and per-category budgets.
     *
     * @param userId - User ID
     * @return BudgetResponse with current budget information
     */
    public BudgetResponse getBudgetStatus(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        return buildBudgetResponse(user);
    }

    private BudgetResponse buildBudgetResponse(User user) {
        List<CategoryBudget> budgets = categoryBudgetRepository.findByUserId(user.getId());
        Map<ExpenseCategory, BigDecimal> spentByCategory = calculateSpentByCategory(user);

        BigDecimal totalBudget = budgets.stream()
                .map(CategoryBudget::getBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSpent = user.getTotalSpent();
        BigDecimal totalRemaining = totalBudget.subtract(totalSpent);

        List<CategoryBudgetResponse> categoryBudgets = budgets.stream()
                .map(cb -> {
                    BigDecimal spent = spentByCategory.getOrDefault(cb.getCategory(), BigDecimal.ZERO);
                    BigDecimal remaining = cb.getBudget().subtract(spent);
                    return new CategoryBudgetResponse(cb.getCategory(), cb.getBudget(), spent, remaining);
                })
                .collect(Collectors.toList());

        return new BudgetResponse(
                user.getId(),
                totalBudget,
                totalSpent,
                totalRemaining,
                categoryBudgets
        );
    }

    private Map<ExpenseCategory, BigDecimal> calculateSpentByCategory(User user) {
        Map<ExpenseCategory, BigDecimal> spentByCategory = new HashMap<>();
        for (ExpenseCategory category : ExpenseCategory.values()) {
            spentByCategory.put(category, BigDecimal.ZERO);
        }

        user.getExpenses().forEach(expense ->
                spentByCategory.merge(expense.getCategory(), expense.getAmount(), BigDecimal::add)
        );

        return spentByCategory;
    }
}
