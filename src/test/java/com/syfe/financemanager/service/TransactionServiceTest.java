package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.request.TransactionRequest;
import com.syfe.financemanager.dto.request.UpdateTransactionRequest;
import com.syfe.financemanager.dto.response.MessageResponse;
import com.syfe.financemanager.dto.response.TransactionListResponse;
import com.syfe.financemanager.dto.response.TransactionResponse;
import com.syfe.financemanager.entity.Category;
import com.syfe.financemanager.entity.Transaction;
import com.syfe.financemanager.entity.User;
import com.syfe.financemanager.enums.TransactionType;
import com.syfe.financemanager.exception.BadRequestException;
import com.syfe.financemanager.exception.ResourceNotFoundException;
import com.syfe.financemanager.repository.CategoryRepository;
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
@DisplayName("TransactionService Unit Tests")
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks private TransactionService transactionService;

    private User user;
    private Category salaryCategory;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();
        salaryCategory = Category.builder().id(1L).name("Salary")
                .type(TransactionType.INCOME).isCustom(false).isDeleted(false).build();
        transaction = Transaction.builder()
                .id(1L).amount(new BigDecimal("5000.00"))
                .date(LocalDate.now().minusDays(1))
                .description("Test salary").category(salaryCategory)
                .user(user).isDeleted(false).build();
    }

    @Test
    @DisplayName("Create: successfully creates a transaction")
    void createTransaction_success() {
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(categoryRepository.findAccessibleByNameAndUserId("Salary", 1L))
                .thenReturn(Optional.of(salaryCategory));
        when(transactionRepository.save(any())).thenReturn(transaction);

        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("5000.00"));
        request.setDate(LocalDate.now().minusDays(1));
        request.setCategory("Salary");
        request.setDescription("Test salary");

        TransactionResponse result = transactionService.createTransaction(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo("5000.00");
        assertThat(result.getCategory()).isEqualTo("Salary");
        assertThat(result.getType()).isEqualTo(TransactionType.INCOME);
    }

    @Test
    @DisplayName("Create: throws BadRequestException for future date")
    void createTransaction_futureDate_throwsBadRequest() {
        when(securityUtils.getCurrentUser()).thenReturn(user);

        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setDate(LocalDate.now().plusDays(1)); 
        request.setCategory("Salary");

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("future date");
    }

    @Test
    @DisplayName("Create: throws ResourceNotFoundException for unknown category")
    void createTransaction_unknownCategory_throwsNotFound() {
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(categoryRepository.findAccessibleByNameAndUserId("Unknown", 1L))
                .thenReturn(Optional.empty());

        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setDate(LocalDate.now());
        request.setCategory("Unknown");

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("GetAll: returns filtered transactions sorted by newest first")
    void getTransactions_withFilters() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(transactionRepository.findAllWithoutCategory(eq(1L), any(), any()))
                .thenReturn(List.of(transaction));

        TransactionListResponse result = transactionService.getTransactions(null, null, null);

        assertThat(result.getTransactions()).hasSize(1);
        assertThat(result.getTransactions().get(0).getCategory()).isEqualTo("Salary");
    }

    @Test
    @DisplayName("Update: successfully updates amount and description")
    void updateTransaction_success() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any())).thenReturn(transaction);

        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setAmount(new BigDecimal("6000.00"));
        request.setDescription("Updated salary");

        TransactionResponse result = transactionService.updateTransaction(1L, request);

        assertThat(result).isNotNull();
        verify(transactionRepository).save(argThat(t -> t.getAmount().compareTo(new BigDecimal("6000.00")) == 0));
    }

    @Test
    @DisplayName("Update: date is NOT updated even if provided")
    void updateTransaction_dateIsImmutable() {

        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any())).thenReturn(transaction);

        LocalDate originalDate = transaction.getDate();
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setDescription("Changed desc");

        transactionService.updateTransaction(1L, request);

        verify(transactionRepository).save(argThat(t -> t.getDate().equals(originalDate)));
    }

    @Test
    @DisplayName("Update: throws ResourceNotFoundException when transaction not found")
    void updateTransaction_notFound_throwsNotFound() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(transactionRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.updateTransaction(99L, new UpdateTransactionRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Delete: soft-deletes a transaction")
    void deleteTransaction_success() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(transaction));

        MessageResponse result = transactionService.deleteTransaction(1L);

        assertThat(result.getMessage()).isEqualTo("Transaction deleted successfully");
        assertThat(transaction.isDeleted()).isTrue();
        verify(transactionRepository).save(transaction);
    }

    @Test
    @DisplayName("Delete: throws ResourceNotFoundException when transaction not found")
    void deleteTransaction_notFound_throwsNotFound() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(transactionRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
