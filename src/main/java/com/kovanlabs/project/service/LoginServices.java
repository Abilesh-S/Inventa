package com.kovanlabs.project.service;


import com.kovanlabs.project.model.LoginCredentials;
import com.kovanlabs.project.repository.LoginCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class LoginServices {

    @Autowired
    LoginCredentialRepository loginRepository;
    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    public String loginValidation(LoginCredentials loginDetails){

        Optional<LoginCredentials> loginCredentials = loginRepository.findByUserName(loginDetails.getUserName());

        if (!loginCredentials.isPresent()) {
            return "User not found";
        }

        LoginCredentials dbUser = loginCredentials.get();

        if (dbUser != null)  {
            if (passwordEncoder.matches(loginDetails.getPassword(),dbUser.getPassword())) {
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