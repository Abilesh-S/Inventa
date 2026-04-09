package com.kovanlabs.project.dto;

import com.kovanlabs.project.model.Branch;
import com.kovanlabs.project.model.Business;
import lombok.Data;

@Data

public class UserDTO {
    private String name;
    private String email;
    private String phone;
    private Long businessId;
    private Long branchId;
}