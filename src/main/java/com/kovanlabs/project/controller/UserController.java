package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.*;
import com.kovanlabs.project.model.Role;
import com.kovanlabs.project.model.User;
import com.kovanlabs.project.service.EmailService;
import com.kovanlabs.project.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("http://localhost:5173")
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    public UserController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @GetMapping()
    public List<User> getAllUsers(org.springframework.security.core.Authentication auth, @RequestParam(required = false) Long branchId) {
        User currentUser = userService.findByEmail(auth.getName());
        
        if (currentUser.getRole() == Role.OWNER) {
            if (branchId != null) {
                return userService.getUsersByBranch(branchId);
            }
            return userService.getAllUsers(currentUser.getBusiness().getId());
        } else if (currentUser.getRole() == Role.MANAGER) {
            if (currentUser.getBranch() != null) {
                return userService.getUsersByBranch(currentUser.getBranch().getId());
            }
            return List.of(); // Manager without a branch shouldn't see anyone or just themselves?
        } else {
            // Staff or others? Maybe just return empty or their own data
            return List.of(currentUser);
        }
    }

    @PostMapping("/register-owner")
    public Object registerOwner(@RequestBody OwnerDTO dto) {
        return userService.registerOwner(dto);
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
        return userService.registerBranchUsersByOwner(dto, Role.MANAGER, authentication.getName());
    }

    @PostMapping("/create-staff-byOwner")
    public User createStaff(@RequestBody UserDTO dto, Authentication authentication) {
        return userService.registerBranchUsersByOwner(dto, Role.STAFF, authentication.getName());
    }

    @PostMapping("/verify-email")
    public String emailVerification(@RequestBody OwnerDTO dto) {
        emailService.sendVerificationEmail(dto.getEmailId());
        return "Verification email sent successfully";
    }

    @PostMapping("/validate-otp")
    public void validateOtp(@RequestBody OwnerDTO dto) {
        emailService.validateVerificationEmail(dto.getEmailId(), dto.getEmailOtp());
    }

    @GetMapping("/me")
    public User getCurrentUser(Authentication auth) {
        return userService.findByEmail(auth.getName());
    }

    @PutMapping("/update-profile")
    public void updateUserDetails(@RequestBody UserUpdateDTO dto, Authentication auth){
        dto.setEmail(auth.getName());
        userService.updateExistingUserDetails(dto);
    }

    @PostMapping("/create-staff-byManager")
    public User staffCreationByManager(@RequestBody UserDTO dto, Authentication authentication ){
        return userService.registerStaffByManager(dto , authentication.getName());
    }
}