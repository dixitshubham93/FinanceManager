package com.syfe.financemanager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Username (email) is required")
    @Email(message = "Username must be a valid email address")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
