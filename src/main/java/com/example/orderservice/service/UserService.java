package com.example.orderservice.service;

import com.example.orderservice.dto.AuthRequest;
import com.example.orderservice.dto.AuthResponse;
import com.example.orderservice.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.UUID;

public interface UserService  extends UserDetailsService {
    AuthResponse registerUser(AuthRequest request);
    AuthResponse createAuthResponse(org.springframework.security.core.userdetails.User user);
    List<User> getAllUsers();
    boolean existsByUsername(String username);
    void deleteUserById(String id);
    void updateUserRefreshToken(UUID userId, String refreshToken);
}
