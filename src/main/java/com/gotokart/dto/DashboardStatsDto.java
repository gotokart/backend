package com.gotokart.dto;

import com.gotokart.model.Order;
import com.gotokart.model.Product;

import java.util.List;

/**
 * Top-level KPIs surfaced on the admin Overview tab. Built by AdminService
 * by aggregating across the existing repositories — no SQL fan-out beyond a
 * handful of countXxx() calls and a small ORDER BY LIMIT.
 */
public record DashboardStatsDto(
        double totalRevenue,
        long totalOrders,
        long totalProducts,
        long totalUsers,
        long totalCategories,
        long ordersLast30Days,
        double revenueLast30Days,
        long lowStockCount,
        List<Product> lowStockProducts,
        List<Order> recentOrders
) {}
