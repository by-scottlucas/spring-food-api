package com.food.configs;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.food.models.Customer;
import com.food.models.Item;
import com.food.models.Order;
import com.food.repositories.CustomerRepository;
import com.food.repositories.ItemRepository;
import com.food.repositories.OrderRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;

    @Override
    public void run(String... args) {
        Customer customer1 = new Customer();
        customer1.setName("Lucas Santos");
        customer1.setAddress("Rua A, 123");

        Customer customer2 = new Customer();
        customer2.setName("Mariana Silva");
        customer2.setAddress("Rua B, 456");

        customerRepository.saveAll(Arrays.asList(customer1, customer2));

        Item item1 = new Item();
        item1.setName("Pizza de Calabresa");
        item1.setPrice(50.00);

        Item item2 = new Item();
        item2.setName("Hambúrguer Duplo");
        item2.setPrice(30.00);

        Item item3 = new Item();
        item3.setName("Suco de Laranja");
        item3.setPrice(20.00);

        itemRepository.saveAll(Arrays.asList(item1, item2, item3));

        List<Long> itemIds = Arrays.asList(item1.getId(), item2.getId(), item3.getId());
        List<Item> managedItems = itemRepository.findAllById(itemIds);

        Order order1 = new Order();
        order1.setCustomer(customer1);
        order1.setItems(managedItems.stream()
                .filter(item -> item.getId().equals(item1.getId()) ||
                        item.getId().equals(item3.getId()))
                .collect(Collectors.toList()));
        order1.setDate(new Date());
        order1.setTotalValue(70.00);

        Order order2 = new Order();
        order2.setCustomer(customer2);
        order2.setItems(
                managedItems
                        .stream()
                        .filter(item -> item.getId().equals(item2.getId()) ||
                                item.getId().equals(item3.getId()))
                        .collect(Collectors.toList()));
        order2.setDate(new Date());
        order2.setTotalValue(50.00);

        orderRepository.saveAll(Arrays.asList(order1, order2));

        System.out.println("Carga inicial adicionada com sucesso!");
    }
}