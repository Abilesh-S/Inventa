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
}