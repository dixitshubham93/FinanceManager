package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.request.RegisterRequest;
import com.syfe.financemanager.dto.request.LoginRequest;
import com.syfe.financemanager.dto.response.RegisterResponse;
import com.syfe.financemanager.dto.response.MessageResponse;
import com.syfe.financemanager.entity.User;
import com.syfe.financemanager.exception.ConflictException;
import com.syfe.financemanager.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private SecurityContextRepository securityContextRepository;
    @Mock private HttpServletRequest httpServletRequest;
    @Mock private HttpServletResponse httpServletResponse;
    @Mock private Authentication authentication;
    @Mock private HttpSession httpSession;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User");
        registerRequest.setPhoneNumber("+1234567890");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("Register: successfully registers a new user")
    void register_success() {
        when(userRepository.existsByUsername("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$12$hashedPassword");

        User savedUser = User.builder()
                .id(1L)
                .username("test@example.com")
                .password("$2a$12$hashedPassword")
                .fullName("Test User")
                .phoneNumber("+1234567890")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterResponse response = authService.register(registerRequest);

        assertThat(response.getMessage()).isEqualTo("User registered successfully");
        assertThat(response.getUserId()).isEqualTo(1L);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register: throws ConflictException when email already exists")
    void register_emailAlreadyExists_throwsConflictException() {
        when(userRepository.existsByUsername("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register: password is BCrypt encoded before saving")
    void register_passwordIsEncoded() {
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        User saved = User.builder().id(1L).username("test@example.com")
                .password("encoded").fullName("Test User").phoneNumber("+1234567890").build();
        when(userRepository.save(any())).thenReturn(saved);

        authService.register(registerRequest);

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(u -> "encoded".equals(u.getPassword())));
    }

    @Test
    @DisplayName("Login: successful login returns success message")
    void login_success() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        doNothing().when(securityContextRepository)
                .saveContext(any(), any(), any());

        MessageResponse response = authService.login(loginRequest, httpServletRequest, httpServletResponse);

        assertThat(response.getMessage()).isEqualTo("Login successful");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Login: throws BadCredentialsException for wrong password")
    void login_badCredentials_throwsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest, httpServletRequest, httpServletResponse))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Logout: invalidates session and returns success message")
    void logout_success_withSession() {
        when(httpServletRequest.getSession(false)).thenReturn(httpSession);

        MessageResponse response = authService.logout(httpServletRequest, httpServletResponse);

        assertThat(response.getMessage()).isEqualTo("Logout successful");
        verify(httpSession).invalidate();
    }

    @Test
    @DisplayName("Logout: returns success even when no active session")
    void logout_success_noSession() {
        when(httpServletRequest.getSession(false)).thenReturn(null);

        MessageResponse response = authService.logout(httpServletRequest, httpServletResponse);

        assertThat(response.getMessage()).isEqualTo("Logout successful");
        verify(httpSession, never()).invalidate();
    }
}
