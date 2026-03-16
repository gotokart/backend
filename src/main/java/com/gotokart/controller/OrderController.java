package com.gotokart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gotokart.model.Order;
import com.gotokart.services.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    OrderService service;

    @PostMapping
    public Order createOrder(@RequestBody Order order){
        return service.createOrder(order);
    }
}