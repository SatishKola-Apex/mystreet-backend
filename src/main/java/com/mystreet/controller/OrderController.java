package com.mystreet.controller;

import com.mystreet.dto.OrderRequestDto;
import com.mystreet.entity.Order;
import com.mystreet.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Endpoints for handling checkout and order history")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Place an order", description = "Processes checkout, Calculates total, and updates inventory stock.")
    public ResponseEntity<Order> createOrder(
            @RequestParam UUID userId,
            @Valid @RequestBody OrderRequestDto requestDto) {
        Order createdOrder = orderService.createOrder(userId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order for a id", description = "Retrieves order for a given order id")
    public ResponseEntity<Order> getOrderById(@PathVariable UUID id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/mine")
    @Operation(summary = "Get user order history", description = "Retrieves all past orders placed by a specific user")
    public ResponseEntity<List<Order>> getMyOrders(@RequestParam UUID userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }
}
