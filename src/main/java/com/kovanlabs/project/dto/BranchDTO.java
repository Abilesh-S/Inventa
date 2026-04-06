package com.kovanlabs.project.dto;

import lombok.Data;

@Data
public class BranchDTO {
    private String name;
    private String location;
    private Long businessId;
    private Long managerId;
    private java.util.List<Long> staffIds;
}