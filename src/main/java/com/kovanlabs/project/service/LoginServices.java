package com.kovanlabs.project.service;


import com.kovanlabs.project.model.LoginCredentials;
import com.kovanlabs.project.repository.LoginCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class LoginServices {

    @Autowired
    LoginCredentialRepository loginRepository;

    public String loginValidation(LoginCredentials loginDetails){

        Optional<LoginCredentials> loginCredentials = loginRepository.findByUserName(loginDetails.getUserName());

        if (!loginCredentials.isPresent()) {
            return "User not found";
        }

        LoginCredentials dbUser = loginCredentials.orElse(null);

        if (dbUser != null)  {
            if (loginDetails.getPassword().equals(dbUser.getPassword())) {
                return "Valid User";
            } else {
                return "Invalid Password";
            }
        }
        else{
            return "User not found";
        }
    }
}