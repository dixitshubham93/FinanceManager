package com.syfe.financemanager.controller;

import com.syfe.financemanager.dto.request.SavingsGoalRequest;
import com.syfe.financemanager.dto.request.UpdateGoalRequest;
import com.syfe.financemanager.dto.response.GoalListResponse;
import com.syfe.financemanager.dto.response.MessageResponse;
import com.syfe.financemanager.dto.response.SavingsGoalResponse;
import com.syfe.financemanager.service.SavingsGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@Tag(name = "Savings Goals", description = "Manage savings goals with live progress tracking")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    @Operation(summary = "Create a savings goal",
            description = "Creates a new savings goal. startDate defaults to today if not provided. targetDate must be in the future.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Goal created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping
    public ResponseEntity<SavingsGoalResponse> createGoal(
            @Valid @RequestBody SavingsGoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savingsGoalService.createGoal(request));
    }

    @Operation(summary = "Get all savings goals",
            description = "Returns all savings goals for the authenticated user with live progress data.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Goals retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public ResponseEntity<GoalListResponse> getAllGoals() {
        return ResponseEntity.ok(savingsGoalService.getAllGoals());
    }

    @Operation(summary = "Get a savings goal by ID",
            description = "Returns a single savings goal with live progress calculation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Goal retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Goal belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> getGoalById(@PathVariable Long id) {
        return ResponseEntity.ok(savingsGoalService.getGoalById(id));
    }

    @Operation(summary = "Update a savings goal",
            description = "Updates targetAmount and/or targetDate. goalName and startDate are immutable.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Goal updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Goal belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGoalRequest request) {
        return ResponseEntity.ok(savingsGoalService.updateGoal(id, request));
    }

    @Operation(summary = "Delete a savings goal",
            description = "Permanently deletes a savings goal.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Goal deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Goal belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteGoal(@PathVariable Long id) {
        return ResponseEntity.ok(savingsGoalService.deleteGoal(id));
    }
}
