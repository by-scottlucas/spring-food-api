package com.food.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.food.enums.OrderStatus;
import com.food.exceptions.NotFoundException;
import com.food.models.Item;
import com.food.models.Order;
import com.food.models.dtos.CustomerDTO;
import com.food.models.dtos.OrderDetailDTO;
import com.food.repositories.ItemRepository;
import com.food.repositories.OrderRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    public CheckoutController(OrderRepository orderRepository, ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ResponseEntity<Order> processOrder(@Valid @RequestBody Order data) {
        List<Item> items = new ArrayList<>();

        for (Item item : data.getItems()) {
            Item foundItem = itemRepository
                    .findById(item.getId())
                    .orElseThrow(() -> new NotFoundException("Item com ID " + item.getId() + " não encontrado"));
            items.add(foundItem);
        }

        data.setItems(items);
        data.setDate(new Date());
        data.setStatus(OrderStatus.PROCESSING);
        recalculateTotal(data);
        Order savedOrder = orderRepository.save(data);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<OrderDetailDTO> getOrderStatus(@PathVariable Long id) {
        Optional<Order> orderOpt = orderRepository.findById(id);

        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Order order = orderOpt.get();

        CustomerDTO customerDTO = new CustomerDTO(
            order.getCustomer().getId(),
            order.getCustomer().getName(),
            order.getCustomer().getAddress()
        );

        OrderDetailDTO orderDetailDTO = new OrderDetailDTO(
                order.getId(),
                order.getItems(),
                order.getDate(),
                order.getTotalValue(),
                order.getStatus(),
                customerDTO);

        return ResponseEntity.ok(orderDetailDTO);
    }

    @Transactional
    private void recalculateTotal(Order order) {
        double totalValue = order.getItems().stream().mapToDouble(Item::getPrice).sum();
        order.setTotalValue(totalValue);
    }
}
