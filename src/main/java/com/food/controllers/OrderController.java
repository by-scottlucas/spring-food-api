package com.food.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.food.exceptions.NotFoundException;
import com.food.models.Item;
import com.food.models.Order;
import com.food.repositories.ItemRepository;
import com.food.repositories.OrderRepository;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    public OrderController(OrderRepository orderRepository, ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
    }

    @GetMapping()
    @ResponseStatus(code = HttpStatus.OK)
    public List<Order> listOrders() {
        return orderRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public Order createOrder(@RequestBody Order data) {
        List<Item> items = new ArrayList<>();
        double totalValue = 0.0;

        for (Item item : data.getItems()) {
            Item foundItem = itemRepository
                    .findById(item.getId())
                    .orElseThrow(() -> new NotFoundException(
                            "Item com ID " + item.getId() + " não encontrado"));

            items.add(foundItem);
            totalValue += foundItem.getPrice();
        }

        data.setItems(items);
        data.setTotalValue(totalValue);

        return orderRepository.save(data);
    }

    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public Order getOrder(@PathVariable() Long id) throws NotFoundException {
        return orderRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado."));
    }

    @PatchMapping("/{id}")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Order updateOrder(@PathVariable Long id, @RequestBody Order data) throws NotFoundException {
        return orderRepository.findById(id)
                .map(order -> {
                    if (data.getCustomer() != null) {
                        order.setCustomer(data.getCustomer());
                    }
                    if (data.getItems() != null && !data.getItems().isEmpty()) {
                        List<Item> items = new ArrayList<>();
                        double totalValue = 0.0;

                        for (Item item : data.getItems()) {
                            Item foundItem = itemRepository
                                    .findById(item.getId())
                                    .orElseThrow(() -> new NotFoundException(
                                            "Item com ID " + item.getId() + " não encontrado"));

                            items.add(foundItem);
                            totalValue += foundItem.getPrice();
                        }

                        order.setItems(items);
                        order.setTotalValue(totalValue);
                    }
                    if (data.getDate() != null) {
                        order.setDate(data.getDate());
                    }

                    return orderRepository.save(order);
                })
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado."));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable() Long id) {
        orderRepository.deleteById(id);
    }
}
