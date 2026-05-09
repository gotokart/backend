package com.gotokart.service;

import com.gotokart.model.Category;
import com.gotokart.model.Product;
import com.gotokart.repository.CategoryRepository;
import com.gotokart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final S3StorageService s3StorageService;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow();
    }

    public Product saveProduct(Product product) {
        // If same name exists, just increase stock
        return productRepository.findByNameIgnoreCase(product.getName())
                .map(existing -> {
                    existing.setStock(existing.getStock() + product.getStock());
                    if (product.getPrice() != null) existing.setPrice(product.getPrice());
                    if (product.getDescription() != null) existing.setDescription(product.getDescription());
                    return productRepository.save(existing);
                })
                .orElseGet(() -> productRepository.save(product));
    }

    public Product updateProduct(Long id, Product product) {
        product.setId(id);
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id).orElseThrow();

        Category category = product.getCategory();
        String oldKey = product.getImageKey();

        productRepository.delete(product);

        if (productRepository.countByCategory(category) == 0) {
            categoryRepository.delete(category);
        }

        s3StorageService.deleteObject(oldKey);
    }

    public Product setImageKey(Long id, String imageKey) {
        Product product = productRepository.findById(id).orElseThrow();
        String oldKey = product.getImageKey();
        product.setImageKey(imageKey);
        Product saved = productRepository.save(product);
        if (oldKey != null && !oldKey.equals(imageKey)) {
            s3StorageService.deleteObject(oldKey);
        }
        return saved;
    }
}