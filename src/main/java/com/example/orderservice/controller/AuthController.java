package com.example.orderservice.controller;

import com.example.orderservice.dto.AuthRequest;
import com.example.orderservice.dto.AuthResponse;
import com.example.orderservice.dto.RefreshTokenRequest;
import com.example.orderservice.exception.UserAlreadyExistsException;
import com.example.orderservice.service.JwtService;
import com.example.orderservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        try {
            // ЗАМЕНЯЕМ старую логику на вызов сервиса
            AuthResponse authResponse = userService.registerUser(request);
            return ResponseEntity.ok(authResponse);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Теперь получаем AuthResponse из сервиса (с access + refresh токенами)
            AuthResponse authResponse = userService.createAuthResponse((org.springframework.security.core.userdetails.User) authentication.getPrincipal());
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // ДОБАВЛЯЕМ новый endpoint
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            // Этот метод нужно будет добавить в JwtService/UserService
            AuthResponse authResponse = jwtService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}