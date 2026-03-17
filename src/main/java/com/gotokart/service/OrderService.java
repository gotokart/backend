package com.gotokart.service;

import com.gotokart.model.Order;
import com.gotokart.model.CartItem;
import com.gotokart.model.User;
import com.gotokart.repository.OrderRepository;
import com.gotokart.repository.CartItemRepository;
import com.gotokart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    public Order placeOrder(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        double total = items.stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();
        Order order = new Order();
        order.setUser(user);
        order.setItems(items);
        order.setTotalAmount(total);
        order.setStatus("PLACED");
        order.setCreatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public List<Order> getOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}