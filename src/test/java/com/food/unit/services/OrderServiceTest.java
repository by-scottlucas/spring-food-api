package com.food.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.food.enums.OrderStatus;
import com.food.exceptions.NotFoundException;
import com.food.models.Customer;
import com.food.models.Item;
import com.food.models.Order;
import com.food.repositories.ItemRepository;
import com.food.repositories.OrderRepository;
import com.food.services.OrderService;
import com.food.utils.CustomerData;
import com.food.utils.ItemData;
import com.food.utils.OrderData;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private OrderService orderService;

    private Customer mockCustomer;
    private Order mockOrder;
    private Item mockItem;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer();
        mockCustomer.setId(CustomerData.ID);
        mockCustomer.setName(CustomerData.NAME);

        mockItem = new Item();
        mockItem.setId(ItemData.ID);
        mockItem.setName(ItemData.NAME);
        mockItem.setPrice(ItemData.PRICE);

        mockOrder = new Order();
        mockOrder.setId(OrderData.ID);
        mockOrder.setCustomer(mockCustomer);
        mockOrder.setItems(Arrays.asList(mockItem));
        mockOrder.setStatus(OrderData.STATUS);
        mockOrder.setPaymentMethod(OrderData.PAYMENT_METHOD);
        mockOrder.setPaymentStatus(OrderData.PAYMENT_STATUS);
        mockOrder.setTotalValue(OrderData.TOTAL_VALUE);
    }

    @Test
    void testListOrders() {
        when(orderRepository.findAll()).thenReturn(Arrays.asList(mockOrder));

        List<Order> orders = orderService.listOrders();

        assertNotNull(orders);
        assertFalse(orders.isEmpty());
        assertEquals(1, orders.size());
    }

    @Test
    void testGetOrderById() {
        when(orderRepository.findById(OrderData.ID)).thenReturn(Optional.of(mockOrder));

        Order order = orderService.getOrderById(OrderData.ID);

        assertNotNull(order);
        assertEquals(OrderData.ID, order.getId());
    }

    @Test
    void testGetOrderById_NotFound() {
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.getOrderById(2L));
    }

    @Test
    void testCreateOrder() {
        when(orderRepository.findById(OrderData.ID)).thenReturn(Optional.of(mockOrder));
        when(itemRepository.findById(ItemData.ID)).thenReturn(Optional.of(mockItem));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        Order updatedOrder = orderService.updateOrder(OrderData.ID, mockOrder);
        assertNotNull(updatedOrder);
        assertEquals(OrderData.ID, updatedOrder.getId());
    }

    @Test
    void testCancelOrder() {
        when(orderRepository.findById(OrderData.ID)).thenReturn(Optional.of(mockOrder));

        orderService.cancelOrder(OrderData.ID);
        assertEquals(OrderStatus.CANCELLED, mockOrder.getStatus());
        verify(orderRepository, times(1)).save(mockOrder);
    }

    @Test
    void testDeleteOrder() {
        when(orderRepository.existsById(OrderData.ID)).thenReturn(true);
        doNothing().when(orderRepository).deleteById(OrderData.ID);

        assertDoesNotThrow(() -> orderService.deleteOrder(OrderData.ID));
        verify(orderRepository, times(1)).deleteById(OrderData.ID);
    }

    @Test
    void testDeleteOrder_NotFound() {
        when(orderRepository.existsById(2L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> orderService.deleteOrder(2L));
    }
}
