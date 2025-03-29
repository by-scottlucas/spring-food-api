package com.food.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.food.enums.OrderStatus;
import com.food.enums.PaymentMethod;
import com.food.enums.PaymentStatus;
import com.food.exceptions.NotFoundException;
import com.food.models.Customer;
import com.food.models.Item;
import com.food.models.Order;
import com.food.models.dtos.OrderDetailDTO;
import com.food.repositories.ItemRepository;
import com.food.repositories.OrderRepository;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CheckoutService checkoutService;

    private Order order;
    private Item item1;
    private Item item2;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setAddress("123 Street, City");

        item1 = new Item();
        item1.setId(1L);
        item1.setName("Pizza");
        item1.setPrice(20.0);
        item1.setQuantity(2);

        item2 = new Item();
        item2.setId(2L);
        item2.setName("Hambúrguer");
        item2.setPrice(20.0);
        item2.setQuantity(1);

        order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setItems(List.of(item1, item2));
        order.setPaymentMethod(PaymentMethod.CREDIT_CARD);
    }

    @Test
    void processOrder_success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order processedOrder = checkoutService.processOrder(order);

        assertNotNull(processedOrder.getDate());
        assertEquals(OrderStatus.PROCESSING, processedOrder.getStatus());
        assertEquals(PaymentStatus.PENDING, processedOrder.getPaymentStatus());
        assertEquals(60.0, processedOrder.getTotalValue());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void processOrder_emptyItems() {
        order.setItems(new ArrayList<>());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> checkoutService.processOrder(order));
        assertEquals("O pedido deve conter pelo menos um item.", exception.getMessage());
    }

    @Test
    void processOrder_itemNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> checkoutService.processOrder(order));
    }

    @Test
    void processOrder_nullPaymentMethod() {
        order.setPaymentMethod(null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> checkoutService.processOrder(order));
        assertEquals("O método de pagamento é obrigatório", exception.getMessage());
    }

    @Test
    void getOrderStatus_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderDetailDTO orderDetailDTO = checkoutService.getOrderStatus(1L);

        assertNotNull(orderDetailDTO);
        assertEquals(order.getId(), orderDetailDTO.getId());
        assertEquals(order.getItems(), orderDetailDTO.getItems());
        assertEquals(order.getDate(), orderDetailDTO.getDate());
        assertEquals(order.getTotalValue(), orderDetailDTO.getTotalValue());
        assertEquals(order.getStatus(), orderDetailDTO.getStatus());
        assertEquals(order.getPaymentMethod(), orderDetailDTO.getPaymentMethod());
        assertEquals(order.getPaymentStatus(), orderDetailDTO.getPaymentStatus());
        assertEquals(order.getCustomer().getId(), orderDetailDTO.getCustomer().getId());
    }

    @Test
    void getOrderStatus_orderNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> checkoutService.getOrderStatus(1L));
    }

    @Test
    void recalculateTotal_correctTotal() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
        checkoutService.processOrder(order);
        assertEquals(60.0, order.getTotalValue());
    }
}