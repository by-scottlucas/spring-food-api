package com.food.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.food.models.Customer;
import com.food.repositories.CustomerRepository;
import com.food.services.AuthService;
import com.food.services.CustomerService;
import com.food.services.JwtService;
import com.food.utils.CustomerData;

public class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomerService customerService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Customer mockCustomer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockCustomer = new Customer();
        mockCustomer.setId(CustomerData.ID);
        mockCustomer.setName(CustomerData.NAME);
        mockCustomer.setEmail(CustomerData.EMAIL);
        mockCustomer.setPassword(CustomerData.HASHED_PASSWORD);
        mockCustomer.setAddress(CustomerData.ADDRESS);
        mockCustomer.setActive(CustomerData.ACTIVE);
    }

    @Test
    void testRegister() {
        when(customerService.createCustomer(any(Customer.class))).thenReturn(mockCustomer);

        Customer result = authService.register(mockCustomer);

        assertNotNull(result);
        assertEquals(CustomerData.EMAIL, result.getEmail());
    }

    @Test
    void testLoginSuccess() {
        when(customerRepository.findByEmail(CustomerData.EMAIL)).thenReturn(Optional.of(mockCustomer));
        when(passwordEncoder.matches(eq("correctPassword"), eq(mockCustomer.getPassword()))).thenReturn(true);
        when(jwtService.generateToken(CustomerData.EMAIL)).thenReturn("mockToken");

        String token = authService.login(CustomerData.EMAIL, "correctPassword");

        assertEquals("mockToken", token);
    }

    @Test
    void testLoginIncorrectPassword() {
        when(customerRepository.findByEmail(CustomerData.EMAIL)).thenReturn(Optional.of(mockCustomer));
        when(passwordEncoder.matches(anyString(), eq(mockCustomer.getPassword()))).thenReturn(false);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(CustomerData.EMAIL, "incorrectPassword");
        });

        assertEquals("Senha incorreta", exception.getMessage());
    }

    @Test
    void testLoginUserNotFound() {
        when(customerRepository.findByEmail(CustomerData.EMAIL)).thenReturn(Optional.empty());

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(CustomerData.EMAIL, "anyPassword");
        });

        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    void testValidateTokenValid() {
        when(jwtService.validateToken("validToken")).thenReturn(CustomerData.EMAIL);
        when(customerRepository.findByEmail(CustomerData.EMAIL)).thenReturn(Optional.of(mockCustomer));

        boolean isValid = authService.validateToken("validToken");

        assertTrue(isValid);
    }

    @Test
    void testValidateTokenInvalid() {
        when(jwtService.validateToken("invalidToken")).thenReturn(null);

        boolean isValid = authService.validateToken("invalidToken");

        assertFalse(isValid);
    }
}
