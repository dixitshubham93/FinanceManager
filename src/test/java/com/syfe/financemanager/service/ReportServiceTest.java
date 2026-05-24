package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.response.MonthlyReportResponse;
import com.syfe.financemanager.dto.response.YearlyReportResponse;
import com.syfe.financemanager.entity.Category;
import com.syfe.financemanager.entity.Transaction;
import com.syfe.financemanager.entity.User;
import com.syfe.financemanager.enums.TransactionType;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService Unit Tests")
class ReportServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks private ReportService reportService;

    private User user;
    private Transaction incomeTransaction;
    private Transaction expenseTransaction;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();

        Category salaryCategory = Category.builder().id(1L).name("Salary")
                .type(TransactionType.INCOME).build();
        Category foodCategory = Category.builder().id(2L).name("Food")
                .type(TransactionType.EXPENSE).build();

        incomeTransaction = Transaction.builder()
                .id(1L).amount(new BigDecimal("3000.00"))
                .date(LocalDate.of(2024, 1, 15))
                .category(salaryCategory).user(user).isDeleted(false).build();

        expenseTransaction = Transaction.builder()
                .id(2L).amount(new BigDecimal("400.00"))
                .date(LocalDate.of(2024, 1, 20))
                .category(foodCategory).user(user).isDeleted(false).build();
    }

    @Test
    @DisplayName("Monthly: aggregates income and expenses by category for a month")
    void getMonthlyReport_success() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(transactionRepository.findByUserIdAndYearAndMonth(1L, 2024, 1))
                .thenReturn(List.of(incomeTransaction, expenseTransaction));

        MonthlyReportResponse result = reportService.getMonthlyReport(2024, 1);

        assertThat(result.getMonth()).isEqualTo(1);
        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getTotalIncome()).containsEntry("Salary", new BigDecimal("3000.00"));
        assertThat(result.getTotalExpenses()).containsEntry("Food", new BigDecimal("400.00"));
        assertThat(result.getNetSavings()).isEqualByComparingTo("2600.00");
    }

    @Test
    @DisplayName("Monthly: returns empty maps and zero savings when no transactions")
    void getMonthlyReport_noTransactions() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(transactionRepository.findByUserIdAndYearAndMonth(1L, 2024, 6))
                .thenReturn(List.of());

        MonthlyReportResponse result = reportService.getMonthlyReport(2024, 6);

        assertThat(result.getTotalIncome()).isEmpty();
        assertThat(result.getTotalExpenses()).isEmpty();
        assertThat(result.getNetSavings()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Yearly: aggregates income and expenses by category for a full year")
    void getYearlyReport_success() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(transactionRepository.findByUserIdAndYear(1L, 2024))
                .thenReturn(List.of(incomeTransaction, expenseTransaction));

        YearlyReportResponse result = reportService.getYearlyReport(2024);

        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getTotalIncome()).containsEntry("Salary", new BigDecimal("3000.00"));
        assertThat(result.getTotalExpenses()).containsEntry("Food", new BigDecimal("400.00"));
        assertThat(result.getNetSavings()).isEqualByComparingTo("2600.00");
    }

    @Test
    @DisplayName("Monthly: negative net savings when expenses exceed income")
    void getMonthlyReport_negativeNetSavings() {
        Category rentCategory = Category.builder().id(3L).name("Rent")
                .type(TransactionType.EXPENSE).build();
        Transaction bigExpense = Transaction.builder()
                .id(3L).amount(new BigDecimal("5000.00"))
                .date(LocalDate.of(2024, 1, 1))
                .category(rentCategory).user(user).isDeleted(false).build();

        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(transactionRepository.findByUserIdAndYearAndMonth(1L, 2024, 1))
                .thenReturn(List.of(incomeTransaction, bigExpense));

        MonthlyReportResponse result = reportService.getMonthlyReport(2024, 1);

        assertThat(result.getNetSavings()).isNegative();
    }
}
