package com.syfe.financemanager.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.syfe.financemanager.entity.Transaction;
import com.syfe.financemanager.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class TransactionResponse {

    private Long id;
    private BigDecimal amount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String category;
    private String description;
    private TransactionType type;

    public static TransactionResponse from(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .date(transaction.getDate())
                .category(transaction.getCategory().getName())
                .description(transaction.getDescription())
                .type(transaction.getCategory().getType())
                .build();
    }
}
