package com.kovanlabs.project.controller;

import com.kovanlabs.project.model.Business;
import com.kovanlabs.project.service.BusinessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private static final Logger logger = LoggerFactory.getLogger(BusinessController.class);

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {

        this.businessService = businessService;
    }

    @PostMapping
    public Business createBusiness(@RequestBody Business business) {
        logger.info("API call: Create Business");
        return businessService.saveBusiness(business);
    }

    @GetMapping
    public List<Business> getAllBusinesses() {
        logger.info("API call: Get all business");
        return businessService.getAllBusinesses();
    }


    @GetMapping("/{id}")
    public Business getBusiness(@PathVariable Long id) {
        logger.info("API call: Get Business with id {}",id);
        return businessService.getBusinessById(id);
    }

    @PutMapping("/{id}")
    public Business updateBusiness(@PathVariable Long id, @RequestBody Business business) {
        logger.info("API call: Update business with id {}",id);
        return businessService.updateBusiness(id, business);
    }

    @DeleteMapping("/{id}")
    public String deleteBusiness(@PathVariable Long id) {
        logger.info("API call: Delete business with id {}",id);
        businessService.deleteBusiness(id);
        return "Business deleted successfully";
    }
}