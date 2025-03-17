package com.food.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 2, max = 30, message = "O nome deve ter no mínimo {min} caracteres")
    private String name;

    @NotNull
    @DecimalMin(value = "20.00", message = "O valor mínimo deve ser {value} reais")
    private Double price;
}
