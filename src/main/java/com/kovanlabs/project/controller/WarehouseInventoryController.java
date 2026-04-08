package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.InventoryDTO;
import com.kovanlabs.project.dto.WarehouseInventoryDTO;
import com.kovanlabs.project.model.User;
import com.kovanlabs.project.model.WarehouseInventory;
import com.kovanlabs.project.service.UserService;
import com.kovanlabs.project.service.WarehouseInventoryService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse/inventory")
@CrossOrigin(origins = "http://localhost:5173")
public class WarehouseInventoryController {

    private final WarehouseInventoryService service;
    private final UserService userService;

    public WarehouseInventoryController(WarehouseInventoryService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    @GetMapping
    public java.util.List<WarehouseInventory> getAllIngredients(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        if (user == null || user.getBusiness() == null) {
            throw new RuntimeException("Invalid user context");
        }
        return service.getAllIngredientsByBusiness(user.getBusiness().getId());
    }

    @PostMapping
    public WarehouseInventory addOrUpdateIngredient(@RequestBody WarehouseInventoryDTO dto, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        if (user == null || user.getBusiness() == null) {
            throw new RuntimeException("Invalid user context");
        }

        // Force inventory writes to the authenticated owner's business warehouse.
        var businessWarehouse = service.ensureBusinessWarehouse(user.getBusiness().getId());
        dto.setWarehouseId(businessWarehouse.getId());
        return service.addOrUpdateIngredient(dto);
    }

    @GetMapping("/unique-names")
    public java.util.List<String> getUniqueIngredientNames(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        if (user == null || user.getBusiness() == null) {
            throw new RuntimeException("Invalid user context");
        }
        return service.getUniqueIngredientNamesByBusiness(user.getBusiness().getId());
    }

    @DeleteMapping("/{id}")
    public String deleteIngredient(@PathVariable Long id) {
        service.deleteIngredient(id);
        return "Deleted successfully";
    }
}