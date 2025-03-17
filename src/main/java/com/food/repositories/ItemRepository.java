package com.food.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.food.models.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>{}
