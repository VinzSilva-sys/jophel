package com.org.PisofMind.services;

import com.org.PisofMind.entities.Expense;
import com.org.PisofMind.entities.User;
import com.org.PisofMind.enums.ExpenseCategory;
import com.org.PisofMind.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * SummaryService - Financial Summary Service Layer.
 * 
 * Single Responsibility: Provides comprehensive financial overview for users.
 * Generates summary data including:
 * - Total spent
 * - Remaining budget
 * - Top spending category
 * - Expense breakdown by category
 */
@Service
@RequiredArgsConstructor
public class SummaryService {

    private final UserRepository userRepository;

    /**
     * Get comprehensive financial summary for a user.
     * Includes total spent, remaining budget, top category, and category breakdown.
     * 
     * @param userId - User ID
     * @return Map with summary data
     */
    public Map<String, Object> getUserSummary(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Map<String, Object> summary = new HashMap<>();

        BigDecimal totalSpent = user.getTotalSpent();
        BigDecimal remainingBudget = user.getRemainingBudget();

        summary.put("totalSpent", totalSpent);
        summary.put("remainingBudget", remainingBudget);
        summary.put("budget", user.getBudget());
        summary.put("rank", user.getRank());
        summary.put("totalExpenses", user.getExpenses().size());

        // Calculate expense breakdown by category
        Map<String, BigDecimal> categoryBreakdown = calculateCategoryBreakdown(user);
        summary.put("expenseBreakdown", categoryBreakdown);

        // Find top spending category
        String topCategory = findTopCategory(categoryBreakdown);
        summary.put("topCategory", topCategory);

        // Calculate spending percentage
        if (user.getBudget() != null && user.getBudget().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal spendingPercentage = totalSpent
                    .divide(user.getBudget(), 2, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            summary.put("spendingPercentage", spendingPercentage);
        } else {
            summary.put("spendingPercentage", BigDecimal.ZERO);
        }

        // Warning flag if over budget
        summary.put("isOverBudget", user.isOverBudget());

        return summary;
    }

    /**
     * Calculate total spending for each expense category.
     * 
     * @param user - User entity
     * @return Map of category name to total amount
     */
    private Map<String, BigDecimal> calculateCategoryBreakdown(User user) {
        Map<String, BigDecimal> breakdown = new HashMap<>();

        // Initialize all categories with zero
        for (ExpenseCategory category : ExpenseCategory.values()) {
            breakdown.put(category.toString(), BigDecimal.ZERO);
        }

        // Sum expenses by category
        for (Expense expense : user.getExpenses()) {
            String categoryName = expense.getCategory().toString();
            BigDecimal currentAmount = breakdown.getOrDefault(categoryName, BigDecimal.ZERO);
            breakdown.put(categoryName, currentAmount.add(expense.getAmount()));
        }

        return breakdown;
    }

    /**
     * Find the category with highest spending.
     * 
     * @param categoryBreakdown - Map of category totals
     * @return Category name with highest spending, or "N/A" if no expenses
     */
    private String findTopCategory(Map<String, BigDecimal> categoryBreakdown) {
        return categoryBreakdown.entrySet().stream()
                .max((entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue()))
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }
}
