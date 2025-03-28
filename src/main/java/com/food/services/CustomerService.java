package com.food.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.food.exceptions.NotFoundException;
import com.food.models.Customer;
import com.food.repositories.CustomerRepository;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
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
        if (data.getAddress() == null || data.getAddress().isEmpty()) {
            throw new IllegalArgumentException("O endereço é obrigatório");
        }

        return this.customerRepository.save(data);
    }

    public Customer updateCustomer(Long id, Customer data) throws NotFoundException {
        return this.customerRepository
                .findById(id)
                .map(response -> {
                    if (data.getName() != null) {
                        response.setName(data.getName());
                    }

                    if (data.getAddress() != null && !data.getAddress().isEmpty()) {
                        response.setAddress(data.getAddress());
                    }

                    if (data.getActive() != null) {
                        response.setActive(data.getActive());
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
