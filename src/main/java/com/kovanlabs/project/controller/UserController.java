package com.kovanlabs.project.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/users")
    public List<String> getUsers() {
        return List.of("Abi", "John", "Sam");
    }
}
