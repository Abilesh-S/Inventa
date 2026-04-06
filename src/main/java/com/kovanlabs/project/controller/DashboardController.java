package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.DashboardDTO;
import com.kovanlabs.project.model.User;
import com.kovanlabs.project.repository.UserRepository;
import com.kovanlabs.project.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@CrossOrigin("http://localhost:5173")
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    public DashboardController(DashboardService dashboardService, UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    @GetMapping("/stats")
    public Object getStats(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return Map.of("error", "Not Authenticated");
            }
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found: " + authentication.getName()));

            return dashboardService.getStats(user);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            errorMap.put("type", e.getClass().getSimpleName());
            return errorMap;
        }
    }
}
