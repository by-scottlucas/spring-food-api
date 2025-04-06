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

import java.util.List;

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
import com.food.exceptions.NotFoundException;
import com.food.models.Customer;
import com.food.services.CustomerService;
import com.food.services.JwtService;
import com.food.utils.AuthData;
import com.food.utils.CustomerData;

@SpringBootTest
@AutoConfigureMockMvc
public class CustomerControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private Customer mockCustomer;
    private String mockToken;
    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer();
        mockCustomer.setId(CustomerData.ID);
        mockCustomer.setName(CustomerData.NAME);
        mockCustomer.setEmail(CustomerData.EMAIL);
        mockCustomer.setPassword(CustomerData.PASSWORD);
        mockCustomer.setAddress(CustomerData.ADDRESS);
        mockCustomer.setActive(CustomerData.ACTIVE);

        mockToken = "Bearer " + AuthData.TOKEN;
        when(jwtService.validateToken(AuthData.TOKEN)).thenReturn(AuthData.EMAIL);

        mockUserDetails = User.withUsername(AuthData.EMAIL)
                .password(AuthData.HASHED_PASSWORD)
                .roles("USER")
                .build();

        when(userDetailsService.loadUserByUsername(AuthData.EMAIL)).thenReturn(mockUserDetails);
    }

    @Test
    void listCustomers_ShouldReturnCustomersWithLinks() throws Exception {
        when(customerService.listCustomers()).thenReturn(List.of(mockCustomer));

        mockMvc.perform(get("/api/v1/customers")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.customerList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.customerList[0].id").value(CustomerData.ID))
                .andExpect(jsonPath("$._embedded.customerList[0].name").value(CustomerData.NAME))
                .andExpect(jsonPath("$._embedded.customerList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void getCustomer_ShouldReturnCustomerWithLinks() throws Exception {
        when(customerService.getCustomer(CustomerData.ID)).thenReturn(mockCustomer);

        mockMvc.perform(get("/api/v1/customers/{id}", CustomerData.ID)
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CustomerData.ID))
                .andExpect(jsonPath("$.name").value(CustomerData.NAME))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.customers.href").exists())
                .andExpect(jsonPath("$._links.update.href").exists())
                .andExpect(jsonPath("$._links.deactivate.href").exists())
                .andExpect(jsonPath("$._links.delete.href").exists());
    }

    @Test
    void getCustomer_ShouldReturnNotFound() throws Exception {
        when(customerService.getCustomer(CustomerData.ID)).thenThrow(new NotFoundException("Customer not found"));

        mockMvc.perform(get("/api/v1/customers/{id}", CustomerData.ID)
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCustomer_ShouldReturnCreatedCustomerWithLinks() throws Exception {
        when(customerService.createCustomer(any(Customer.class))).thenReturn(mockCustomer);

        mockMvc.perform(post("/api/v1/customers")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockCustomer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(CustomerData.ID))
                .andExpect(jsonPath("$.name").value(CustomerData.NAME))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.customers.href").exists());
    }

    @Test
    void updateCustomer_ShouldReturnUpdatedCustomerWithLinks() throws Exception {
        when(customerService.updateCustomer(eq(CustomerData.ID), any(Customer.class))).thenReturn(mockCustomer);

        mockMvc.perform(patch("/api/v1/customers/{id}", CustomerData.ID)
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockCustomer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(CustomerData.ID))
                .andExpect(jsonPath("$.name").value(CustomerData.NAME))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.customers.href").exists())
                .andExpect(jsonPath("$._links.update.href").exists())
                .andExpect(jsonPath("$._links.deactivate.href").exists())
                .andExpect(jsonPath("$._links.delete.href").exists());
    }

    @Test
    void deactivateCustomer_ShouldReturnNoContent() throws Exception {
        when(customerService.deactivateCustomer(CustomerData.ID)).thenReturn(mockCustomer);

        mockMvc.perform(patch("/api/v1/customers/{id}/deactivate", CustomerData.ID)
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCustomer_ShouldReturnNoContent() throws Exception {
        doNothing().when(customerService).deleteCustomer(CustomerData.ID);

        mockMvc.perform(delete("/api/v1/customers/{id}", CustomerData.ID)
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}