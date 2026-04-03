package com.kovanlabs.project.controller;

import com.kovanlabs.project.model.LowStockAlert;
import com.kovanlabs.project.service.LowStockAlertService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class LowStockAlertController {

    private final LowStockAlertService lowStockAlertService;

    public LowStockAlertController(LowStockAlertService lowStockAlertService) {
        this.lowStockAlertService = lowStockAlertService;
    }

    @GetMapping("/owner/open")
    public List<LowStockAlert> ownerOpen(Authentication authentication) {
        return lowStockAlertService.getOwnerOpenAlerts(authentication.getName());
    }

    @GetMapping("/manager/open")
    public List<LowStockAlert> managerOpen(Authentication authentication) {
        return lowStockAlertService.getManagerOpenAlerts(authentication.getName());
    }
}
