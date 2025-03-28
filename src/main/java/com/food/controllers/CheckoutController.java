package com.food.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.food.models.Order;
import com.food.models.dtos.OrderDetailDTO;
import com.food.services.CheckoutService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {
    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Order> processOrder(@Valid @RequestBody Order data) {
        Order savedOrder = checkoutService.processOrder(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<OrderDetailDTO> getOrderStatus(@PathVariable Long id) {
        OrderDetailDTO orderDetailDTO = checkoutService.getOrderStatus(id);
        return ResponseEntity.ok(orderDetailDTO);
    }
}
