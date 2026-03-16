package com.gotokart.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gotokart.model.Order;
import com.gotokart.repository.OrderRepository;

@Service
public class OrderService {

    @Autowired
    OrderRepository repo;

    public Order createOrder(Order order){
        return repo.save(order);
    }

}