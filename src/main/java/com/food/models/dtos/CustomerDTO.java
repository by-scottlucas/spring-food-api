package com.food.models.dtos;

import lombok.Data;

@Data
public class CustomerDTO {
    private Long id;
    private String name;
    private String address;

    public CustomerDTO(Long id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }
}
