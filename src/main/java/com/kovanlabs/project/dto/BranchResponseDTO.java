package com.kovanlabs.project.dto;

import lombok.Data;

@Data
public class BranchResponseDTO {
    private Long id;
    private String name;
    private String location;
    private String managerName;
    private Double totalInventory;
    private String status;
}
