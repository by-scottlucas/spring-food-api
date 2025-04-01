package com.food.unit.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.food.models.Customer;
import com.food.repositories.CustomerRepository;
import com.food.services.UserDetailsServiceImpl;
import com.food.utils.CustomerData;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setEmail(CustomerData.EMAIL);
        customer.setPassword(CustomerData.PASSWORD);
    }

    @Test
    void testLoadUserByUsername_UserFound() {
        when(customerRepository.findByEmail(CustomerData.EMAIL)).thenReturn(java.util.Optional.of(customer));

        UserDetails userDetails = userDetailsService.loadUserByUsername(CustomerData.EMAIL);

        assertNotNull(userDetails);
        assertEquals(CustomerData.EMAIL, userDetails.getUsername());
        assertEquals(CustomerData.PASSWORD, userDetails.getPassword());
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(customerRepository.findByEmail(CustomerData.EMAIL)).thenReturn(java.util.Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(CustomerData.EMAIL));
    }
}
