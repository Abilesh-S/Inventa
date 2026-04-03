package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.WarehouseDTO;
import com.kovanlabs.project.model.Warehouse;
import com.kovanlabs.project.service.WarehouseService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PostMapping
    public Warehouse createWarehouse(@RequestBody WarehouseDTO dto) {
        return warehouseService.createWarehouse(dto);
    }
}