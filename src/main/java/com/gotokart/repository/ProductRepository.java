package com.gotokart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gotokart.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long>{

}