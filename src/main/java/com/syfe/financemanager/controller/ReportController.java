package com.syfe.financemanager.controller;

import com.syfe.financemanager.dto.response.MonthlyReportResponse;
import com.syfe.financemanager.dto.response.YearlyReportResponse;
import com.syfe.financemanager.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Monthly and yearly financial reports")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Monthly financial report",
            description = "Returns total income by category, total expenses by category, and net savings for a specific month.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly report generated successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(reportService.getMonthlyReport(year, month));
    }

    @Operation(summary = "Yearly financial report",
            description = "Returns aggregated total income by category, total expenses by category, and net savings for a full year.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Yearly report generated successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(@PathVariable int year) {
        return ResponseEntity.ok(reportService.getYearlyReport(year));
    }
}
