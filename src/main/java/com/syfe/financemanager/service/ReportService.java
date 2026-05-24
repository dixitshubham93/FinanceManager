package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.response.MonthlyReportResponse;
import com.syfe.financemanager.dto.response.YearlyReportResponse;
import com.syfe.financemanager.entity.Transaction;
import com.syfe.financemanager.enums.TransactionType;
import com.syfe.financemanager.repository.TransactionRepository;
import com.syfe.financemanager.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(int year, int month) {
        Long userId = securityUtils.getCurrentUserId();

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndYearAndMonth(userId, year, month);

        Map<String, BigDecimal> totalIncome = aggregateByCategory(transactions, TransactionType.INCOME);
        Map<String, BigDecimal> totalExpenses = aggregateByCategory(transactions, TransactionType.EXPENSE);

        BigDecimal netSavings = sumValues(totalIncome).subtract(sumValues(totalExpenses));

        return MonthlyReportResponse.builder()
                .month(month)
                .year(year)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(netSavings)
                .build();
    }

    @Transactional(readOnly = true)
    public YearlyReportResponse getYearlyReport(int year) {
        Long userId = securityUtils.getCurrentUserId();

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndYear(userId, year);

        Map<String, BigDecimal> totalIncome = aggregateByCategory(transactions, TransactionType.INCOME);
        Map<String, BigDecimal> totalExpenses = aggregateByCategory(transactions, TransactionType.EXPENSE);

        BigDecimal netSavings = sumValues(totalIncome).subtract(sumValues(totalExpenses));

        return YearlyReportResponse.builder()
                .year(year)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(netSavings)
                .build();
    }

    private Map<String, BigDecimal> aggregateByCategory(List<Transaction> transactions, TransactionType type) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        transactions.stream()
                .filter(t -> t.getCategory().getType() == type)
                .forEach(t -> {
                    String categoryName = t.getCategory().getName();
                    result.merge(categoryName, t.getAmount(), BigDecimal::add);
                });
        return result;
    }

    private BigDecimal sumValues(Map<String, BigDecimal> map) {
        return map.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
