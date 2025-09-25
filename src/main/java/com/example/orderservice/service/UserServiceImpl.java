package com.example.orderservice.service;

import com.example.orderservice.dto.AuthRequest;
import com.example.orderservice.dto.AuthResponse;
import com.example.orderservice.entity.Role;
import com.example.orderservice.entity.User;
import com.example.orderservice.exception.UserAlreadyExistsException;
import com.example.orderservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public AuthResponse registerUser(AuthRequest request) {
        if (existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("User already exists: " + request.getUsername());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);
        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        savedUser.setRefreshToken(refreshToken);
        userRepository.save(savedUser);

        return new AuthResponse(accessToken, refreshToken, savedUser.getRole());
    }

    @Override
    public AuthResponse createAuthResponse(org.springframework.security.core.userdetails.User userDetails) {User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Обновляем refresh token в базе
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new AuthResponse(accessToken, refreshToken, user.getRole());
    }



    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public void deleteUserById(String id) {
        userRepository.deleteById(UUID.fromString(id));
    }

    @Override
    public void updateUserRefreshToken(UUID userId, String refreshToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
    }
}
