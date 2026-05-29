package com.org.PisofMind.services;

import com.org.PisofMind.entities.User;
import com.org.PisofMind.enums.UserRank;
import com.org.PisofMind.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * RankService - Gamification Service Layer.
 * 
 * Single Responsibility: Handles rank evaluation and updates based on savings behavior.
 * Implements gamification logic to encourage financial discipline.
 * 
 * Rank system:
 * - ROOKIE_SAVER: Starting rank
 * - ELITE_SAVER: Spent < 50% of budget
 * - MASTER_SAVER: Spent < 30% of budget
 * - LEGENDARY_SAVER: Spent < 15% of budget
 * - MYTHICAL_SAVER: Spent < 5% of budget
 * - IMMORTAL_SAVER: Never spent anything
 */
@Service
@RequiredArgsConstructor
public class RankService {

    private final UserRepository userRepository;

    /**
     * Evaluate and update user rank based on current spending behavior.
     * Called after every expense addition or deletion.
     * 
     * @param userId - User ID whose rank needs evaluation
     */
    @Transactional
    public void evaluateAndUpdateRank(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        UserRank newRank = calculateRank(user);

        if (!user.getRank().equals(newRank)) {
            user.setRank(newRank);
            userRepository.save(user);
        }
    }

    /**
     * Calculate the appropriate rank for a user based on spending behavior.
     * 
     * Logic:
     * - No budget set yet → ROOKIE_SAVER
     * - Never spent anything → IMMORTAL_SAVER
     * - Spent < 5% of budget → MYTHICAL_SAVER
     * - Spent < 15% of budget → LEGENDARY_SAVER
     * - Spent < 30% of budget → MASTER_SAVER
     * - Spent < 50% of budget → ELITE_SAVER
     * - Spent >= 50% of budget → ROOKIE_SAVER
     */
    private UserRank calculateRank(User user) {
        // No budget set yet
        if (user.getBudget() == null || user.getBudget().compareTo(BigDecimal.ZERO) <= 0) {
            return UserRank.ROOKIE_SAVER;
        }

        BigDecimal totalSpent = user.getTotalSpent();

        // Never spent anything
        if (totalSpent.compareTo(BigDecimal.ZERO) == 0) {
            return UserRank.IMMORTAL_SAVER;
        }

        // Calculate spending percentage
        BigDecimal spendingPercentage = totalSpent
                .divide(user.getBudget(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        if (spendingPercentage.compareTo(BigDecimal.valueOf(5)) < 0) {
            return UserRank.MYTHICAL_SAVER;
        } else if (spendingPercentage.compareTo(BigDecimal.valueOf(15)) < 0) {
            return UserRank.LEGENDARY_SAVER;
        } else if (spendingPercentage.compareTo(BigDecimal.valueOf(30)) < 0) {
            return UserRank.MASTER_SAVER;
        } else if (spendingPercentage.compareTo(BigDecimal.valueOf(50)) < 0) {
            return UserRank.ELITE_SAVER;
        } else {
            return UserRank.ROOKIE_SAVER;
        }
    }

    /**
     * Get current rank for a user.
     */
    public UserRank getUserRank(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        return user.getRank();
    }
}
