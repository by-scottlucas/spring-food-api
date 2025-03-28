package com.food.enums;

public enum PaymentMethod {
    CREDIT_CARD("Cartão de Crédito"),  
    DEBIT_CARD("Cartão de Débito"),   
    CASH("Dinheiro"), 
    PIX("PIX");

    private final String description;

    PaymentMethod(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}
