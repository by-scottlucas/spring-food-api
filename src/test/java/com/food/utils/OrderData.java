package com.food.utils;

import java.util.Date;
import java.util.List;

import com.food.enums.OrderStatus;
import com.food.enums.PaymentMethod;
import com.food.enums.PaymentStatus;

public class OrderData {
    public static final Long ID = 1L;
    public static final Date DATE = new Date();
    public static final Double TOTAL_VALUE = 25.00;
    public static final OrderStatus STATUS = OrderStatus.PENDING;
    public static final PaymentMethod PAYMENT_METHOD = PaymentMethod.CREDIT_CARD;
    public static final PaymentStatus PAYMENT_STATUS = PaymentStatus.PENDING;
    public static final List<Long> ITEM_IDS = List.of(ItemData.ID);
}
