package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.BranchDTO;
import com.kovanlabs.project.model.Branch;
import com.kovanlabs.project.service.BranchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @PostMapping
    public Branch create(@RequestBody BranchDTO dto) {
        return branchService.createBranch(dto);
    }

    @GetMapping("/business/{businessId}")
    public List<Branch> getAll(@PathVariable Long businessId) {
        return branchService.getAllBranches(businessId);
    }

    @GetMapping("/{id}")
    public Branch get(@PathVariable Long id) {
        return branchService.getBranch(id);
    }

    @PutMapping("/{id}")
    public Branch update(@PathVariable Long id, @RequestBody BranchDTO dto) {
        return branchService.updateBranch(id, dto);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        return branchService.deleteBranch(id);
    }
}