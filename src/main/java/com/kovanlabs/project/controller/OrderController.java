package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.OrderDTO;
import com.kovanlabs.project.dto.CustomerOrderDTO;
import com.kovanlabs.project.dto.OrderBillResponseDTO;
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
    public OrderBillResponseDTO placeOrder(@RequestBody OrderDTO dto) {
        return service.placeOrder(dto);
    }

    @PostMapping("/customer")
    public OrderBillResponseDTO placeCustomerOrder(@RequestBody CustomerOrderDTO dto) {
        return service.placeCustomerOrder(dto);
    }
}