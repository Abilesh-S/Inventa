package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.NewProductDTO;
import com.kovanlabs.project.model.Product;
import com.kovanlabs.project.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ProductServices {
    @Autowired
    ProductRepository productRepository;
    public List<Map<String, Object>> getAllProductList(){
        // Fetch raw entities
        List<Product> products = productRepository.findAll();

        // Convert to plain Maps so Jackson NEVER crashes
        return products.stream().map(product -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", product.getId());
            map.put("productName", product.getProductName());
            map.put("description", product.getDescription());
            map.put("price", product.getPrice());
            map.put("category", product.getCategory());
            map.put("active", product.isActive());
            if (product.getCreatedDate() != null) {
                map.put("createdDate", product.getCreatedDate().toString()); // safe string!
            }
            return map;
        }).collect(Collectors.toList());
    }
    public String addGivenProducts(NewProductDTO productDetails){
        if(Pattern.matches("^[A-Za-z ]+$",productDetails.getProduct().getProductName())){
            if(Pattern.matches("^[0-9.]+$" , Float.toString(productDetails.getProduct().getPrice()))){
                productRepository.save(productDetails.getProduct());
            }
        }
        return "Not a Valid Product";
    }
}