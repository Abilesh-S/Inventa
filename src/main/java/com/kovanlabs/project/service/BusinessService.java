package com.kovanlabs.project.service;

import com.kovanlabs.project.model.Business;
import com.kovanlabs.project.repository.BusinessRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Service
public class BusinessService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessService.class);
    private final BusinessRepository businessRepository;

    public BusinessService(BusinessRepository businessRepository) {

        this.businessRepository = businessRepository;
    }

    public Business saveBusiness(Business business) {
        logger.info("Saving new business(Main branch): {}", business.getOwnerName());
        return businessRepository.save(business);
    }

    public List<Business> getAllBusinesses() {
        logger.info("Fetching all businesses");
        return businessRepository.findAll();
    }

    public Business getBusinessById(Long id) {
        logger.info("Fetching Business with id: {}",id);
        System.out.println("CONTROLLER HIT");
        return businessRepository.findById(id).orElse(null);

    }

    public Business updateBusiness(Long id, Business updatedBusiness) {
        logger.info("Update business with id: {}",id);
        Business existing = businessRepository.findById(id).orElse(null);

        if (existing != null) {
            existing.setName(updatedBusiness.getName());
            existing.setOwnerName(updatedBusiness.getOwnerName());
            existing.setLocation(updatedBusiness.getLocation());
            logger.info("Business updated successfully: {}",id);
            return businessRepository.save(existing);
        }
        logger.error("Cannot update. Business not found with id: {}",id);
        return null;
    }

    public void deleteBusiness(Long id) {
        logger.info("Deleting business with id: {}",id);
        businessRepository.deleteById(id);
    }
}