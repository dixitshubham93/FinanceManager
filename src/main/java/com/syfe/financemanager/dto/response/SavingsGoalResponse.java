package com.syfe.financemanager.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class SavingsGoalResponse {

    private Long id;
    private String goalName;
    private BigDecimal targetAmount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    private BigDecimal currentProgress;

    private BigDecimal progressPercentage;

    private BigDecimal remainingAmount;
}
