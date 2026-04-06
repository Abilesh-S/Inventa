package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.OrderDTO;
import com.kovanlabs.project.dto.CustomerOrderDTO;
import com.kovanlabs.project.service.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public org.springframework.http.ResponseEntity<?> placeOrder(@RequestBody OrderDTO dto) {
        try {
            return org.springframework.http.ResponseEntity.ok(service.placeOrder(dto));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(500).body("Order Error: " + e.getMessage());
        }
    }

    @PostMapping("/customer")
    public org.springframework.http.ResponseEntity<?> placeCustomerOrder(@RequestBody CustomerOrderDTO dto) {
        try {
            return org.springframework.http.ResponseEntity.ok(service.placeCustomerOrder(dto));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(500).body("Billing Error: " + e.getMessage());
        }
    }
}