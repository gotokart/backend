package com.gotokart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gotokart.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long>{

}