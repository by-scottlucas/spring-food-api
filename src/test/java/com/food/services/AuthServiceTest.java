package com.food.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
        mockCustomer.setEmail("test@example.com");
        mockCustomer.setPassword("$2a$10$7V2BhS.DYfgBOHEuUGo5F.QMyx/jRa/FaaHfj8y.gHLqbyU0zNi.y");
    }

    @Test
    void testRegister() {
        when(customerService.createCustomer(any(Customer.class))).thenReturn(mockCustomer);

        Customer result = authService.register(mockCustomer);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void testLoginSuccess() {
        when(customerRepository.findByEmail(mockCustomer.getEmail())).thenReturn(Optional.of(mockCustomer));
        when(passwordEncoder.matches(anyString(), eq(mockCustomer.getPassword()))).thenReturn(true);
        when(jwtService.generateToken(mockCustomer.getEmail())).thenReturn("mockToken");

        String token = authService.login(mockCustomer.getEmail(), "correctPassword");

        assertEquals("mockToken", token);
    }

    @Test
    void testLoginIncorrectPassword() {
        when(customerRepository.findByEmail(mockCustomer.getEmail())).thenReturn(Optional.of(mockCustomer));
        when(passwordEncoder.matches(anyString(), eq(mockCustomer.getPassword()))).thenReturn(false);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(mockCustomer.getEmail(), "incorrectPassword");
        });

        assertEquals("Senha incorreta", exception.getMessage());
    }

    @Test
    void testLoginUserNotFound() {
        when(customerRepository.findByEmail(mockCustomer.getEmail())).thenReturn(Optional.empty());

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(mockCustomer.getEmail(), "anyPassword");
        });

        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    void testValidateTokenValid() {
        when(jwtService.validateToken("validToken")).thenReturn("test@example.com");
        when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockCustomer));

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
