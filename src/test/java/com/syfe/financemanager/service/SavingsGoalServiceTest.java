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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SavingsGoalService Unit Tests")
class SavingsGoalServiceTest {

    @Mock private SavingsGoalRepository savingsGoalRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks private SavingsGoalService savingsGoalService;

    private User user;
    private SavingsGoal goal;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();
        goal = SavingsGoal.builder()
                .id(1L).goalName("Emergency Fund")
                .targetAmount(new BigDecimal("5000.00"))
                .targetDate(LocalDate.now().plusMonths(6))
                .startDate(LocalDate.now().minusMonths(1))
                .user(user).build();
    }

    private void mockProgressCalculation() {
        when(transactionRepository.sumByUserIdAndTypeAndDateAfter(
                eq(1L), eq(TransactionType.INCOME), any()))
                .thenReturn(new BigDecimal("2000.00"));
        when(transactionRepository.sumByUserIdAndTypeAndDateAfter(
                eq(1L), eq(TransactionType.EXPENSE), any()))
                .thenReturn(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("Create: successfully creates a goal with provided startDate")
    void createGoal_withExplicitStartDate() {
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(savingsGoalRepository.save(any())).thenReturn(goal);
        mockProgressCalculation();

        SavingsGoalRequest request = new SavingsGoalRequest();
        request.setGoalName("Emergency Fund");
        request.setTargetAmount(new BigDecimal("5000.00"));
        request.setTargetDate(LocalDate.now().plusMonths(6));
        request.setStartDate(LocalDate.now().minusMonths(1));

        SavingsGoalResponse result = savingsGoalService.createGoal(request);

        assertThat(result.getGoalName()).isEqualTo("Emergency Fund");
        assertThat(result.getCurrentProgress()).isEqualByComparingTo("1000.00"); 
        assertThat(result.getProgressPercentage()).isEqualByComparingTo("20.00"); 
        assertThat(result.getRemainingAmount()).isEqualByComparingTo("4000.00");
    }

    @Test
    @DisplayName("Create: startDate defaults to today when not provided")
    void createGoal_defaultsStartDateToToday() {
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(goal);
        mockProgressCalculation();

        SavingsGoalRequest request = new SavingsGoalRequest();
        request.setGoalName("Emergency Fund");
        request.setTargetAmount(new BigDecimal("5000.00"));
        request.setTargetDate(LocalDate.now().plusMonths(6));

        savingsGoalService.createGoal(request);

        verify(savingsGoalRepository).save(argThat(g -> g.getStartDate().equals(LocalDate.now())));
    }

    @Test
    @DisplayName("GetAll: returns all goals with live progress")
    void getAllGoals_returnsAllWithProgress() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(savingsGoalRepository.findAllByUserId(1L)).thenReturn(List.of(goal));
        mockProgressCalculation();

        GoalListResponse result = savingsGoalService.getAllGoals();

        assertThat(result.getGoals()).hasSize(1);
        assertThat(result.getGoals().get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GetById: returns goal with live progress")
    void getGoalById_success() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(savingsGoalRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(goal));
        mockProgressCalculation();

        SavingsGoalResponse result = savingsGoalService.getGoalById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getGoalName()).isEqualTo("Emergency Fund");
    }

    @Test
    @DisplayName("GetById: throws ResourceNotFoundException when not found")
    void getGoalById_notFound() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(savingsGoalRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> savingsGoalService.getGoalById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Update: updates targetAmount and targetDate")
    void updateGoal_success() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(savingsGoalRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(goal));
        when(savingsGoalRepository.save(any())).thenReturn(goal);
        mockProgressCalculation();

        UpdateGoalRequest request = new UpdateGoalRequest();
        request.setTargetAmount(new BigDecimal("6000.00"));
        request.setTargetDate(LocalDate.now().plusMonths(9));

        SavingsGoalResponse result = savingsGoalService.updateGoal(1L, request);

        verify(savingsGoalRepository).save(argThat(g ->
                g.getTargetAmount().compareTo(new BigDecimal("6000.00")) == 0));
    }

    @Test
    @DisplayName("Delete: successfully deletes a goal")
    void deleteGoal_success() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(savingsGoalRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(goal));

        MessageResponse result = savingsGoalService.deleteGoal(1L);

        assertThat(result.getMessage()).isEqualTo("Goal deleted successfully");
        verify(savingsGoalRepository).delete(goal);
    }

    @Test
    @DisplayName("Delete: throws ResourceNotFoundException when not found")
    void deleteGoal_notFound() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(savingsGoalRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> savingsGoalService.deleteGoal(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
