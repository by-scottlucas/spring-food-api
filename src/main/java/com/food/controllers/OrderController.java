package com.food.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.Map;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.food.models.Order;
import com.food.services.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public CollectionModel<EntityModel<Order>> listOrders() {
        List<EntityModel<Order>> orders = orderService.listOrders().stream()
                .map(order -> EntityModel.of(order,
                        linkTo(methodOn(OrderController.class).getOrder(order.getId())).withSelfRel()))
                .toList();

        return CollectionModel.of(orders,
                linkTo(methodOn(OrderController.class).listOrders()).withSelfRel());
    }

    @GetMapping("/customer/{customerId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CollectionModel<EntityModel<Order>> getOrderByCustomer(@PathVariable Long customerId) {
        List<Order> orders = orderService.getOrdersByCustomer(customerId);
        List<EntityModel<Order>> orderModels = orders.stream()
                .map(order -> EntityModel.of(order,
                        linkTo(methodOn(OrderController.class).getOrder(order.getId())).withSelfRel(),
                        linkTo(methodOn(OrderController.class).listOrders()).withRel("orders")))
                .toList();

        return CollectionModel.of(orderModels,
                linkTo(methodOn(OrderController.class).getOrderByCustomer(customerId)).withSelfRel());
    }

    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public EntityModel<Order> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return EntityModel.of(order,
                linkTo(methodOn(OrderController.class).getOrder(id)).withSelfRel(),
                linkTo(methodOn(OrderController.class).listOrders()).withRel("orders"),
                linkTo(methodOn(OrderController.class).cancelOrder(id)).withRel("cancel"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<Order> createOrder(@Valid @RequestBody Order data) {
        Order savedOrder = orderService.createOrder(data);
        return EntityModel.of(savedOrder,
                linkTo(methodOn(OrderController.class).getOrder(savedOrder.getId())).withSelfRel(),
                linkTo(methodOn(OrderController.class).listOrders()).withRel("orders"));
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EntityModel<Order> updateOrder(@PathVariable Long id, @Valid @RequestBody Order data) {
        Order updatedOrder = orderService.updateOrder(id, data);

        return EntityModel.of(updatedOrder,
                linkTo(methodOn(OrderController.class).getOrder(updatedOrder.getId())).withSelfRel(),
                linkTo(methodOn(OrderController.class).listOrders()).withRel("orders"));
    }

    @PatchMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }

    @GetMapping("/summary")
    @ResponseStatus(code = HttpStatus.OK)
    public Map<String, Object> getOrderSummary() {
        return orderService.getOrderSummary();
    }
}
