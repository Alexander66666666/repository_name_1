package com.example.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@SuppressWarnings("unused")
@Data
public class AuthRequest {
    @NotBlank(message = "имя пользователя является обязательным")
    private String username;

    @NotBlank(message = "Пароль обязателен")
    private String password;
}