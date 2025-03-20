package com.food.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    public OrderController(OrderRepository orderRepository, ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<Order> listOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/customer/{customerId}")
    @ResponseStatus(code = HttpStatus.OK)
    public List<Order> listOrdersByCustomer(@PathVariable Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @GetMapping("/date/{date}")
    @ResponseStatus(code = HttpStatus.OK)
    public List<Order> listOrdersByDate(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        return orderRepository.findByDate(date);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Order createOrder(@Valid @RequestBody Order data) {
        List<Item> items = new ArrayList<>();

        for (Item item : data.getItems()) {
            Item foundItem = itemRepository
                    .findById(item.getId())
                    .orElseThrow(() -> new NotFoundException("Item com ID " + item.getId() + " não encontrado"));
            items.add(foundItem);
        }

        data.setItems(items);
        recalculateTotal(data);
        return orderRepository.save(data);
    }

    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public Order getOrder(@PathVariable Long id) {
        return orderRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado."));
    }

    @PatchMapping("/{id}")
    @Transactional
    @ResponseStatus(code = HttpStatus.CREATED)
    public Order updateOrder(@PathVariable Long id, @Valid @RequestBody Order data) {
        return orderRepository
                .findById(id)
                .map(order -> {
                    if (data.getCustomer() != null) {
                        order.setCustomer(data.getCustomer());
                    }

                    if (data.getItems() != null && !data.getItems().isEmpty()) {
                        List<Item> items = new ArrayList<>();
                        for (Item item : data.getItems()) {
                            Item foundItem = itemRepository
                                    .findById(item.getId())
                                    .orElseThrow(() -> new NotFoundException(
                                            "Item com ID " + item.getId() + " não encontrado"));
                            items.add(foundItem);
                        }
                        order.setItems(items);
                        recalculateTotal(order);
                    }

                    if (data.getDate() != null) {
                        order.setDate(data.getDate());
                    }

                    return orderRepository.save(order);
                })
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado."));
    }

    @PatchMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void cancelOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado."));
        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void deleteOrder(@PathVariable Long id) {
        if (!orderRepository.existsById(id)) {
            throw new NotFoundException("Pedido não encontrado.");
        }
        orderRepository.deleteById(id);
    }

    @GetMapping("/summary")
    @ResponseStatus(code = HttpStatus.OK)
    public Map<String, Object> getOrderSummary() {
        List<Order> orders = orderRepository.findAll();
        double totalSales = orders.stream().mapToDouble(Order::getTotalValue).sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalOrders", orders.size());
        summary.put("totalSales", totalSales);
        return summary;
    }

    @Transactional
    private void recalculateTotal(Order order) {
        double totalValue = order.getItems().stream().mapToDouble(Item::getPrice).sum();
        order.setTotalValue(totalValue);
        orderRepository.save(order);
    }
}
