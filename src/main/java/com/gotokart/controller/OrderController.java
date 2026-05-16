package com.gotokart.controller;

import com.gotokart.model.Order;
import com.gotokart.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST,
        RequestMethod.DELETE, RequestMethod.PUT,
        RequestMethod.PATCH, RequestMethod.OPTIONS
})
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/{userId}/place")
    public Order placeOrder(@PathVariable Long userId) {
        return orderService.placeOrder(userId);
    }

    @GetMapping("/{userId}")
    public List<Order> getOrders(@PathVariable Long userId) {
        return orderService.getOrders(userId);
    }

    /** Admin: list every order in the system, newest first. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    /** Admin: move an order through PLACED → SHIPPED → DELIVERED / CANCELLED. */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Order updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return orderService.updateStatus(id, body.get("status"));
    }
}
