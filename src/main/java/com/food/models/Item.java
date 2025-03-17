package com.food.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
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
    @Size(
        min = 2,
        max = 30,
        message = "O nome deve ter no mínimo {min} caracteres"
    )
    private String name;

    @NotNull
    @Min(value = 20, message = "O valor mínimo deve ser {value} reais")
    private Double price;
}
