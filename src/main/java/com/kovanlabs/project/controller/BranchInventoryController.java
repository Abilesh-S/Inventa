package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.BranchInventoryDTO;
import com.kovanlabs.project.model.BranchInventory;
import com.kovanlabs.project.service.BranchInventoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branch-inventory")
public class BranchInventoryController {

    private final BranchInventoryService branchInventoryService;

    public BranchInventoryController(BranchInventoryService branchInventoryService) {
        this.branchInventoryService = branchInventoryService;
    }

    @PostMapping
    public BranchInventory addOrUpdate(@RequestBody BranchInventoryDTO dto) {
        return branchInventoryService.addOrUpdateIngredient(dto);
    }

    @GetMapping("/branch/{branchId}")
    public List<BranchInventory> getAll(@PathVariable("branchId") Long branchId) {
        return branchInventoryService.getAllIngredients(branchId);
    }

    @GetMapping("/branch/{branchId}/ingredient/{name}")
    public BranchInventory getOne(@PathVariable Long branchId, @PathVariable String name) {
        return branchInventoryService.getIngredient(branchId, name);
    }

    @DeleteMapping("/branch/{branchId}/ingredient/{name}")
    public String delete(@PathVariable Long branchId, @PathVariable String name) {
        return branchInventoryService.deleteIngredient(branchId, name);
    }
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        branchInventoryService.deleteById(id);
    }
}