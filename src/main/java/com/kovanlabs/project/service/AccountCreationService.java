package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.AccountCreationDTO;
import com.kovanlabs.project.model.Customer;
import com.kovanlabs.project.model.LoginCredentials;
import com.kovanlabs.project.repository.CustomerDetailsRepository;
import com.kovanlabs.project.repository.LoginCredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountCreationService {

    private static final Logger LOG = LoggerFactory.getLogger(AccountCreationService.class);
    @Autowired
    CustomerDetailsRepository customerRepository;
    @Autowired
    LoginCredentialRepository loginRepository;
    public String createAccount(AccountCreationDTO signUpDetails){
        LOG.info("Entered into Create Account");
        Optional<Customer> userExist = customerRepository.findByEmailId(signUpDetails.getUserName());
        if(userExist.isPresent()){
            return "User Already Exist";
        }
        LoginCredentials login = new LoginCredentials(signUpDetails.getUserName() , signUpDetails.getPassword());
        LoginCredentials loginKey =  loginRepository.save(login);
        LOG.info("Created LoginCredentials For the User");
        long loginKeyId = loginKey.getId();

        Customer customer = new Customer(signUpDetails.getEmailId() , signUpDetails.getAddress() , signUpDetails.getPhoneNumber());

        customer.setLoginCredentials(loginKey);
        customerRepository.save(customer);
        LOG.info("Created User Account Successfully");


        return "Account Created Successfully";
    }

}