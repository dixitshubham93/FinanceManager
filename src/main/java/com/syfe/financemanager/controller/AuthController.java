package com.syfe.financemanager.controller;

import com.syfe.financemanager.dto.request.LoginRequest;
import com.syfe.financemanager.dto.request.RegisterRequest;
import com.syfe.financemanager.dto.response.MessageResponse;
import com.syfe.financemanager.dto.response.RegisterResponse;
import com.syfe.financemanager.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, and logout")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user",
            description = "Creates a new user account. Username must be a valid email address.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Login",
            description = "Authenticates the user and returns a session cookie (JSESSIONID). " +
                    "Include this cookie in all subsequent requests.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful — JSESSIONID cookie set"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<MessageResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        MessageResponse response = authService.login(request, servletRequest, servletResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout", description = "Invalidates the current session and clears the session cookie.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        MessageResponse response = authService.logout(servletRequest, servletResponse);
        return ResponseEntity.ok(response);
    }
}
