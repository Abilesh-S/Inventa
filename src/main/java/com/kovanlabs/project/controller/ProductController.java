package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.NewProductDTO;
import com.kovanlabs.project.model.Product;
import com.kovanlabs.project.model.Recipe;
import com.kovanlabs.project.service.ProductServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping
public class ProductController {
    @Autowired
    ProductServices productServices;
    @GetMapping("/products")
    public List<Map<String,Object>> productList(){
        return productServices.getAllProductList();
    }
    @PostMapping("/addProducts")
    public String addProducts(@RequestBody NewProductDTO productDescription){
        return productServices.addGivenProducts(productDescription);
    }
}
