package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.WarehouseDTO;
import com.kovanlabs.project.model.User;
import com.kovanlabs.project.model.Warehouse;
import com.kovanlabs.project.service.UserService;
import com.kovanlabs.project.service.WarehouseService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/warehouse")
@CrossOrigin(origins = "http://localhost:5173")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final UserService userService;

    public WarehouseController(WarehouseService warehouseService, UserService userService) {
        this.warehouseService = warehouseService;
        this.userService = userService;
    }

    @GetMapping
    public List<Warehouse> getAllWarehouses(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        if (user == null || user.getBusiness() == null) {
            throw new RuntimeException("Invalid user context");
        }
        Warehouse warehouse = warehouseService.getOrCreateWarehouseForBusiness(user.getBusiness().getId());
        return java.util.List.of(warehouse);
    }

    @PostMapping
    public Warehouse createWarehouse(@RequestBody WarehouseDTO dto) {
        return warehouseService.createWarehouse(dto);
    }
}