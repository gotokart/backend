package com.gotokart.controller;

import com.gotokart.model.Product;
import com.gotokart.service.ProductService;
import com.gotokart.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final S3StorageService s3StorageService;

    @GetMapping
    public List<Product> getAll() { return productService.getAllProducts(); }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) { return productService.getProductById(id); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Product create(@RequestBody Product product) { return productService.saveProduct(product); }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product product) {
        return productService.updateProduct(id, product);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) { productService.deleteProduct(id); }

    /**
     * Step 1 of the image upload flow.
     * Admin asks the backend for a short-lived presigned PUT URL.
     * The browser will upload the file bytes directly to S3 with that URL —
     * the file never passes through this Spring Boot process.
     */
    @PostMapping("/{id}/image-upload-url")
    @PreAuthorize("hasRole('ADMIN')")
    public S3StorageService.PresignedUpload getUploadUrl(
            @PathVariable Long id,
            @RequestParam String contentType) {
        productService.getProductById(id);
        return s3StorageService.presignUpload(contentType);
    }

    /**
     * Step 2 of the image upload flow.
     * After the browser PUT to S3 succeeds, it tells the backend the new key
     * so we can persist it on the product row.
     */
    @PatchMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public Product attachImage(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return productService.setImageKey(id, body.get("imageKey"));
    }
}