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

    public BranchService(BranchRepository branchRepository, BusinessRepository businessRepository) {
        this.branchRepository = branchRepository;
        this.businessRepository = businessRepository;
    }

    public Branch createBranch(BranchDTO dto) {
        Business business = businessRepository.findById(dto.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found"));

        Branch branch = new Branch();
        branch.setName(dto.getName());
        branch.setLocation(dto.getLocation());
        branch.setBusiness(business);
        logger.info("Creating branch");
        return branchRepository.save(branch);
    }

    public List<Branch> getAllBranches(Long businessId) {
        logger.info("Getting all branches from the business_id: {}",businessId);
        return branchRepository.findByBusinessId(businessId);
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
        logger.info("Update branch with id {}",id);
        return branchRepository.save(branch);
    }

    public String deleteBranch(Long id) {
        Branch branch = getBranch(id);
        logger.info("Deleting branch with id {}",id);
        branchRepository.delete(branch);
        return "Branch deleted successfully";
    }
}