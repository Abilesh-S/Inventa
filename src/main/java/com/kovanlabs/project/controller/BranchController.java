package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.BranchDTO;
import com.kovanlabs.project.model.Branch;
import com.kovanlabs.project.model.User;
import com.kovanlabs.project.repository.UserRepository;
import com.kovanlabs.project.service.BranchService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService branchService;
    private final UserRepository userRepository;

    public BranchController(BranchService branchService, UserRepository userRepository) {
        this.branchService = branchService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public Branch create(@RequestBody BranchDTO dto) {
        return branchService.createBranch(dto);
    }

    @CrossOrigin("http://localhost:5173")
    @GetMapping("/business/{businessId}")
    public List<com.kovanlabs.project.dto.BranchResponseDTO> getAll(@PathVariable Long businessId) {
        return branchService.getAllBranches(businessId);
    }

    @GetMapping("/my")
    public List<com.kovanlabs.project.dto.BranchResponseDTO> getMyBusinessBranches(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Not authenticated");
        }
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getBusiness() == null) {
            throw new RuntimeException("Business not found for user");
        }
        return branchService.getAllBranches(user.getBusiness().getId());
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