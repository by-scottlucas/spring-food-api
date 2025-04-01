package com.food.unit.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Collections;
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
import com.food.services.CustomerService;
import com.food.utils.CustomerData;

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
                mockCustomer.setId(CustomerData.ID);
                mockCustomer.setName(CustomerData.NAME);
                mockCustomer.setEmail(CustomerData.EMAIL);
                mockCustomer.setPassword(CustomerData.PASSWORD);
                mockCustomer.setAddress(CustomerData.ADDRESS);
                mockCustomer.setActive(CustomerData.ACTIVE);
        }

        @Test
        void testListCustomers() {
                when(customerRepository
                                .findAll())
                                .thenReturn(Collections.singletonList(mockCustomer));

                List<Customer> customers = customerService.listCustomers();

                assertNotNull(customers);
                assertEquals(1, customers.size());
                assertEquals(CustomerData.NAME, customers.get(0).getName());
        }

        @Test
        void testGetCustomerFound() {
                when(customerRepository
                                .findById(CustomerData.ID))
                                .thenReturn(Optional.of(mockCustomer));

                Customer customer = customerService.getCustomer(CustomerData.ID);

                assertNotNull(customer);
                assertEquals(CustomerData.NAME, customer.getName());
        }

        @Test
        void testGetCustomerNotFound() {
                when(customerRepository
                                .findById(2L))
                                .thenReturn(Optional.empty());

                assertThrows(
                                NotFoundException.class,
                                () -> customerService.getCustomer(2L));
        }

        @Test
        void testCreateCustomer() {
                when(passwordEncoder
                                .encode(anyString()))
                                .thenReturn("encodedPassword");
                when(customerRepository
                                .save(any(Customer.class)))
                                .thenReturn(mockCustomer);

                mockCustomer.setPassword("encodedPassword");
                Customer createdCustomer = customerService.createCustomer(mockCustomer);

                assertNotNull(createdCustomer);
                assertEquals(CustomerData.NAME, createdCustomer.getName());
        }

        @Test
        void testUpdateCustomerFound() {
                when(customerRepository
                                .findById(CustomerData.ID))
                                .thenReturn(Optional.of(mockCustomer));
                when(customerRepository
                                .save(any(Customer.class)))
                                .thenReturn(mockCustomer);

                mockCustomer.setName("Updated Name");
                mockCustomer.setEmail("updated@example.com");

                Customer updatedCustomer = customerService.updateCustomer(CustomerData.ID, mockCustomer);

                assertNotNull(updatedCustomer);
                assertEquals("Updated Name", updatedCustomer.getName());
                assertEquals("updated@example.com", updatedCustomer.getEmail());
        }

        @Test
        void testUpdateCustomerNotFound() {
                when(customerRepository
                                .findById(2L))
                                .thenReturn(Optional.empty());

                assertThrows(
                                NotFoundException.class,
                                () -> customerService.updateCustomer(2L, mockCustomer));
        }

        @Test
        void testDeactivateCustomerFound() {
                when(customerRepository
                                .findById(CustomerData.ID))
                                .thenReturn(Optional.of(mockCustomer));
                when(customerRepository
                                .save(any(Customer.class)))
                                .thenReturn(mockCustomer);

                mockCustomer.setActive(false);
                Customer deactivatedCustomer = customerService.deactivateCustomer(CustomerData.ID);

                assertNotNull(deactivatedCustomer);
                assertFalse(deactivatedCustomer.getActive());
        }

        @Test
        void testDeactivateCustomerNotFound() {
                when(customerRepository
                                .findById(2L))
                                .thenReturn(Optional.empty());
                assertThrows(
                                NotFoundException.class,
                                () -> customerService.deactivateCustomer(2L));
        }

        @Test
        void testDeleteCustomer() {
                doNothing().when(customerRepository).deleteById(CustomerData.ID);
                assertDoesNotThrow(() -> customerService.deleteCustomer(CustomerData.ID));
        }
}
