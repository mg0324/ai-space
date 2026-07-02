package com.keycard.controller;

import com.keycard.dto.*;
import com.keycard.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check")
    public Map<String, Boolean> check() {
        boolean hasUser = authService.hasUser();
        return Map.of("hasUser", hasUser);
    }

    @PostMapping("/logout")
    public Map<String, String> logout() {
        // JWT is stateless; frontend clears the token
        // Server-side just confirms the action
        SecurityContextHolder.clearContext();
        return Map.of("message", "退出成功");
    }
}
