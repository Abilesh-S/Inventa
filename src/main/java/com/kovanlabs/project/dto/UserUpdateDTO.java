package com.kovanlabs.project.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String name;
    private String email;
    private String phone;
    private String password;
    private String oldPassword;
}