package com.food.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.food.exceptions.NotFoundException;
import com.food.models.Customer;
import com.food.services.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public CollectionModel<EntityModel<Customer>> listCustomers() {
        List<EntityModel<Customer>> customers = this.customerService
                .listCustomers().stream().map(customer -> EntityModel.of(customer,
                        linkTo(methodOn(CustomerController.class).getCustomer(customer.getId())).withSelfRel()))
                .toList();

        return CollectionModel.of(customers,
                linkTo(CustomerController.class).slash("api/v1/customers").withSelfRel());
    }

    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public EntityModel<Customer> getCustomer(@PathVariable() Long id) throws NotFoundException {
        Customer customer = this.customerService.getCustomer(id);

        return EntityModel.of(customer,
                linkTo(methodOn(CustomerController.class).getCustomer(id)).withSelfRel(),
                linkTo(methodOn(CustomerController.class).listCustomers()).withRel("customers"),
                linkTo(methodOn(CustomerController.class).updateCustomer(id, customer)).withRel("update"),
                linkTo(methodOn(CustomerController.class).deactivateCustomer(id)).withRel("deactivate"),
                linkTo(methodOn(CustomerController.class).deleteCustomer(id)).withRel("delete"));
    }

    @PostMapping()
    @ResponseStatus(code = HttpStatus.CREATED)
    public EntityModel<Customer> createCustomer(@Valid @RequestBody Customer data) {
        Customer savedCustomer = this.customerService.createCustomer(data);

        return EntityModel.of(savedCustomer,
                linkTo(methodOn(CustomerController.class).getCustomer(savedCustomer.getId())).withSelfRel(),
                linkTo(methodOn(CustomerController.class).listCustomers()).withRel("customers"));
    }

    @PatchMapping("/{id}")
    @ResponseStatus(code = HttpStatus.CREATED)
    public EntityModel<Customer> updateCustomer(@PathVariable Long id, @Valid @RequestBody Customer data) {
        Customer updatedCustomer = this.customerService.updateCustomer(id, data);

        return EntityModel.of(updatedCustomer,
                linkTo(methodOn(CustomerController.class).getCustomer(updatedCustomer.getId())).withSelfRel(),
                linkTo(methodOn(CustomerController.class).listCustomers()).withRel("customers"),
                linkTo(methodOn(CustomerController.class).updateCustomer(updatedCustomer.getId(), updatedCustomer))
                        .withRel("update"),
                linkTo(methodOn(CustomerController.class).deactivateCustomer(updatedCustomer.getId()))
                        .withRel("deactivate"),
                linkTo(methodOn(CustomerController.class).deleteCustomer(updatedCustomer.getId())).withRel("delete"));
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public EntityModel<Customer> deactivateCustomer(@PathVariable Long id) {
        Customer updatedCustomer = this.customerService.deactivateCustomer(id);

        return EntityModel.of(updatedCustomer,
                linkTo(methodOn(CustomerController.class).getCustomer(updatedCustomer.getId())).withSelfRel(),
                linkTo(methodOn(CustomerController.class).listCustomers()).withRel("customers"),
                linkTo(methodOn(CustomerController.class).updateCustomer(updatedCustomer.getId(), updatedCustomer))
                        .withRel("update"),
                linkTo(methodOn(CustomerController.class).deactivateCustomer(updatedCustomer.getId()))
                        .withRel("deactivate"),
                linkTo(methodOn(CustomerController.class).deleteCustomer(updatedCustomer.getId())).withRel("delete"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteCustomer(@PathVariable() Long id) {
        this.customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
