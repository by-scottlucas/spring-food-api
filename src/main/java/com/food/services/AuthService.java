package com.food.services;

import java.util.Optional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.food.models.Customer;
import com.food.repositories.CustomerRepository;

@Service
public class AuthService {
    private final JwtService jwtService;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(
        JwtService jwtService,
        CustomerService customerService,
        CustomerRepository customerRepository,
        BCryptPasswordEncoder passwordEncoder
    ) {
        this.jwtService = jwtService;
        this.customerService = customerService;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Customer register(Customer data) {
        return this.customerService.createCustomer(data);
    }

    public String login(String email, String password) {
        Optional<Customer> userOptional = this.customerRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            Customer user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                String token = jwtService.generateToken(user.getEmail());
                return token;
            } else {
                throw new BadCredentialsException("Senha incorreta");
            }
        } else {
            throw new BadCredentialsException("Usuário não encontrado");
        }
    }

    public boolean validateToken(String token) {
        String username = jwtService.validateToken(token);
        if (username != null && this.customerRepository.findByEmail(username).isPresent()) {
            return true;
        } else {
            return false;
        }
    }
}