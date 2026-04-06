package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.InventoryDTO;
import com.kovanlabs.project.dto.WarehouseInventoryDTO;
import com.kovanlabs.project.model.WarehouseInventory;
import com.kovanlabs.project.service.WarehouseInventoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse/inventory")
@CrossOrigin(origins = "http://localhost:5173")
public class WarehouseInventoryController {

    private final WarehouseInventoryService service;

    public WarehouseInventoryController(WarehouseInventoryService service) {
        this.service = service;
    }

    @GetMapping
    public java.util.List<WarehouseInventory> getAllIngredients() {
        return service.getAllIngredients();
    }

    @PostMapping
    public WarehouseInventory addOrUpdateIngredient(@RequestBody WarehouseInventoryDTO dto) {
        return service.addOrUpdateIngredient(dto);
    }

    @GetMapping("/unique-names")
    public java.util.List<String> getUniqueIngredientNames() {
        return service.getUniqueIngredientNames();
    }

    @DeleteMapping("/{id}")
    public String deleteIngredient(@PathVariable Long id) {
        service.deleteIngredient(id);
        return "Deleted successfully";
    }
}