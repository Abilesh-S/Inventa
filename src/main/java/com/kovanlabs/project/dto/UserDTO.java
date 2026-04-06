package com.kovanlabs.project.dto;

import lombok.Data;

@Data

public class UserDTO {

    private String name;
    private String email;
    private String password;
    private String phone;
    private Long businessId;

    private Long branchId;


}