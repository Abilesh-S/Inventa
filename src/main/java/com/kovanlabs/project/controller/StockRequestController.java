package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.StockRequestCreateDTO;
import com.kovanlabs.project.dto.StockRequestDecisionDTO;
import com.kovanlabs.project.model.StockRequest;
import com.kovanlabs.project.service.StockRequestService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-requests")
public class StockRequestController {

    private final StockRequestService stockRequestService;

    public StockRequestController(StockRequestService stockRequestService) {
        this.stockRequestService = stockRequestService;
    }

    @PostMapping
    public StockRequest create(@RequestBody StockRequestCreateDTO dto, Authentication authentication) {
        return stockRequestService.createRequest(dto, authentication.getName());
    }

    @GetMapping("/pending")
    public List<StockRequest> getPending(Authentication authentication) {
        return stockRequestService.getPendingForOwner(authentication.getName());
    }

    @GetMapping("/my-branch")
    public List<StockRequest> getMyBranchRequests(Authentication authentication) {
        return stockRequestService.getRequestsForManager(authentication.getName());
    }

    @PostMapping("/{id}/approve")
    public org.springframework.http.ResponseEntity<?> approve(
            @PathVariable("id") Long id,
            @RequestBody(required = false) StockRequestDecisionDTO dto,
            org.springframework.security.core.Authentication authentication
    ) {
        try {
            String remark = dto == null ? null : dto.getRemark();
            return org.springframework.http.ResponseEntity.ok(stockRequestService.approve(id, authentication.getName(), remark));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    public org.springframework.http.ResponseEntity<?> reject(
            @PathVariable("id") Long id,
            @RequestBody(required = false) StockRequestDecisionDTO dto,
            org.springframework.security.core.Authentication authentication
    ) {
        try {
            String remark = dto == null ? null : dto.getRemark();
            return org.springframework.http.ResponseEntity.ok(stockRequestService.reject(id, authentication.getName(), remark));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/receive")
    public org.springframework.http.ResponseEntity<?> receive(
            @PathVariable("id") Long id,
            org.springframework.security.core.Authentication authentication
    ) {
        try {
            return org.springframework.http.ResponseEntity.ok(stockRequestService.receive(id, authentication.getName()));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
