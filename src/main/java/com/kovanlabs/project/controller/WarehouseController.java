package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.WarehouseDTO;
import com.kovanlabs.project.model.Warehouse;
import com.kovanlabs.project.service.WarehouseService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/warehouse")
@CrossOrigin(origins = "http://localhost:5173")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    public List<Warehouse> getAllWarehouses() {
        return warehouseService.getWarehouses();
    }

    @PostMapping
    public Warehouse createWarehouse(@RequestBody WarehouseDTO dto) {
        return warehouseService.createWarehouse(dto);
    }
}