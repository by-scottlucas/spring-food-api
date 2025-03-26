package com.food.enums;

public enum OrderStatus {
    PENDING("Pendente"),
    PROCESSING("Em processamento"),
    COMPLETED("Concluído"),
    CANCELLED("Cancelado");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
