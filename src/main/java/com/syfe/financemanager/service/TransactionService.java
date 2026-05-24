package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.request.TransactionRequest;
import com.syfe.financemanager.dto.request.UpdateTransactionRequest;
import com.syfe.financemanager.dto.response.MessageResponse;
import com.syfe.financemanager.dto.response.TransactionListResponse;
import com.syfe.financemanager.dto.response.TransactionResponse;
import com.syfe.financemanager.entity.Category;
import com.syfe.financemanager.entity.Transaction;
import com.syfe.financemanager.entity.User;
import com.syfe.financemanager.exception.BadRequestException;
import com.syfe.financemanager.exception.ResourceNotFoundException;
import com.syfe.financemanager.repository.CategoryRepository;
import com.syfe.financemanager.repository.TransactionRepository;
import com.syfe.financemanager.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        User user = securityUtils.getCurrentUser();

        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be a future date");
        }

        Category category = categoryRepository
                .findAccessibleByNameAndUserId(request.getCategory(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found or not accessible: " + request.getCategory()));

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .description(request.getDescription())
                .category(category)
                .user(user)
                .isDeleted(false)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction created: id={}, user={}", saved.getId(), user.getUsername());
        return TransactionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public TransactionListResponse getTransactions(LocalDate startDate, LocalDate endDate, Long categoryId) {
        Long userId = securityUtils.getCurrentUserId();

        List<TransactionResponse> transactions = transactionRepository
                .findAllByUserIdWithFilters(userId, startDate, endDate, categoryId)
                .stream()
                .map(TransactionResponse::from)
                .toList();

        return new TransactionListResponse(transactions);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, UpdateTransactionRequest request) {
        Long userId = securityUtils.getCurrentUserId();

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }

        if (request.getCategory() != null) {
            Category category = categoryRepository
                    .findAccessibleByNameAndUserId(request.getCategory(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found or not accessible: " + request.getCategory()));
            transaction.setCategory(category);
        }

        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        Transaction updated = transactionRepository.save(transaction);
        log.info("Transaction updated: id={}", updated.getId());
        return TransactionResponse.from(updated);
    }

    @Transactional
    public MessageResponse deleteTransaction(Long id) {
        Long userId = securityUtils.getCurrentUserId();

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));

        transaction.setDeleted(true);
        transactionRepository.save(transaction);
        log.info("Transaction soft-deleted: id={}", id);

        return new MessageResponse("Transaction deleted successfully");
    }
}
