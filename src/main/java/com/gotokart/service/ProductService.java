package com.gotokart.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gotokart.dto.ImageBackfillResultDto;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private static final String BROWSER_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final S3StorageService s3StorageService;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(15))
            .build();

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

    /**
     * Fetches a product photo from Unsplash, optionally stores a copy on S3,
     * and always exposes the Unsplash CDN URL in {@code imageUrl} so browsers
     * can load images without public S3 bucket access.
     */
    public boolean fetchAndStoreProductImage(Product product) {
        if (product.getName() == null || product.getName().isBlank()) return false;

        String slug = slugify(product.getName());
        String key = "products/" + slug + ".jpg";
        String displayUrl;
        byte[] imageBytes;

        try {
            displayUrl = resolveUnsplashSearchUrl(product);
            imageBytes = downloadImage(displayUrl);
        } catch (Exception unsplashError) {
            log.warn("Unsplash failed for '{}': {} — using Picsum fallback",
                    product.getName(), describeError(unsplashError));
            displayUrl = picsumUrlFor(slug);
            try {
                imageBytes = downloadImage(displayUrl);
            } catch (Exception picsumError) {
                log.warn("Image fetch failed for '{}': unsplash={}, picsum={}",
                        product.getName(), describeError(unsplashError), describeError(picsumError));
                return false;
            }
        }

        try {
            s3StorageService.uploadObject(key, imageBytes, "image/jpeg");
            product.setImageKey(key);
        } catch (Exception s3Error) {
            log.warn("S3 upload failed for '{}' (display URL still used): {}",
                    product.getName(), s3Error.getMessage());
        }
        product.setImageUrl(displayUrl);
        return true;
    }

    private static String slugify(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    private static String picsumUrlFor(String slug) {
        return "https://picsum.photos/seed/gotokart-" + slug + "/400/400";
    }

    private static String describeError(Exception e) {
        String msg = e.getMessage();
        return e.getClass().getSimpleName() + (msg != null ? ": " + msg : "");
    }

    /** Attach Unsplash images to products missing one or stuck on private S3 URLs. */
    public ImageBackfillResultDto backfillMissingImages(boolean force) {
        List<Product> targets = force
                ? productRepository.findAll()
                : productRepository.findNeedingImageRefresh();
        int updated = 0;
        List<String> failedNames = new ArrayList<>();

        for (Product product : targets) {
            if (fetchAndStoreProductImage(product)) {
                productRepository.save(product);
                updated++;
            } else {
                failedNames.add(product.getName());
            }
            pauseBetweenUnsplashRequests();
        }

        return new ImageBackfillResultDto(targets.size(), updated, failedNames.size(), failedNames);
    }

    private void pauseBetweenUnsplashRequests() {
        try {
            Thread.sleep(350);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String resolveUnsplashSearchUrl(Product product) throws IOException, InterruptedException {
        IOException lastError = null;
        for (String query : buildSearchQueries(product)) {
            try {
                return searchUnsplashQuery(query);
            } catch (IOException e) {
                lastError = e;
            }
        }
        throw lastError != null
                ? lastError
                : new IOException("No Unsplash image found for: " + product.getName());
    }

    /** Try full name first, then shorter generic phrases and category-based terms. */
    private List<String> buildSearchQueries(Product product) {
        Set<String> queries = new LinkedHashSet<>();
        String name = product.getName().trim();
        queries.add(name);

        String[] words = name.split("\\s+");
        if (words.length >= 2) {
            queries.add(words[words.length - 2] + " " + words[words.length - 1]);
        }
        if (words.length >= 3) {
            queries.add(String.join(" ", Arrays.copyOfRange(words, 1, words.length)));
        }
        if (words.length >= 4) {
            queries.add(String.join(" ", Arrays.copyOfRange(words, 2, words.length)));
        }
        if (words.length >= 1) {
            queries.add(words[words.length - 1]);
        }

        if (product.getCategory() != null && product.getCategory().getName() != null) {
            String category = product.getCategory().getName().trim();
            if (!category.isBlank()) {
                queries.add(category);
                if (words.length >= 1) {
                    queries.add(category + " " + words[words.length - 1]);
                }
            }
        }

        return List.copyOf(queries);
    }

    private String searchUnsplashQuery(String query) throws IOException, InterruptedException {
        String searchUrl = "https://unsplash.com/napi/search/photos?query="
                + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&per_page=10";
        JsonNode root = objectMapper.readTree(downloadText(searchUrl, true));
        JsonNode results = root.path("results");
        if (!results.isArray() || results.isEmpty()) {
            throw new IOException("No Unsplash image found for: " + query);
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
        throw new IOException("No suitable Unsplash URL for: " + query);
    }

    private String downloadText(String url, boolean unsplashApi) throws IOException, InterruptedException {
        HttpResponse<String> response = sendGet(url, HttpResponse.BodyHandlers.ofString(), unsplashApi);
        if (response.statusCode() >= 400) {
            throw new IOException("HTTP " + response.statusCode() + " for " + url);
        }
        return response.body();
    }

    private String downloadText(String url) throws IOException, InterruptedException {
        return downloadText(url, false);
    }

    private byte[] downloadImage(String url) throws IOException, InterruptedException {
        HttpResponse<byte[]> response = sendGet(url, HttpResponse.BodyHandlers.ofByteArray(), false);
        if (response.statusCode() >= 400) {
            throw new IOException("HTTP " + response.statusCode() + " for " + url);
        }
        return response.body();
    }

    private <T> HttpResponse<T> sendGet(String url, HttpResponse.BodyHandler<T> handler, boolean unsplashApi)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("User-Agent", BROWSER_USER_AGENT)
                .timeout(Duration.ofSeconds(30))
                .GET();

        if (unsplashApi) {
            builder.header("Accept", "application/json")
                    .header("Referer", "https://unsplash.com/")
                    .header("Origin", "https://unsplash.com");
        } else {
            builder.header("Accept", "*/*");
        }

        return httpClient.send(builder.build(), handler);
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
