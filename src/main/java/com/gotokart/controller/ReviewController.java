package com.gotokart.controller;

import com.gotokart.model.Review;
import com.gotokart.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST,
        RequestMethod.DELETE, RequestMethod.PUT,
        RequestMethod.PATCH, RequestMethod.OPTIONS
})
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** Public — list approved reviews for a product (for the storefront). */
    @GetMapping("/product/{productId}")
    public List<Review> forProduct(@PathVariable Long productId) {
        return reviewService.getForProduct(productId);
    }

    /** Public — submit a new review. Goes into PENDING for admin moderation. */
    @PostMapping
    public Review submit(@RequestBody Map<String, Object> body) {
        Long userId    = Long.valueOf(body.get("userId").toString());
        Long productId = Long.valueOf(body.get("productId").toString());
        Integer rating = Integer.valueOf(body.get("rating").toString());
        String comment = body.get("comment") == null ? null : body.get("comment").toString();
        return reviewService.submit(userId, productId, rating, comment);
    }

    /** Admin — list everything (or filter by ?status=PENDING). */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Review> getAll(@RequestParam(required = false) String status) {
        return reviewService.getByStatus(status);
    }

    /** Admin — approve / reject / re-pending a review. */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Review updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return reviewService.updateStatus(id, body.get("status"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        reviewService.delete(id);
    }
}
