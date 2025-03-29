package com.food.services;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.food.exceptions.NotFoundException;
import com.food.models.Customer;
import com.food.repositories.CustomerRepository;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public CustomerService(
        CustomerRepository customerRepository,
        BCryptPasswordEncoder passwordEncoder
    ) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Customer> listCustomers() {
        return this.customerRepository.findAll();
    }

    public Customer getCustomer(Long id) {
        return this.customerRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado."));
    }

    public Customer createCustomer(Customer data) {
        String encodedPassword = passwordEncoder.encode(data.getPassword());
        data.setPassword(encodedPassword);
        return this.customerRepository.save(data);
    }

    public Customer updateCustomer(Long id, Customer data) throws NotFoundException {
        return this.customerRepository
                .findById(id)
                .map(response -> {
                    response.setName(data.getName());
                    response.setEmail(data.getEmail());

                    if (data.getAddress() != null && !data.getAddress().isEmpty()) {
                        response.setAddress(data.getAddress());
                    }

                    if (data.getActive() != null) {
                        response.setActive(data.getActive());
                    }

                    if (data.getPassword() != null && !data.getPassword().isEmpty()) {
                        String encodedPassword = passwordEncoder.encode(data.getPassword());
                        response.setPassword(encodedPassword);
                    }

                    return this.customerRepository.save(response);
                }).orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
    }

    public Customer deactivateCustomer(Long id) {
        return this.customerRepository
                .findById(id)
                .map(customer -> {
                    customer.setActive(false);
                    return this.customerRepository.save(customer);
                })
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado."));
    }

    public void deleteCustomer(Long id) {
        this.customerRepository.deleteById(id);
    }
}
