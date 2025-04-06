package com.food.integration.controllers;

import static org.hamcrest.Matchers.hasSize; // Ensure this import is present
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.enums.OrderStatus;
import com.food.enums.PaymentMethod;
import com.food.enums.PaymentStatus;
import com.food.exceptions.NotFoundException;
import com.food.models.Customer;
import com.food.models.Item;
import com.food.models.Order;
import com.food.models.dtos.CustomerDTO;
import com.food.models.dtos.OrderDetailDTO;
import com.food.services.CheckoutService;
import com.food.services.JwtService;
import com.food.utils.AuthData;
import com.food.utils.CustomerData;
import com.food.utils.ItemData;
import com.food.utils.OrderData;

@SpringBootTest
@AutoConfigureMockMvc
public class CheckoutControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CheckoutService checkoutService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private String mockToken;
    private UserDetails mockUserDetails;
    private Order mockOrder;
    private Item mockItem;
    private Customer mockCustomer;
    private OrderDetailDTO mockOrderDetailDTO;
    private Order orderWithInvalidItem;

    @BeforeEach
    void setUp() {
        mockToken = "Bearer " + AuthData.TOKEN;
        when(jwtService.validateToken(AuthData.TOKEN)).thenReturn(AuthData.EMAIL);

        mockUserDetails = User.withUsername(AuthData.EMAIL)
                .password(AuthData.HASHED_PASSWORD)
                .roles("USER")
                .build();
        when(userDetailsService.loadUserByUsername(AuthData.EMAIL)).thenReturn(mockUserDetails);

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
        mockOrder.setItems(Arrays.asList(mockItem));
        mockOrder.setPaymentMethod(OrderData.PAYMENT_METHOD);
        mockOrder.setStatus(OrderStatus.PROCESSING);
        mockOrder.setPaymentStatus(PaymentStatus.PENDING);
        mockOrder.setTotalValue(ItemData.PRICE * ItemData.QUANTITY);

        mockOrderDetailDTO = new OrderDetailDTO(
                mockOrder.getId(),
                mockOrder.getItems(),
                new Date(),
                mockOrder.getTotalValue(),
                mockOrder.getStatus(),
                mockOrder.getPaymentMethod(),
                mockOrder.getPaymentStatus(),
                new CustomerDTO(
                        mockCustomer.getId(),
                        mockCustomer.getName(),
                        mockCustomer.getAddress()
                )
        );

        orderWithInvalidItem = new Order();
        orderWithInvalidItem.setCustomer(mockCustomer);
        Item invalidItem = new Item();
        invalidItem.setId(999L);
        invalidItem.setName("Invalid Item");
        invalidItem.setPrice(1.0);
        invalidItem.setQuantity(1);
        orderWithInvalidItem.setItems(Arrays.asList(invalidItem));
        orderWithInvalidItem.setPaymentMethod(OrderData.PAYMENT_METHOD);
    }

    @Test
    void processOrder_ShouldReturnCreatedOrder() throws Exception {
        when(checkoutService.processOrder(any(Order.class))).thenReturn(mockOrder);

        mockMvc.perform(post("/api/v1/checkout")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockOrder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(mockOrder.getId()))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.status").value(OrderStatus.PROCESSING.toString()))
                .andExpect(jsonPath("$.paymentMethod").value(OrderData.PAYMENT_METHOD.toString()))
                .andExpect(jsonPath("$.paymentStatus").value(PaymentStatus.PENDING.toString()))
                .andExpect(jsonPath("$.totalValue").value(mockOrder.getTotalValue()));
    }

    @Test
    void processOrder_ShouldReturnBadRequest_WhenNoItems() throws Exception {
        Order orderWithoutItems = new Order();
        orderWithoutItems.setCustomer(mockCustomer);
        orderWithoutItems.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        when(checkoutService.processOrder(any(Order.class)))
                .thenThrow(new IllegalArgumentException("O pedido deve conter pelo menos um item."));

        mockMvc.perform(post("/api/v1/checkout")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderWithoutItems)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O pedido deve conter pelo menos um item."));
    }

    @Test
    void processOrder_ShouldReturnBadRequest_WhenNoPaymentMethod() throws Exception {
        Order orderWithoutPayment = new Order();
        orderWithoutPayment.setCustomer(mockCustomer);
        orderWithoutPayment.setItems(Arrays.asList(mockItem));

        when(checkoutService.processOrder(any(Order.class)))
                .thenThrow(new IllegalArgumentException("O método de pagamento é obrigatório"));

        mockMvc.perform(post("/api/v1/checkout")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderWithoutPayment)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O método de pagamento é obrigatório"));
    }

    @Test
    void processOrder_ShouldReturnNotFound_WhenItemNotFound() throws Exception {
        when(checkoutService.processOrder(any(Order.class)))
                .thenThrow(new NotFoundException("Item com ID 999 não encontrado"));

        mockMvc.perform(post("/api/v1/checkout")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderWithInvalidItem)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Item com ID 999 não encontrado"));
    }

    @Test
    void getOrderStatus_ShouldReturnOrderStatus() throws Exception {
        when(checkoutService.getOrderStatus(OrderData.ID)).thenReturn(mockOrderDetailDTO);

        mockMvc.perform(get("/api/v1/checkout/{id}/status", OrderData.ID)
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockOrderDetailDTO.getId()))
                .andExpect(jsonPath("$.status").value(mockOrderDetailDTO.getStatus().toString()))
                .andExpect(jsonPath("$.paymentMethod").value(mockOrderDetailDTO.getPaymentMethod().toString()))
                .andExpect(jsonPath("$.paymentStatus").value(mockOrderDetailDTO.getPaymentStatus().toString()))
                .andExpect(jsonPath("$.totalValue").value(mockOrderDetailDTO.getTotalValue()))
                .andExpect(jsonPath("$.customer.id").value(mockOrderDetailDTO.getCustomer().getId()))
                .andExpect(jsonPath("$.customer.name").value(mockOrderDetailDTO.getCustomer().getName()));
    }

    @Test
    void getOrderStatus_ShouldReturnNotFound() throws Exception {
        when(checkoutService.getOrderStatus(999L)).thenThrow(new NotFoundException("Pedido com ID 999 não encontrado"));

        mockMvc.perform(get("/api/v1/checkout/{id}/status", 999L)
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Pedido com ID 999 não encontrado"));
    }
}