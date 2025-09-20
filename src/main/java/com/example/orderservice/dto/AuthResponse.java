package com.example.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@SuppressWarnings("unused")
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
}