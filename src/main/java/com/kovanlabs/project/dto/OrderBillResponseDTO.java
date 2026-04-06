package com.kovanlabs.project.dto;

import com.kovanlabs.project.model.Bill;
import com.kovanlabs.project.model.Order;
import lombok.Data;

@Data
public class OrderBillResponseDTO {
    private Order order;
    private Bill bill;

    public OrderBillResponseDTO() {
    }

    public OrderBillResponseDTO(Order order, Bill bill) {
        this.order = order;
        this.bill = bill;
    }

}
