package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.WarehouseDTO;
import com.kovanlabs.project.model.Business;
import com.kovanlabs.project.model.Warehouse;
import com.kovanlabs.project.repository.BusinessRepository;
import com.kovanlabs.project.repository.WarehouseRepository;
import org.springframework.stereotype.Service;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final BusinessRepository businessRepository;

    public WarehouseService(WarehouseRepository warehouseRepository,
                            BusinessRepository businessRepository) {
        this.warehouseRepository = warehouseRepository;
        this.businessRepository = businessRepository;
    }

    public Warehouse createWarehouse(WarehouseDTO dto) {

        Business business = businessRepository.findById(dto.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found"));

        if (warehouseRepository.findByBusinessId(dto.getBusinessId()).isPresent()) {
            throw new RuntimeException("Warehouse already exists for this business");
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setName(dto.getName());
        warehouse.setLocation(dto.getLocation());
        warehouse.setBusiness(business);

        return warehouseRepository.save(warehouse);
    }

    public java.util.List<Warehouse> getWarehouses() {
        return warehouseRepository.findAll();
    }

    public java.util.List<Warehouse> getWarehousesByBusiness(Long businessId) {
        return warehouseRepository.findByBusinessId(businessId)
                .map(java.util.List::of)
                .orElseGet(java.util.List::of);
    }

    public Warehouse getOrCreateWarehouseForBusiness(Long businessId) {
        return warehouseRepository.findByBusinessId(businessId).orElseGet(() -> {
            Business business = businessRepository.findById(businessId)
                    .orElseThrow(() -> new RuntimeException("Business not found"));
            Warehouse warehouse = new Warehouse();
            warehouse.setName((business.getName() == null || business.getName().isBlank())
                    ? "Main Warehouse"
                    : business.getName() + " Warehouse");
            warehouse.setLocation((business.getLocation() == null || business.getLocation().isBlank())
                    ? "Main Hub"
                    : business.getLocation());
            warehouse.setBusiness(business);
            return warehouseRepository.save(warehouse);
        });
    }
}