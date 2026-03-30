package com.kovanlabs.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NO_CONTENT)
public class LoginValidationIssues extends RuntimeException{
    public LoginValidationIssues(String msg){
        super(msg);
    }
}
