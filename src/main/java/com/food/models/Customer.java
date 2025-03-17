package com.food.models;

import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(
        min = 2,
        max = 80,
        message = "O nome deve ter no mínimo {min} caracteres"
    )
    private String name;

    @NotNull
    @Size(
        min = 2,
        max = 300,
        message = "O endereço deve ter no mínimo {min} caracteres"
    )
    private String address;

    @OneToMany(
        mappedBy = "customer",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Order> orders;
}
