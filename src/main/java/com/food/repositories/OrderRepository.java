package com.food.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.food.models.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {}