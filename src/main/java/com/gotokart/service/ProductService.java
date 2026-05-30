package com.gotokart.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gotokart.model.Category;
import com.gotokart.model.Product;
import com.gotokart.repository.CategoryRepository;
import com.gotokart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final S3StorageService s3StorageService;
    private final ObjectMapper objectMapper;

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
                .orElseGet(() -> {
                    fetchAndStoreProductImage(product);
                    return productRepository.save(product);
                });
    }

    private void fetchAndStoreProductImage(Product product) {
        if (product.getName() == null || product.getName().isBlank()) return;

        String slug = product.getName().toLowerCase().replace(' ', '-');
        String key = "products/" + slug + ".jpg";
        try {
            String unsplashUrl = resolveUnsplashSearchUrl(product.getName());
            byte[] imageBytes = downloadImage(unsplashUrl);
            try {
                s3StorageService.uploadObject(key, imageBytes, "image/jpeg");
                product.setImageKey(key);
                product.setImageUrl(s3StorageService.publicUrl(key));
            } catch (Exception s3Error) {
                log.warn("S3 upload failed for '{}', using Unsplash URL directly: {}",
                        product.getName(), s3Error.getMessage());
                product.setImageUrl(unsplashUrl);
            }
        } catch (Exception e) {
            log.warn("Could not fetch Unsplash image for product '{}': {}", product.getName(), e.getMessage());
        }
    }

    private String resolveUnsplashSearchUrl(String productName) throws IOException, InterruptedException {
        String searchUrl = "https://unsplash.com/napi/search/photos?query="
                + URLEncoder.encode(productName, StandardCharsets.UTF_8) + "&per_page=10";
        JsonNode root = objectMapper.readTree(downloadText(searchUrl));
        JsonNode results = root.path("results");
        if (!results.isArray() || results.isEmpty()) {
            throw new IOException("No Unsplash image found for: " + productName);
        }
        for (JsonNode result : results) {
            JsonNode urls = result.path("urls");
            for (String field : List.of("small", "regular", "thumb")) {
                String url = urls.path(field).asText(null);
                if (url != null && !url.isBlank()) {
                    return url;
                }
            }
        }
        throw new IOException("No suitable Unsplash URL for: " + productName);
    }

    private String downloadText(String url) throws IOException, InterruptedException {
        HttpResponse<String> response = sendGet(url, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("HTTP " + response.statusCode());
        }
        return response.body();
    }

    private byte[] downloadImage(String url) throws IOException, InterruptedException {
        HttpResponse<byte[]> response = sendGet(url, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() >= 400) {
            throw new IOException("HTTP " + response.statusCode());
        }
        return response.body();
    }

    private <T> HttpResponse<T> sendGet(String url, HttpResponse.BodyHandler<T> handler)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "*/*")
                .GET()
                .build();
        return client.send(request, handler);
    }

    /**
     * Partial update for the admin dashboard. Only fields that are present
     * in the request payload overwrite the existing row; null fields are
     * preserved. The image key is intentionally not handled here — it goes
     * through the dedicated /image route after a presigned S3 upload.
     */
    public Product updateProduct(Long id, Product patch) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (patch.getName() != null && !patch.getName().isBlank()) {
            existing.setName(patch.getName().trim());
        }
        if (patch.getDescription() != null) {
            existing.setDescription(patch.getDescription());
        }
        if (patch.getPrice() != null) {
            existing.setPrice(patch.getPrice());
        }
        if (patch.getStock() != null) {
            existing.setStock(patch.getStock());
        }
        if (patch.getCategory() != null && patch.getCategory().getId() != null) {
            categoryRepository.findById(patch.getCategory().getId())
                    .ifPresent(existing::setCategory);
        }
        return productRepository.save(existing);
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
        product.setImageUrl(s3StorageService.publicUrl(imageKey));
        Product saved = productRepository.save(product);
        if (oldKey != null && !oldKey.equals(imageKey)) {
            s3StorageService.deleteObject(oldKey);
        }
        return saved;
    }
}
