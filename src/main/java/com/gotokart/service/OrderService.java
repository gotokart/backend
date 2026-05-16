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
    private final CouponRepository couponRepository;

    /** Legacy overload — no coupon. Kept so older callers / scripts still work. */
    @Transactional
    public Order placeOrder(Long userId) {
        return placeOrder(userId, null);
    }

    /**
     * Atomically: reserves stock, snapshots each line item, optionally
     * validates + applies a coupon (bumping its usedCount), and finally
     * clears the cart. Everything runs inside one DB transaction so a
     * mid-checkout failure leaves no half-state.
     */
    @Transactional
    public Order placeOrder(Long userId, String couponCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        if (cartItems == null || cartItems.isEmpty())
            throw new RuntimeException("Cart is empty");

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PLACED");
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalAmount(0.0);
        Order savedOrder = orderRepository.save(order);

        double subtotal = 0.0;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            int newStock = product.getStock() - cartItem.getQuantity();
            if (newStock < 0)
                throw new RuntimeException("Insufficient stock for: " + product.getName());
            product.setStock(newStock);
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProductName(product.getName());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotal(product.getPrice() * cartItem.getQuantity());
            orderItemRepository.save(orderItem);

            subtotal += orderItem.getSubtotal();
        }

        // ── Apply coupon (if any) ─────────────────────────────────────
        double discount = 0.0;
        String appliedCode = null;
        if (couponCode != null && !couponCode.isBlank()) {
            Coupon coupon = couponRepository.findByCodeIgnoreCase(couponCode.trim())
                    .orElseThrow(() -> new RuntimeException("Invalid coupon code"));
            // Re-validate at the moment of redemption (active / not expired /
            // under usage limit). The cart's earlier validateForRedemption()
            // call could have been many minutes ago.
            if (!Boolean.TRUE.equals(coupon.getActive()))
                throw new RuntimeException("Coupon is inactive");
            if (coupon.getValidUntil() != null
                    && coupon.getValidUntil().isBefore(LocalDateTime.now()))
                throw new RuntimeException("Coupon has expired");
            if (coupon.getUsageLimit() != null
                    && coupon.getUsedCount() != null
                    && coupon.getUsedCount() >= coupon.getUsageLimit())
                throw new RuntimeException("Coupon usage limit reached");

            int pct = coupon.getDiscountPercent() == null ? 0 : coupon.getDiscountPercent();
            discount = Math.round(subtotal * pct) / 100.0;
            appliedCode = coupon.getCode();

            coupon.setUsedCount((coupon.getUsedCount() == null ? 0 : coupon.getUsedCount()) + 1);
            couponRepository.save(coupon);
        }

        double total = Math.max(0.0, subtotal - discount);

        savedOrder.setSubtotal(subtotal);
        savedOrder.setDiscountAmount(discount);
        savedOrder.setCouponCode(appliedCode);
        savedOrder.setTotalAmount(total);
        orderRepository.save(savedOrder);

        cartItemRepository.deleteAllByUserId(userId);

        return savedOrder;
    }

    public List<Order> getOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Whitelist of allowed status values. Anything outside this set is
     * rejected with a 400-style RuntimeException so we don't end up with
     * "Shippped" / "DELIVERD" typos in production.
     */
    private static final java.util.Set<String> VALID_STATUSES = java.util.Set.of(
            "PLACED", "SHIPPED", "DELIVERED", "CANCELLED"
    );

    @Transactional
    public Order updateStatus(Long orderId, String status) {
        if (status == null) {
            throw new RuntimeException("Status is required");
        }
        String normalised = status.trim().toUpperCase();
        if (!VALID_STATUSES.contains(normalised)) {
            throw new RuntimeException(
                    "Invalid status. Allowed: " + VALID_STATUSES);
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(normalised);
        return orderRepository.save(order);
    }
}