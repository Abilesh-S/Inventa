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

    @PostMapping("/{id}/approve")
    public StockRequest approve(
            @PathVariable("id") Long id,
            @RequestBody(required = false) StockRequestDecisionDTO dto,
            Authentication authentication
    ) {
        String remark = dto == null ? null : dto.getRemark();
        return stockRequestService.approve(id, authentication.getName(), remark);
    }

    @PostMapping("/{id}/reject")
    public StockRequest reject(
            @PathVariable("id") Long id,
            @RequestBody(required = false) StockRequestDecisionDTO dto,
            Authentication authentication
    ) {
        String remark = dto == null ? null : dto.getRemark();
        return stockRequestService.reject(id, authentication.getName(), remark);
    }
}
