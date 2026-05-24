package com.syfe.financemanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GoalListResponse {
    private List<SavingsGoalResponse> goals;
}
