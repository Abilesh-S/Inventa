package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.LoginDTO;
import com.kovanlabs.project.dto.OwnerDTO;
import com.kovanlabs.project.dto.UserDTO;
import com.kovanlabs.project.dto.UserUpdateDTO;
import com.kovanlabs.project.model.*;
import com.kovanlabs.project.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;
    private final BranchRepository branchRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, BusinessRepository businessRepository,
            BranchRepository branchRepository, PasswordEncoder passwordEncoder,
            EmailVerificationRepository emailVerificationRepository , EmailService emailService) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.passwordEncoder = passwordEncoder;
        this.branchRepository = branchRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.emailService = emailService;
    }

    public User registerOwner(OwnerDTO dto) {
        EmailVerification verification = emailVerificationRepository.findByEmailId(dto.getEmailId())
                .orElseThrow(() -> new RuntimeException("Please verify your email first."));

        if (!verification.isVerified()) {
            throw new RuntimeException("Invalid verification code.");
        }

        // Create a new Business
        Business details = dto.getBusinessDetails();
        Business business;
        business = businessRepository.findByNameAndOwnerName(details.getName(), details.getOwnerName())
                .orElseGet(() -> {
                    Business newBusiness = new Business();
                    newBusiness.setOwnerName(details.getOwnerName());
                    newBusiness.setName(details.getName());
                    newBusiness.setLocation(details.getLocation());
                    newBusiness.setCreatedAt(details.getCreatedAt());
                    return businessRepository.save(newBusiness);
                });

        User user = new User();
        user.setName(dto.getBusinessDetails().getOwnerName());
        user.setEmail(dto.getEmailId());
        user.setPhone(dto.getPhoneNo());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.OWNER);
        user.setBusiness(business);
        user.setBranch(dto.getBranch());
        
        emailVerificationRepository.delete(verification);
        return userRepository.save(user);
    }

    public User login(LoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (user.getRole() != Role.OWNER) {
            throw new RuntimeException("Not an owner");
        }

        return user;
    }

    public User loginAnyRole(LoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return user;
    }

    @Transactional
    public User registerBranchUsersByOwner(UserDTO dto, Role role, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner user not found"));

        if (owner.getRole() != Role.OWNER) {
            throw new RuntimeException("Only owner can create manager/staff");
        }

        // Default to owner's business if not provided
        Long businessId = dto.getBusinessId();
        if (businessId == null && owner.getBusiness() != null) {
            businessId = owner.getBusiness().getId();
        }

        if (businessId == null) {
            throw new RuntimeException("businessId is required");
        }

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        if (owner.getBusiness() == null || !owner.getBusiness().getId().equals(business.getId())) {
            throw new RuntimeException("Owner can only create users for their own business");
        }

        Branch branch = null;
        if (dto.getBranchId() != null) {
            branch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() -> new RuntimeException("Branch not found"));

            if (branch.getBusiness() == null || !branch.getBusiness().getId().equals(business.getId())) {
                throw new RuntimeException("Branch does not belong to this business");
            }
        }

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(emailService.oneTimePasswordforAccountCreation(dto.getEmail())));
        user.setBusiness(business);
        user.setBranch(branch);

        return userRepository.save(user);
    }

    @Transactional
    public User registerStaffByManager(UserDTO dto, String managerEmail) {

        // 1. Fetch Manager
        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        // 2. Role Check
        if (manager.getRole() != Role.MANAGER) {
            throw new RuntimeException("Only managers can add staff");
        }

        // 3. Validate Manager Assignment
        Business business = manager.getBusiness();
        Branch managerBranch = manager.getBranch();

        if (business == null || managerBranch == null) {
            throw new RuntimeException("Manager must belong to a business and branch");
        }

        // 4. Validate Branch
        if (dto.getBranchId() == null) {
            throw new RuntimeException("Branch ID is required");
        }

        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Invalid branch"));

        // 5. Check Branch belongs to manager's business
        if (!branch.getBusiness().getId().equals(business.getId())) {
            throw new RuntimeException("Branch does not belong to manager's business");
        }

        // 6. Check Manager can only add staff to their own branch
        if (!branch.getId().equals(managerBranch.getId())) {
            throw new RuntimeException("Manager can only add staff to their own branch");
        }

        // 7. Email uniqueness check
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // 8. Create Staff
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(Role.STAFF);
        user.setPassword(passwordEncoder.encode(emailService.oneTimePasswordforAccountCreation(dto.getEmail())));
        user.setBusiness(business);
        user.setBranch(branch);

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public List<User> getAllUsers(Long businessId) {
        return userRepository.findByBusinessId(businessId);
    }
    
    public List<User> getUsersByBranch(Long branchId) {
        return userRepository.findByBranchId(branchId);
    }

    @Transactional
    public void updateExistingUserDetails(UserUpdateDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            if (passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
            } else {
                throw new RuntimeException("Current password is not valid");
            }
        }
        userRepository.save(user);
    }
}