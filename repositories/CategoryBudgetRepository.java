package com.org.PisofMind.repositories;

import com.org.PisofMind.entities.CategoryBudget;
import com.org.PisofMind.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryBudgetRepository extends JpaRepository<CategoryBudget, Long> {
    List<CategoryBudget> findByUserId(Long userId);
    Optional<CategoryBudget> findByUserIdAndCategory(Long userId, ExpenseCategory category);
}
