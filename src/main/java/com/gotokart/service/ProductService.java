package com.gotokart.service;

import com.gotokart.model.Product;
import com.gotokart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> getAllProducts() { return productRepository.findAll(); }
    public Product getProductById(Long id) { return productRepository.findById(id).orElseThrow(); }
    public Product saveProduct(Product product) { return productRepository.save(product); }
    public Product updateProduct(Long id, Product product) {
        product.setId(id);
        return productRepository.save(product);
    }
    public void deleteProduct(Long id) { productRepository.deleteById(id); }
}