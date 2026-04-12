package com.kovanlabs.project.dto;

import com.kovanlabs.project.model.Branch;
import com.kovanlabs.project.model.Business;
import lombok.Data;

@Data
public class OwnerDTO {
    private Business businessDetails;
    private String password ;
    private String emailId ;
    private String emailOtp;
    private String phoneNo;
    private Branch branch;
}