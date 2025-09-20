package com.example.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@SuppressWarnings("unused")
@Data
public class OrderRequest {
    @NotBlank(message = "Description is mandatory")
    private String description;
}