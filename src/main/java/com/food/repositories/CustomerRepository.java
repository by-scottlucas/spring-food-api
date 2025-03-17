package com.food.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.food.models.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {}