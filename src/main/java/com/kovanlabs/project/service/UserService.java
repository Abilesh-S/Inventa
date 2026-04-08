package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.LoginDTO;
import com.kovanlabs.project.dto.OwnerDTO;
import com.kovanlabs.project.dto.UserDTO;
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

    public UserService(UserRepository userRepository, BusinessRepository businessRepository,
                       BranchRepository branchRepository, PasswordEncoder passwordEncoder,
                       EmailVerificationRepository emailVerificationRepository) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.passwordEncoder = passwordEncoder;
        this.branchRepository = branchRepository;
        this.emailVerificationRepository = emailVerificationRepository;
    }

    public User registerOwner(OwnerDTO dto) {
        EmailVerification verification = emailVerificationRepository.findByEmailId(dto.getEmailId())
                .orElseThrow(() -> new RuntimeException("Please verify your email first."));

        if (!verification.isVerified()) {
            throw new RuntimeException("Invalid verification code.");
        }

//      Create a new Business
        Business details = dto.getBusinessDetails();
        Business business;
        business = businessRepository.findByNameAndOwnerName(details.getName() , details.getOwnerName())
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
        Optional<EmailVerification> emailVerification = emailVerificationRepository.findByEmailId(dto.getEmailId());
        emailVerificationRepository.delete(emailVerification.get());
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
    public User registerBranchUserByOwner(UserDTO dto, Role role, String ownerEmail) {
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
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(role);
        user.setBusiness(business);
        user.setBranch(branch);

        return userRepository.save(user);
    }

    public User createBranchManager(UserDTO dto) {
        Business business = businessRepository.findById(dto.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found for ID: " + dto.getBusinessId()));

        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found for ID: " + dto.getBranchId()));

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.MANAGER);
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
}