package com.food.models.dtos;

import java.util.Date;
import java.util.List;

import com.food.enums.OrderStatus;
import com.food.enums.PaymentMethod;
import com.food.enums.PaymentStatus;
import com.food.models.Item;
import lombok.Data;

@Data
public class OrderDetailDTO {
    private Long id;
    private List<Item> items;
    private Date date;
    private Double totalValue;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private CustomerDTO customer;

    public OrderDetailDTO(Long id, List<Item> items, Date date, Double totalValue, OrderStatus status,
            PaymentMethod paymentMethod, PaymentStatus paymentStatus, CustomerDTO customer) {
        this.id = id;
        this.items = items;
        this.date = date;
        this.totalValue = totalValue;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.customer = customer;
    }
}
