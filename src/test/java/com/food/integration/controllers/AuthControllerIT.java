package com.food.integration.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.models.Customer;
import com.food.services.AuthService;
import com.food.utils.CustomerData;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(CustomerData.ID);
        customer.setName(CustomerData.NAME);
        customer.setEmail(CustomerData.EMAIL);
        customer.setPassword(CustomerData.HASHED_PASSWORD);
    }

    @Test
    void register_ShouldReturnCustomer_WhenSuccessful() throws Exception {
        when(authService.register(any(Customer.class))).thenReturn(customer);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(CustomerData.ID))
                .andExpect(jsonPath("$.name").value(CustomerData.NAME));
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() throws Exception {
        String token = "valid-token";
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", CustomerData.EMAIL);
        credentials.put("password", CustomerData.PASSWORD);

        when(authService.login(anyString(), anyString())).thenReturn(token);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenCredentialsAreInvalid() throws Exception {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", CustomerData.EMAIL);
        credentials.put("password", "wrong-password");

        when(authService.login(anyString(), anyString()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isUnauthorized());
    }
}