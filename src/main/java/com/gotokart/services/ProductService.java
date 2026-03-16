package com.gotokart.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gotokart.model.Product;
import com.gotokart.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    ProductRepository repo;

    public List<Product> getAllProducts(){
        return repo.findAll();
    }

    public Product addProduct(Product product){
        return repo.save(product);
    }

}