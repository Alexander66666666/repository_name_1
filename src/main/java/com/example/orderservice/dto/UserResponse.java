package com.example.orderservice.dto;

import com.example.orderservice.entity.Role;
import lombok.Data;

import java.util.UUID;
@SuppressWarnings("unused")
@Data
public class UserResponse {
    private UUID id;
    private String username;
    private Role role;
}