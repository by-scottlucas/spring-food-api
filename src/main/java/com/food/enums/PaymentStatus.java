package com.food.enums;

public enum PaymentStatus {
    PENDING("Pagamento Pendente"),
    PAID("Pagamento Realizado"),
    FAILED("Falha no Pagamento"),
    CANCELED("Falha no Pagamento"),
    REFUNDED("Pagamento Reembolsado");

    private final String description;

    PaymentStatus(String destription) {
        this.description = destription;
    }

    public String getDescription() {
        return description;
    }
}
