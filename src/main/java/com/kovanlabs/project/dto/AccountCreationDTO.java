package com.kovanlabs.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreationDTO {
    public String userName ;
    public String password ;
    public String emailId ;
    public String phoneNumber;
    public String address ;
}
