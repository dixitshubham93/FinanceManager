package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.request.SavingsGoalRequest;
import com.syfe.financemanager.dto.request.UpdateGoalRequest;
import com.syfe.financemanager.dto.response.GoalListResponse;
import com.syfe.financemanager.dto.response.MessageResponse;
import com.syfe.financemanager.dto.response.SavingsGoalResponse;
import com.syfe.financemanager.entity.SavingsGoal;
import com.syfe.financemanager.entity.User;
import com.syfe.financemanager.enums.TransactionType;
import com.syfe.financemanager.exception.ResourceNotFoundException;
import com.syfe.financemanager.repository.SavingsGoalRepository;
import com.syfe.financemanager.repository.TransactionRepository;
import com.syfe.financemanager.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public SavingsGoalResponse createGoal(SavingsGoalRequest request) {
        User user = securityUtils.getCurrentUser();

        LocalDate startDate = request.getStartDate() != null
                ? request.getStartDate()
                : LocalDate.now();

        SavingsGoal goal = SavingsGoal.builder()
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .startDate(startDate)
                .user(user)
                .build();

        SavingsGoal saved = savingsGoalRepository.save(goal);
        log.info("Savings goal created: id={}, user={}", saved.getId(), user.getUsername());
        return buildGoalResponse(saved, user.getId());
    }

    @Transactional(readOnly = true)
    public GoalListResponse getAllGoals() {
        Long userId = securityUtils.getCurrentUserId();
        List<SavingsGoalResponse> goals = savingsGoalRepository
                .findAllByUserId(userId)
                .stream()
                .map(goal -> buildGoalResponse(goal, userId))
                .toList();
        return new GoalListResponse(goals);
    }

    @Transactional(readOnly = true)
    public SavingsGoalResponse getGoalById(Long id) {
        Long userId = securityUtils.getCurrentUserId();
        SavingsGoal goal = savingsGoalRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal", id));
        return buildGoalResponse(goal, userId);
    }

    @Transactional
    public SavingsGoalResponse updateGoal(Long id, UpdateGoalRequest request) {
        Long userId = securityUtils.getCurrentUserId();

        SavingsGoal goal = savingsGoalRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal", id));

        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }

        if (request.getTargetDate() != null) {
            goal.setTargetDate(request.getTargetDate());
        }

        SavingsGoal updated = savingsGoalRepository.save(goal);
        log.info("Savings goal updated: id={}", updated.getId());
        return buildGoalResponse(updated, userId);
    }

    @Transactional
    public MessageResponse deleteGoal(Long id) {
        Long userId = securityUtils.getCurrentUserId();

        SavingsGoal goal = savingsGoalRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal", id));

        savingsGoalRepository.delete(goal);
        log.info("Savings goal deleted: id={}", id);
        return new MessageResponse("Goal deleted successfully");
    }

    private SavingsGoalResponse buildGoalResponse(SavingsGoal goal, Long userId) {
        BigDecimal totalIncome = transactionRepository
                .sumByUserIdAndTypeAndDateAfter(userId, TransactionType.INCOME, goal.getStartDate());
        BigDecimal totalExpense = transactionRepository
                .sumByUserIdAndTypeAndDateAfter(userId, TransactionType.EXPENSE, goal.getStartDate());

        BigDecimal currentProgress = totalIncome.subtract(totalExpense);
        BigDecimal targetAmount = goal.getTargetAmount();

        BigDecimal progressPercentage = targetAmount.compareTo(BigDecimal.ZERO) > 0
                ? currentProgress
                .divide(targetAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal remainingAmount = targetAmount.subtract(currentProgress);

        return SavingsGoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .targetDate(goal.getTargetDate())
                .startDate(goal.getStartDate())
                .currentProgress(currentProgress.setScale(2, RoundingMode.HALF_UP))
                .progressPercentage(progressPercentage)
                .remainingAmount(remainingAmount.setScale(2, RoundingMode.HALF_UP))
                .build();
    }
}
