package com.food.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.food.exceptions.NotFoundException;
import com.food.models.Customer;
import com.food.repositories.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService customerService;

    private Customer mockCustomer;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer();
        mockCustomer.setId(1L);
        mockCustomer.setName("John Doe");
        mockCustomer.setEmail("johndoe@example.com");
        mockCustomer.setPassword("password123");
        mockCustomer.setAddress("123 Street, City");
        mockCustomer.setActive(true);
    }

    @Test
    void testListCustomers() {
        when(customerRepository.findAll()).thenReturn(Arrays.asList(mockCustomer));

        List<Customer> customers = customerService.listCustomers();

        assertNotNull(customers);
        assertEquals(1, customers.size());
        assertEquals("John Doe", customers.get(0).getName());
    }

    @Test
    void testGetCustomerFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));

        Customer customer = customerService.getCustomer(1L);
        
        assertNotNull(customer);
        assertEquals("John Doe", customer.getName());
    }

    @Test
    void testGetCustomerNotFound() {
        when(customerRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> customerService.getCustomer(2L));
    }

    @Test
    void testCreateCustomer() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);

        Customer newCustomer = new Customer();
        newCustomer.setName("Jane Doe");
        newCustomer.setEmail("janedoe@example.com");
        newCustomer.setPassword("password456");

        Customer createdCustomer = customerService.createCustomer(newCustomer);

        assertNotNull(createdCustomer);
        assertEquals("John Doe", createdCustomer.getName());
    }

    @Test
    void testUpdateCustomerFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);

        Customer updateData = new Customer();
        updateData.setName("Updated Name");
        updateData.setEmail("updated@example.com");
        updateData.setPassword("newpassword");

        Customer updatedCustomer = customerService.updateCustomer(1L, updateData);

        assertNotNull(updatedCustomer);
        assertEquals("Updated Name", updatedCustomer.getName());
        assertEquals("updated@example.com", updatedCustomer.getEmail());
    }

    @Test
    void testUpdateCustomerNotFound() {
        when(customerRepository.findById(2L)).thenReturn(Optional.empty());

        Customer updateData = new Customer();
        updateData.setName("Updated Name");
        updateData.setEmail("updated@example.com");
        
        assertThrows(NotFoundException.class, () -> customerService.updateCustomer(2L, updateData));
    }

    @Test
    void testDeactivateCustomerFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);

        Customer deactivatedCustomer = customerService.deactivateCustomer(1L);

        assertNotNull(deactivatedCustomer);
        assertFalse(deactivatedCustomer.getActive());
    }

    @Test
    void testDeactivateCustomerNotFound() {
        when(customerRepository.findById(2L)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, () -> customerService.deactivateCustomer(2L));
    }

    @Test
    void testDeleteCustomer() {
        doNothing().when(customerRepository).deleteById(1L);

        assertDoesNotThrow(() -> customerService.deleteCustomer(1L));
    }
}