package com.kovanlabs.project.controller;
import com.kovanlabs.project.dto.LoginDTO;
import com.kovanlabs.project.model.Role;
import com.kovanlabs.project.model.User;
import com.kovanlabs.project.dto.UserDTO;
import com.kovanlabs.project.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register-owner")
    public Object registerOwner(@RequestBody UserDTO dto) {
        return userService.registerOwner(dto, Role.OWNER);
    }
    @PostMapping("/login-owner")

    public User loginOwner(@RequestBody LoginDTO dto) {
        return userService.login(dto);
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginDTO dto) {
        return userService.loginAnyRole(dto);
    }



    @PostMapping("/create-manager")
    public User createManager(@RequestBody UserDTO dto, Authentication authentication) {
        return userService.registerBranchUserByOwner(dto, Role.MANAGER, authentication.getName());
    }

    @PostMapping("/create-staff")
    public User createStaff(@RequestBody UserDTO dto, Authentication authentication) {
        return userService.registerBranchUserByOwner(dto, Role.STAFF, authentication.getName());
    }

}