package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.InventoryDTO;
import com.kovanlabs.project.model.WarehouseInventory;
import com.kovanlabs.project.service.WarehouseInventoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse/inventory")
public class WarehouseInventoryController {

    private final WarehouseInventoryService service;

    public WarehouseInventoryController(WarehouseInventoryService service) {
        this.service = service;
    }

    @PostMapping
    public WarehouseInventory addOrUpdateIngredient(@RequestBody InventoryDTO dto) {
        return service.addOrUpdateIngredient(dto);
    }

}