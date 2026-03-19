package com.gotokart.service;

import com.gotokart.model.*;
import com.gotokart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public Order placeOrder(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        if (cartItems == null || cartItems.isEmpty())
            throw new RuntimeException("Cart is empty");

        // Save order shell first
        Order order = new Order();
        order.setUser(user);
        order.setStatus("PLACED");
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalAmount(0.0);
        Order savedOrder = orderRepository.save(order);

        double total = 0.0;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // Reduce stock
            int newStock = product.getStock() - cartItem.getQuantity();
            if (newStock < 0)
                throw new RuntimeException("Insufficient stock for: " + product.getName());
            product.setStock(newStock);
            productRepository.save(product);

            // Save order item snapshot
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProductName(product.getName());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotal(product.getPrice() * cartItem.getQuantity());
            orderItemRepository.save(orderItem);

            total += orderItem.getSubtotal();
        }

        // Update total amount
        savedOrder.setTotalAmount(total);
        orderRepository.save(savedOrder);

        // Clear cart
        cartItemRepository.deleteAllByUserId(userId);

        return savedOrder;
    }

    public List<Order> getOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}