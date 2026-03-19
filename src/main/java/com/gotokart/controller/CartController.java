package com.gotokart.controller;

import com.gotokart.model.CartItem;
import com.gotokart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
        RequestMethod.GET,
        RequestMethod.POST,
        RequestMethod.DELETE,
        RequestMethod.PUT,
        RequestMethod.OPTIONS
})
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    public List<CartItem> getCart(@PathVariable Long userId) {
        return cartService.getCart(userId);
    }

    @PostMapping("/{userId}/add")
    public CartItem addToCart(@PathVariable Long userId,
                              @RequestParam Long productId,
                              @RequestParam Integer quantity) {
        return cartService.addToCart(userId, productId, quantity);
    }

    @DeleteMapping("/{userId}/remove")
    public ResponseEntity<String> removeFromCart(
            @PathVariable Long userId,
            @RequestParam Long productId) {
        cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok("removed");
    }
}