package com.food.unit.services;

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
import com.food.enums.PaymentStatus;
import com.food.exceptions.NotFoundException;
import com.food.models.Customer;
import com.food.models.Item;
import com.food.models.Order;
import com.food.models.dtos.OrderDetailDTO;
import com.food.repositories.ItemRepository;
import com.food.repositories.OrderRepository;
import com.food.services.CheckoutService;
import com.food.utils.CustomerData;
import com.food.utils.ItemData;
import com.food.utils.OrderData;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CheckoutService checkoutService;

    private Customer mockCustomer;
    private Order mockOrder;
    private Item mockItem;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer();
        mockCustomer.setId(CustomerData.ID);
        mockCustomer.setName(CustomerData.NAME);
        mockCustomer.setAddress(CustomerData.ADDRESS);

        mockItem = new Item();
        mockItem.setId(ItemData.ID);
        mockItem.setName(ItemData.NAME);
        mockItem.setPrice(ItemData.PRICE);
        mockItem.setQuantity(ItemData.QUANTITY);

        mockOrder = new Order();
        mockOrder.setId(OrderData.ID);
        mockOrder.setCustomer(mockCustomer);
        mockOrder.setItems(List.of(mockItem));
        mockOrder.setPaymentMethod(OrderData.PAYMENT_METHOD);
        mockOrder.setStatus(OrderData.STATUS);
        mockOrder.setPaymentStatus(OrderData.PAYMENT_STATUS);
    }

    @Test
    void processOrder_success() {
        when(itemRepository.findById(ItemData.ID)).thenReturn(Optional.of(mockItem));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        Order processedOrder = checkoutService.processOrder(mockOrder);

        assertNotNull(processedOrder.getDate());
        assertEquals(OrderStatus.PROCESSING, processedOrder.getStatus());
        assertEquals(PaymentStatus.PENDING, processedOrder.getPaymentStatus());
        assertEquals(ItemData.PRICE * ItemData.QUANTITY, processedOrder.getTotalValue());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void processOrder_emptyItems() {
        mockOrder.setItems(new ArrayList<>());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> checkoutService.processOrder(mockOrder));

        assertEquals("O pedido deve conter pelo menos um item.", exception.getMessage());
    }

    @Test
    void processOrder_itemNotFound() {
        when(itemRepository.findById(ItemData.ID)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> checkoutService.processOrder(mockOrder));
    }

    @Test
    void processOrder_nullPaymentMethod() {
        mockOrder.setPaymentMethod(null);
        when(itemRepository.findById(ItemData.ID)).thenReturn(Optional.of(mockItem));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> checkoutService.processOrder(mockOrder));
        assertEquals("O método de pagamento é obrigatório", exception.getMessage());
    }

    @Test
    void getOrderStatus_success() {
        when(orderRepository.findById(OrderData.ID)).thenReturn(Optional.of(mockOrder));

        OrderDetailDTO orderDetailDTO = checkoutService.getOrderStatus(OrderData.ID);

        assertNotNull(orderDetailDTO);
        assertEquals(mockOrder.getId(), orderDetailDTO.getId());
        assertEquals(mockOrder.getItems(), orderDetailDTO.getItems());
        assertEquals(mockOrder.getDate(), orderDetailDTO.getDate());
        assertEquals(mockOrder.getTotalValue(), orderDetailDTO.getTotalValue());
        assertEquals(mockOrder.getStatus(), orderDetailDTO.getStatus());
        assertEquals(mockOrder.getPaymentMethod(), orderDetailDTO.getPaymentMethod());
        assertEquals(mockOrder.getPaymentStatus(), orderDetailDTO.getPaymentStatus());
        assertEquals(mockOrder.getCustomer().getId(), orderDetailDTO.getCustomer().getId());
    }

    @Test
    void getOrderStatus_orderNotFound() {
        when(orderRepository.findById(OrderData.ID)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> checkoutService.getOrderStatus(OrderData.ID));
    }

    @Test
    void recalculateTotal_correctTotal() {
        when(itemRepository.findById(ItemData.ID)).thenReturn(Optional.of(mockItem));
        checkoutService.processOrder(mockOrder);
        assertEquals(ItemData.PRICE * ItemData.QUANTITY, mockOrder.getTotalValue());
    }
}