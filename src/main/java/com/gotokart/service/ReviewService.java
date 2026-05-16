package com.gotokart.service;

import com.gotokart.model.Product;
import com.gotokart.model.Review;
import com.gotokart.model.User;
import com.gotokart.repository.ProductRepository;
import com.gotokart.repository.ReviewRepository;
import com.gotokart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository  reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository    userRepository;

    private static final Set<String> VALID_STATUSES = Set.of("PENDING", "APPROVED", "REJECTED");

    public List<Review> getAll() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Review> getByStatus(String status) {
        if (status == null) return getAll();
        return reviewRepository.findByStatusOrderByCreatedAtDesc(status.trim().toUpperCase());
    }

    public List<Review> getForProduct(Long productId) {
        return reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(productId, "APPROVED");
    }

    public Review submit(Long userId, Long productId, Integer rating, String comment) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new RuntimeException("Rating must be 1-5");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);
        review.setStatus("PENDING");
        return reviewRepository.save(review);
    }

    public Review updateStatus(Long id, String status) {
        if (status == null) throw new RuntimeException("Status is required");
        String normalised = status.trim().toUpperCase();
        if (!VALID_STATUSES.contains(normalised)) {
            throw new RuntimeException("Invalid status. Allowed: " + VALID_STATUSES);
        }
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setStatus(normalised);
        return reviewRepository.save(review);
    }

    public void delete(Long id) {
        reviewRepository.deleteById(id);
    }
}
