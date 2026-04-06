package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.OrderDTO;
import com.kovanlabs.project.dto.CustomerOrderDTO;
import com.kovanlabs.project.model.User;
import com.kovanlabs.project.service.OrderService;
import com.kovanlabs.project.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService service;
    private final UserService userService;

    public OrderController(OrderService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    private Long resolveAuthenticatedBranchId(Authentication auth, Long fallbackBranchId) {
        if (auth == null) return fallbackBranchId;
        try {
            User user = userService.findByEmail(auth.getName());
            if (user != null && user.getBranch() != null) {
                logger.info("[BRANCH-GUARD] Overriding frontend branchId={} with authenticated user's branchId={}",
                        fallbackBranchId, user.getBranch().getId());
                return user.getBranch().getId();
            }
        } catch (Exception e) {
            logger.warn("[BRANCH-GUARD] Could not resolve branch for user: {}", auth.getName());
        }
        return fallbackBranchId;
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderDTO dto, Authentication auth) {
        try {
            dto.setBranchId(resolveAuthenticatedBranchId(auth, dto.getBranchId()));
            return ResponseEntity.ok(service.placeOrder(dto));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Order Error: " + e.getMessage());
        }
    }

    @PostMapping("/customer")
    public ResponseEntity<?> placeCustomerOrder(@RequestBody CustomerOrderDTO dto, Authentication auth) {
        try {
            dto.setBranchId(resolveAuthenticatedBranchId(auth, dto.getBranchId()));
            return ResponseEntity.ok(service.placeCustomerOrder(dto));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Billing Error: " + e.getMessage());
        }
    }
}