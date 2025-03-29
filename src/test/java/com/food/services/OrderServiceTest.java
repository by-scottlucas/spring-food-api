package com.food.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private Customer customer;
    private Item item;

     @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        
        item = new Item();
        item.setId(1L);
        item.setName("Pizza");
        item.setPrice(20.0);
        
        order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setItems(Arrays.asList(item));
        order.setStatus(OrderStatus.PENDING);
        order.setTotalValue(20.0);
    }

    @Test
    void testListOrders(){
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order));

        List<Order> orders = orderService.listOrders();

        assertNotNull(orders);
        assertFalse(orders.isEmpty());
        assertEquals(1, orders.size());
    }

    @Test
    void testGetOrderById(){
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order order = orderService.getOrderById(1L);

        assertNotNull(order);
        assertEquals(1, order.getId());
    }

    @Test
    void testGetOrderById_NotFound(){
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.getOrderById(2L));
    }

    @Test
    void testCreateOrder(){
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        Order updatedOrder = orderService.updateOrder(1L, order);
        assertNotNull(updatedOrder);
        assertEquals(1L, updatedOrder.getId());
    }

    @Test
    void testCancelOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        
        orderService.cancelOrder(1L);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }
    
    @Test
    void testDeleteOrder() {
        when(orderRepository.existsById(1L)).thenReturn(true);
        doNothing().when(orderRepository).deleteById(1L);
        
        assertDoesNotThrow(() -> orderService.deleteOrder(1L));
        verify(orderRepository, times(1)).deleteById(1L);
    }
    
    @Test
    void testDeleteOrder_NotFound() {
        when(orderRepository.existsById(2L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> orderService.deleteOrder(2L));
    }
}
