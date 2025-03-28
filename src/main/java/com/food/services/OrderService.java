package com.food.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.food.enums.OrderStatus;
import com.food.exceptions.NotFoundException;
import com.food.models.Item;
import com.food.models.Order;
import com.food.repositories.ItemRepository;
import com.food.repositories.OrderRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    public OrderService(OrderRepository orderRepository, ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
    }

    public List<Order> listOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado."));
    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        if (orders.isEmpty()) {
            throw new NotFoundException(
                    "Nenhum pedido encontrado para o cliente com ID " + customerId);
        }
        return orders;
    }

    @Transactional
    public Order createOrder(Order data) {
        // Valida os itens e mapeia para os itens existentes no banco
        List<Item> items = data.getItems().stream().map(item -> itemRepository
                .findById(item.getId())
                .orElseThrow(() -> new NotFoundException(
                        "Item com ID " + item.getId() + " não encontrado")))
                .collect(Collectors.toList());

        // Definindo os itens no pedido e configurando o status
        data.setItems(items);
        data.setStatus(OrderStatus.PENDING);
        
        // Recalcula o total do pedido
        recalculateTotal(data);

        // Salva o pedido no banco
        return orderRepository.save(data);
    }

    @Transactional
    public Order updateOrder(Long id, Order data) {
        // Buscar o pedido existente
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado."));
        
        // Atualiza os itens do pedido
        List<Item> items = data.getItems().stream().map(item -> itemRepository
                .findById(item.getId())
                .orElseThrow(() -> new NotFoundException(
                        "Item com ID " + item.getId() + " não encontrado")))
                .collect(Collectors.toList());
        
        existingOrder.setItems(items);

        // Atualiza outras propriedades conforme necessário
        if (data.getDate() != null) {
            existingOrder.setDate(data.getDate());
        }
        if (data.getCustomer() != null) {
            existingOrder.setCustomer(data.getCustomer());
        }

        // Recalcula o total do pedido
        recalculateTotal(existingOrder);

        // Salva as mudanças no pedido
        return orderRepository.save(existingOrder);
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = getOrderById(id);
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new NotFoundException("Pedido não encontrado.");
        }
        orderRepository.deleteById(id);
    }

    public void recalculateTotal(Order order) {
        double totalValue = order.getItems().stream().mapToDouble(Item::getPrice).sum();
        order.setTotalValue(totalValue);
    }

    public Map<String, Object> getOrderSummary() {
        List<Order> orders = orderRepository.findAll();
        double totalSales = orders.stream().mapToDouble(Order::getTotalValue).sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalOrders", orders.size());
        summary.put("totalSales", totalSales);
        return summary;
    }
}
