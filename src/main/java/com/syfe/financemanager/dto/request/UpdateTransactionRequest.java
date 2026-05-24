package com.syfe.financemanager.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateTransactionRequest {

    @Positive(message = "Amount must be a positive value")
    private BigDecimal amount;

    private String category;

    private String description;
}
