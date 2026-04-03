package com.kovanlabs.project.controller;

import com.kovanlabs.project.model.Bill;
import com.kovanlabs.project.service.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final OrderService orderService;

    public BillController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/order/{orderId}")
    public Bill getByOrderId(@PathVariable Long orderId) {
        return orderService.getBillByOrderId(orderId);
    }
}
