package com.food.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.food.enums.OrderStatus;
import com.food.enums.PaymentStatus;
import com.food.exceptions.NotFoundException;
import com.food.models.Item;
import com.food.models.Order;
import com.food.models.dtos.CustomerDTO;
import com.food.models.dtos.OrderDetailDTO;
import com.food.repositories.ItemRepository;
import com.food.repositories.OrderRepository;

@Service
public class CheckoutService {
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    public CheckoutService(OrderRepository orderRepository, ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public Order processOrder(Order data) {
        if (data.getItems() == null || data.getItems().isEmpty()) {
            throw new IllegalArgumentException("O pedido deve conter pelo menos um item.");
        }

        List<Item> items = new ArrayList<>();

        for (Item item : data.getItems()) {
            Item foundItem = itemRepository
                    .findById(item.getId())
                    .orElseThrow(() -> new NotFoundException(
                            "Item com ID " + item.getId() + " não encontrado"));

            foundItem.setQuantity(item.getQuantity());
            items.add(foundItem);
        }

        data.setItems(items);
        data.setDate(new Date());
        data.setStatus(OrderStatus.PROCESSING);

        if (data.getPaymentMethod() == null) {
            throw new IllegalArgumentException("O método de pagamento é obrigatório");
        }

        data.setPaymentStatus(PaymentStatus.PENDING);
        recalculateTotal(data);
        return orderRepository.save(data);
    }

    public OrderDetailDTO getOrderStatus(Long id) {
        Order order = orderRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Pedido com ID " + id + " não encontrado"));

        CustomerDTO customerDTO = new CustomerDTO(
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                order.getCustomer().getAddress());

        return new OrderDetailDTO(
                order.getId(),
                order.getItems(),
                order.getDate(),
                order.getTotalValue(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                customerDTO);
    }

    @Transactional
    private void recalculateTotal(Order order) {
        double totalValue = order.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        order.setTotalValue(totalValue);
    }
}
