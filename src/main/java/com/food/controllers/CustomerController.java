package com.food.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.food.exceptions.NotFoundException;
import com.food.models.Customer;
import com.food.repositories.CustomerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping()
    @ResponseStatus(code = HttpStatus.OK)
    public List<Customer> listCustomers() {
        return customerRepository.findAll();
    }

    @PostMapping()
    @ResponseStatus(code = HttpStatus.CREATED)
    public Customer createCustomer(@RequestBody Customer customer) {
        return customerRepository.save(customer);
    }

    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public Customer getCustomer(@PathVariable() Long id) throws NotFoundException {
        return customerRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado."));
    }

    @PatchMapping("/{id}")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Customer updateCustomer(@PathVariable Long id, @RequestBody Customer data) {
        return customerRepository.findById(id)
                .map(customer -> {
                    if (data.getName() != null) {
                        customer.setName(data.getName());
                    }
                    if (data.getAddress() != null) {
                        customer.setAddress(data.getAddress());
                    }

                    return customerRepository.save(customer);
                })
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable() Long id) {
        customerRepository.deleteById(id);
    }
}
