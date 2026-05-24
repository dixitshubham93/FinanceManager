package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.request.LoginRequest;
import com.syfe.financemanager.dto.request.RegisterRequest;
import com.syfe.financemanager.dto.response.MessageResponse;
import com.syfe.financemanager.dto.response.RegisterResponse;
import com.syfe.financemanager.entity.User;
import com.syfe.financemanager.exception.ConflictException;
import com.syfe.financemanager.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Email address is already registered: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getUsername());

        return new RegisterResponse("User registered successfully", savedUser.getId());
    }

    public MessageResponse login(LoginRequest request,
                                 HttpServletRequest servletRequest,
                                 HttpServletResponse servletResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, servletRequest, servletResponse);

        log.info("User logged in: {}", request.getUsername());
        return new MessageResponse("Login successful");
    }

    public MessageResponse logout(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {

        SecurityContextHolder.clearContext();

        HttpSession session = servletRequest.getSession(false);
        if (session != null) {
            String sessionId = session.getId();
            session.invalidate();
            log.info("Session invalidated: {}", sessionId);
        }

        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        servletResponse.addCookie(cookie);

        return new MessageResponse("Logout successful");
    }
}
