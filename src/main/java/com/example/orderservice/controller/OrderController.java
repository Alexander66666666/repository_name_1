package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.Status;
import com.example.orderservice.entity.User;
import com.example.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal User user) {
        Order order = new Order();
        order.setDescription(request.getDescription());
        Order savedOrder = orderService.createOrder(order, user);

        OrderResponse response = mapToOrderResponse(savedOrder);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(@AuthenticationPrincipal User user) {
        List<OrderResponse> orders = orderService.getOrdersByUser(user).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID id,
            @RequestParam Status status,
            @AuthenticationPrincipal User user) {
        // Проверка прав: только ADMIN
        boolean isAdmin = user.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ADMIN"));

        if (isAdmin) {
            return ResponseEntity.status(403).build();
        }

        return orderService.getOrderById(id)
                .map(order -> {
                    Order updatedOrder = orderService.updateOrderStatus(order, status);
                    return ResponseEntity.ok(mapToOrderResponse(updatedOrder));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteOrder(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return orderService.getOrderById(id)
                .map(order -> {
                    // Проверка прав: ADMIN или владелец заказа
                    boolean isAdmin = user.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ADMIN"));
                    boolean isOwner = order.getUser().getId().equals(user.getId());

                    if (isAdmin && !isOwner) {
                        return ResponseEntity.status(403).build();
                    }

                    orderService.deleteOrder(order);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUser().getId());
        response.setDescription(order.getDescription());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }
}