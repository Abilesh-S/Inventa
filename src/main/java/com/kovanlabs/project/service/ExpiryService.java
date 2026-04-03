package com.kovanlabs.project.service;

import com.kovanlabs.project.model.BranchInventory;
import com.kovanlabs.project.repository.BranchInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExpiryService {

    @Autowired
    private BranchInventoryRepository repo;

    public void markExpired() {
        LocalDate today = LocalDate.now();

        List<BranchInventory> list = repo.findAll();

        for (BranchInventory b : list) {
            if (b.getExpiryDate().isBefore(today)
                    && "ACTIVE".equals(b.getStatus())) {

                b.setStatus("EXPIRED");
                repo.save(b);
            }
        }
    }
}