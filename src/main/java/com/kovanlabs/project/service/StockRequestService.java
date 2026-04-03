package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.StockRequestCreateDTO;
import com.kovanlabs.project.model.*;
import com.kovanlabs.project.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockRequestService {

    private static final Logger logger = LoggerFactory.getLogger(StockRequestService.class);
    private final StockRequestRepository stockRequestRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseInventoryRepository warehouseInventoryRepository;
    private final BranchInventoryRepository branchInventoryRepository;
    private final LowStockAlertService lowStockAlertService;

    public StockRequestService(
            StockRequestRepository stockRequestRepository,
            UserRepository userRepository,
            BranchRepository branchRepository,
            WarehouseRepository warehouseRepository,
            WarehouseInventoryRepository warehouseInventoryRepository,
            BranchInventoryRepository branchInventoryRepository,
            LowStockAlertService lowStockAlertService
    ) {
        this.stockRequestRepository = stockRequestRepository;
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.warehouseRepository = warehouseRepository;
        this.warehouseInventoryRepository = warehouseInventoryRepository;
        this.branchInventoryRepository = branchInventoryRepository;
        this.lowStockAlertService = lowStockAlertService;
    }

    public StockRequest createRequest(StockRequestCreateDTO dto, String requesterEmail) {
        logger.info("Creating stock request: requester={}, branchId={}, ingredient={}, quantity={}",
                requesterEmail, dto.getBranchId(), dto.getIngredientName(), dto.getQuantity());
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> {
                    logger.error("User not found: email={}", requesterEmail);
                    return new RuntimeException("User not found");
                });

        if (requester.getRole() != Role.MANAGER) {
            logger.warn("Unauthorized request creation attempt by user={}", requesterEmail);
            throw new RuntimeException("Only manager can create stock request");
        }

        if (dto.getBranchId() == null || dto.getIngredientName() == null || dto.getQuantity() == null || dto.getQuantity() <= 0) {
            logger.warn("Invalid stock request input: {}", dto);
            throw new RuntimeException("branchId, ingredientName and positive quantity are required");
        }

        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> {
                    logger.error("Branch not found: branchId={}", dto.getBranchId());
                    return new RuntimeException("Branch not found");
                });

        if (requester.getBusiness() == null || branch.getBusiness() == null
                || !requester.getBusiness().getId().equals(branch.getBusiness().getId())) {
            logger.warn("Cross-business request attempt: user={}, branchId={}", requesterEmail, dto.getBranchId());
            throw new RuntimeException("Manager can request stock only for own business branch");
        }

        if (requester.getBranch() == null || !requester.getBranch().getId().equals(branch.getId())) {
            logger.warn("Unauthorized branch access: user={}, branchId={}", requesterEmail, dto.getBranchId());
            throw new RuntimeException("Manager can request stock only for assigned branch");
        }

        String normalizedIngredient = dto.getIngredientName().trim().toLowerCase();
        boolean duplicatePending = stockRequestRepository
                .existsByBranchIdAndIngredientNameIgnoreCaseAndStatus(
                        branch.getId(),
                        normalizedIngredient,
                        StockRequestStatus.PENDING
                );
        if (duplicatePending) {
            logger.warn("Duplicate pending request: branchId={}, ingredient={}",
                    branch.getId(), normalizedIngredient);
            throw new RuntimeException("A pending request already exists for this ingredient in this branch");
        }

        StockRequest request = new StockRequest();
        request.setBusiness(branch.getBusiness());
        request.setBranch(branch);
        request.setRequestedBy(requester);
        request.setIngredientName(normalizedIngredient);
        request.setQuantity(dto.getQuantity());
        request.setUnit(dto.getUnit());
        request.setStatus(StockRequestStatus.PENDING);
        request=stockRequestRepository.save(request);
        logger.info("Stock request created: requestId={}, branchId={}, ingredient={}",
                request.getId(), branch.getId(), normalizedIngredient);

        return request;
    }

    public List<StockRequest> getPendingForOwner(String ownerEmail) {
        logger.info("Fetching pending stock requests for owner={}", ownerEmail);
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        if (owner.getRole() != Role.OWNER || owner.getBusiness() == null) {
            throw new RuntimeException("Only owner can view pending requests");
        }

        return stockRequestRepository.findByBusinessIdAndStatusOrderByIdDesc(
                owner.getBusiness().getId(),
                StockRequestStatus.PENDING
        );
    }

    @Transactional
    public StockRequest approve(Long requestId, String ownerEmail, String remark) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        if (owner.getRole() != Role.OWNER || owner.getBusiness() == null) {
            throw new RuntimeException("Only owner can approve requests");
        }

        StockRequest request = stockRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Stock request not found"));

        if (!request.getBusiness().getId().equals(owner.getBusiness().getId())) {
            throw new RuntimeException("Cannot approve request from another business");
        }

        if (request.getStatus() != StockRequestStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be approved");
        }

        Warehouse warehouse = warehouseRepository.findByBusinessId(owner.getBusiness().getId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found for business"));

        WarehouseInventory warehouseInventory = warehouseInventoryRepository
                .findByIngredientNameIgnoreCaseAndWarehouseId(request.getIngredientName(), warehouse.getId())
                .orElseThrow(() -> new RuntimeException("Ingredient not found in warehouse"));

        if (warehouseInventory.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Not enough warehouse stock");
        }

        warehouseInventory.setQuantity(warehouseInventory.getQuantity() - request.getQuantity());
        warehouseInventoryRepository.save(warehouseInventory);

        BranchInventory branchInventory = branchInventoryRepository
                .findByBranchIdAndIngredientNameIgnoreCase(request.getBranch().getId(), request.getIngredientName())
                .orElseGet(() -> {
                    BranchInventory newItem = new BranchInventory();
                    newItem.setBranch(request.getBranch());
                    newItem.setIngredientName(request.getIngredientName());
                    newItem.setQuantity(0.0);
                    newItem.setThreshold(0.0);
                    newItem.setUnit(request.getUnit());
                    return newItem;
                });

        branchInventory.setQuantity(branchInventory.getQuantity() + request.getQuantity());
        if (branchInventory.getUnit() == null || branchInventory.getUnit().trim().isEmpty()) {
            branchInventory.setUnit(request.getUnit());
        }
        branchInventoryRepository.save(branchInventory);
        lowStockAlertService.closeIfRecovered(branchInventory);

        request.setStatus(StockRequestStatus.APPROVED);
        request.setApprovedBy(owner);
        request.setOwnerRemark(remark);
        return stockRequestRepository.save(request);
    }

    public StockRequest reject(Long requestId, String ownerEmail, String remark) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        if (owner.getRole() != Role.OWNER || owner.getBusiness() == null) {
            throw new RuntimeException("Only owner can reject requests");
        }

        StockRequest request = stockRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Stock request not found"));

        if (!request.getBusiness().getId().equals(owner.getBusiness().getId())) {
            throw new RuntimeException("Cannot reject request from another business");
        }

        if (request.getStatus() != StockRequestStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be rejected");
        }

        request.setStatus(StockRequestStatus.REJECTED);
        request.setApprovedBy(owner);
        request.setOwnerRemark(remark);
        return stockRequestRepository.save(request);
    }
}
