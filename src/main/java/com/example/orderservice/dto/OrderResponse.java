package com.example.orderservice.dto;

import com.example.orderservice.entity.Status;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
public class OrderResponse {
    private UUID id;
    private UUID userId;
    private String description;
    private Status status;
    private LocalDateTime createdAt;
}