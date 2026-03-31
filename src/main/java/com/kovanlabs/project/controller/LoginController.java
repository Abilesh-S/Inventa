package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.AccountCreationDTO;
import com.kovanlabs.project.model.LoginCredentials;
import com.kovanlabs.project.repository.CustomerDetailsRepository;
import com.kovanlabs.project.service.AccountCreationService;
import com.kovanlabs.project.service.LoginServices;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping
public class LoginController {
    private static final Log LOG = LogFactory.getLog(LoginController.class);
    @Autowired
    LoginServices loginService;
    @Autowired
    AccountCreationService accountCreation;
    @PostMapping("/login")
    public String validateUser(@RequestBody LoginCredentials loginDetails){
        return loginService.loginValidation(loginDetails);
    }

    @PostMapping("/signup")
    public String createUser(@RequestBody AccountCreationDTO requestUser){
        LOG.info("Entered Successfully");
        return accountCreation.createAccount(requestUser);

    }

}
