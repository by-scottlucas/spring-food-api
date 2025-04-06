package com.food.integration.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.food.models.Customer;
import com.food.models.Item;
import com.food.models.Order;
import com.food.services.JwtService;
import com.food.services.OrderService;
import com.food.utils.AuthData;
import com.food.utils.CustomerData;
import com.food.utils.ItemData;
import com.food.utils.OrderData;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private Customer mockCustomer;
    private Order mockOrder;
    private Item mockItem;
    private String mockToken;
    private UserDetails mockUserDetails;

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

        mockToken = "Bearer " + AuthData.TOKEN;
        when(jwtService.validateToken(AuthData.TOKEN)).thenReturn(AuthData.EMAIL);

        mockUserDetails = User.withUsername(AuthData.EMAIL)
                .password(AuthData.HASHED_PASSWORD)
                .roles("USER")
                .build();

        when(userDetailsService.loadUserByUsername(AuthData.EMAIL)).thenReturn(mockUserDetails);
    }

    @Test
    void listOrders_ShouldReturnOrdersWithLinks() throws Exception {
        when(orderService.listOrders()).thenReturn(List.of(mockOrder));

        mockMvc.perform(get("/api/v1/orders")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.orderList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.orderList[0].items", hasSize(1)))
                .andExpect(jsonPath("$._embedded.orderList[0].status").value(OrderData.STATUS.toString()))
                .andExpect(jsonPath("$._embedded.orderList[0].paymentMethod").value(OrderData.PAYMENT_METHOD.toString()))
                .andExpect(jsonPath("$._embedded.orderList[0].paymentStatus").value(OrderData.PAYMENT_STATUS.toString()))
                .andExpect(jsonPath("$._embedded.orderList[0].items[0].id").value(ItemData.ID))
                .andExpect(jsonPath("$._embedded.orderList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void getOrderByCustomer_ShouldReturnOrdersWithLinks() throws Exception {
        when(orderService.getOrdersByCustomer(mockCustomer.getId())).thenReturn(List.of(mockOrder));

        mockMvc.perform(get("/api/v1/orders/customer/{customerId}", CustomerData.ID)
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.orderList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.orderList[0].id").value(OrderData.ID))
                .andExpect(jsonPath("$._embedded.orderList[0].status").value(OrderData.STATUS.toString()))
                .andExpect(jsonPath("$._embedded.orderList[0].paymentMethod").value(OrderData.PAYMENT_METHOD.toString()))
                .andExpect(jsonPath("$._embedded.orderList[0].paymentStatus").value(OrderData.PAYMENT_STATUS.toString()))
                .andExpect(jsonPath("$._embedded.orderList[0].totalValue").value(OrderData.TOTAL_VALUE))
                .andExpect(jsonPath("$._embedded.orderList[0].items", hasSize(1)))
                .andExpect(jsonPath("$._embedded.orderList[0].items[0].id").value(ItemData.ID))
                .andExpect(jsonPath("$._embedded.orderList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void getOrder_ShouldReturnOrderWithLinks() throws Exception {
        when(orderService.getOrderById(OrderData.ID)).thenReturn(mockOrder);

        mockMvc.perform(get("/api/v1/orders/{id}", OrderData.ID)
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(OrderData.ID))
                .andExpect(jsonPath("$.status").value(OrderData.STATUS.toString()))
                .andExpect(jsonPath("$.paymentMethod").value(OrderData.PAYMENT_METHOD.toString()))
                .andExpect(jsonPath("$.paymentStatus").value(OrderData.PAYMENT_STATUS.toString()))
                .andExpect(jsonPath("$.totalValue").value(OrderData.TOTAL_VALUE))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(ItemData.ID))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.orders.href").exists())
                .andExpect(jsonPath("$._links.cancel.href").exists());
    }

    @Test
    void createOrder_ShouldReturnCreatedOrderWithLinks() throws Exception {
        when(orderService.createOrder(any(Order.class))).thenReturn(mockOrder);

        mockMvc.perform(post("/api/v1/orders")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockOrder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(OrderData.ID))
                .andExpect(jsonPath("$.status").value(OrderData.STATUS.toString()))
                .andExpect(jsonPath("$.paymentMethod").value(OrderData.PAYMENT_METHOD.toString()))
                .andExpect(jsonPath("$.paymentStatus").value(OrderData.PAYMENT_STATUS.toString()))
                .andExpect(jsonPath("$.totalValue").value(OrderData.TOTAL_VALUE))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(ItemData.ID))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.orders.href").exists());
    }

    @Test
    void updateOrder_ShouldReturnUpdatedOrderWithLinks() throws Exception {
        when(orderService.updateOrder(eq(OrderData.ID), any(Order.class))).thenReturn(mockOrder);

        mockMvc.perform(patch("/api/v1/orders/{id}", OrderData.ID)
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(OrderData.ID))
                .andExpect(jsonPath("$.status").value(OrderStatus.PENDING.toString()))
                .andExpect(jsonPath("$.paymentMethod").value(PaymentMethod.CREDIT_CARD.toString()))
                .andExpect(jsonPath("$.paymentStatus").value(PaymentStatus.PENDING.toString()))
                .andExpect(jsonPath("$.totalValue").value(OrderData.TOTAL_VALUE))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(ItemData.ID))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.orders.href").exists());
    }

    @Test
    void cancelOrder_ShouldReturnNoContent() throws Exception {
        doNothing().when(orderService).cancelOrder(OrderData.ID);

        mockMvc.perform(patch("/api/v1/orders/{id}/cancel", OrderData.ID)
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteOrder_ShouldReturnNoContent() throws Exception {
        doNothing().when(orderService).deleteOrder(OrderData.ID);

        mockMvc.perform(delete("/api/v1/orders/{id}", OrderData.ID)
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void getOrderSummary_ShouldReturnOrderSummary() throws Exception {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalOrders", 10);
        summary.put("totalRevenue", 100.0);
        when(orderService.getOrderSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/orders/summary")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(10))
                .andExpect(jsonPath("$.totalRevenue").value(100.0));
    }
}