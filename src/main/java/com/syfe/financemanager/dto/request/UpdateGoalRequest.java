package com.syfe.financemanager.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateGoalRequest {

    @Positive(message = "Target amount must be a positive value")
    private BigDecimal targetAmount;

    @Future(message = "Target date must be a future date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;
}
