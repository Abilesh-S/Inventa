package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.BranchDTO;
import com.kovanlabs.project.model.*;
import com.kovanlabs.project.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchService {
    private static final Logger logger = LoggerFactory.getLogger(BranchService.class);
    private final BranchRepository branchRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final BranchInventoryRepository inventoryRepository;

    public BranchService(BranchRepository branchRepository,
                         BusinessRepository businessRepository,
                         UserRepository userRepository,
                         BranchInventoryRepository inventoryRepository) {
        this.branchRepository = branchRepository;
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public Branch createBranch(BranchDTO dto) {
        Business business = businessRepository.findById(dto.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found"));

        Branch branch = new Branch();
        branch.setName(dto.getName());
        branch.setLocation(dto.getLocation());
        branch.setBusiness(business);

        Branch savedBranch = branchRepository.save(branch);

        if (dto.getManagerId() != null) {
            User manager = userRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            manager.setBranch(savedBranch);
            userRepository.save(manager);
        }

        if (dto.getStaffIds() != null && !dto.getStaffIds().isEmpty()) {
            List<User> staff = userRepository.findAllById(dto.getStaffIds());
            for (User s : staff) {
                s.setBranch(savedBranch);
            }
            userRepository.saveAll(staff);
        }

        logger.info("Creating branch with manager and staff");
        return savedBranch;
    }

    public List<com.kovanlabs.project.dto.BranchResponseDTO> getAllBranches(Long businessId) {
        logger.info("Getting all branches from the business_id: {}", businessId);
        List<Branch> branches = branchRepository.findByBusinessId(businessId);

        return branches.stream().map(branch -> {
            com.kovanlabs.project.dto.BranchResponseDTO response = new com.kovanlabs.project.dto.BranchResponseDTO();
            response.setId(branch.getId());
            response.setName(branch.getName());
            response.setLocation(branch.getLocation());

            List<User> managers = userRepository.findByBranchIdAndRole(branch.getId(), Role.MANAGER);
            if (!managers.isEmpty()) {
                response.setManagerName(managers.get(0).getName());
            } else {
                response.setManagerName("Not Assigned");
            }

            List<BranchInventory> inventory = inventoryRepository.findByBranchId(branch.getId());
            double totalItems = inventory.stream().mapToDouble(BranchInventory::getQuantity).sum();
            response.setTotalInventory(totalItems);
            response.setStatus("Operational");

            return response;
        }).toList();
    }

    public Branch getBranch(Long id) {
        logger.info("Getting branch from id {}",id);
        return branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
    }

    public Branch updateBranch(Long id, BranchDTO dto) {
        Branch branch = getBranch(id);
        branch.setName(dto.getName());
        branch.setLocation(dto.getLocation());

        List<User> currentUsers = userRepository.findByBranchIdAndRole(id, Role.MANAGER);
        currentUsers.addAll(userRepository.findByBranchIdAndRole(id, Role.STAFF));
        for (User u : currentUsers) {
            u.setBranch(null);
        }
        userRepository.saveAll(currentUsers);

        if (dto.getManagerId() != null) {
            User manager = userRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            manager.setBranch(branch);
            userRepository.save(manager);
        }

        if (dto.getStaffIds() != null && !dto.getStaffIds().isEmpty()) {
            List<User> staff = userRepository.findAllById(dto.getStaffIds());
            for (User s : staff) {
                s.setBranch(branch);
            }
            userRepository.saveAll(staff);
        }

        logger.info("Update branch with id {}", id);
        return branchRepository.save(branch);
    }

    public String deleteBranch(Long id) {
        Branch branch = getBranch(id);
        logger.info("Deleting branch with id {}",id);
        branchRepository.delete(branch);
        return "Branch deleted successfully";
    }
}