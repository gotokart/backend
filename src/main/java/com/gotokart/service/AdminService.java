package com.gotokart.service;

import com.gotokart.dto.DashboardStatsDto;
import com.gotokart.dto.RevenueBucketDto;
import com.gotokart.model.Order;
import com.gotokart.model.Product;
import com.gotokart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final OrderRepository    orderRepository;
    private final ProductRepository  productRepository;
    private final UserRepository     userRepository;
    private final CategoryRepository categoryRepository;

    /** Configurable threshold for the "low stock" widget. */
    private static final int LOW_STOCK_THRESHOLD = 5;

    /** How many recent orders to show on the dashboard. */
    private static final int RECENT_ORDERS_LIMIT = 8;

    public DashboardStatsDto stats() {
        List<Order> allOrders = orderRepository.findAllByOrderByCreatedAtDesc();

        double totalRevenue = allOrders.stream()
                .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                .mapToDouble(o -> o.getTotalAmount() == null ? 0 : o.getTotalAmount())
                .sum();

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long ordersLast30Days = orderRepository.countByCreatedAtAfter(thirtyDaysAgo);
        double revenueLast30Days = allOrders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(thirtyDaysAgo))
                .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                .mapToDouble(o -> o.getTotalAmount() == null ? 0 : o.getTotalAmount())
                .sum();

        List<Product> lowStock = productRepository
                .findByStockLessThanEqualOrderByStockAsc(LOW_STOCK_THRESHOLD);

        List<Order> recent = allOrders.stream()
                .limit(RECENT_ORDERS_LIMIT)
                .toList();

        return new DashboardStatsDto(
                round2(totalRevenue),
                allOrders.size(),
                productRepository.count(),
                userRepository.count(),
                categoryRepository.count(),
                ordersLast30Days,
                round2(revenueLast30Days),
                lowStock.size(),
                lowStock,
                recent
        );
    }

    /**
     * Time-bucketed revenue for the charts tab.
     *
     * @param period one of "daily" (last 30 days), "weekly" (last 12 weeks),
     *               "monthly" (last 12 months). Anything else falls back to daily.
     */
    public List<RevenueBucketDto> revenueByPeriod(String period) {
        List<Order> all = orderRepository.findAllByOrderByCreatedAtDesc();
        String p = period == null ? "daily" : period.trim().toLowerCase();

        return switch (p) {
            case "monthly" -> bucket(all, 12, "monthly");
            case "weekly"  -> bucket(all, 12, "weekly");
            default        -> bucket(all, 30, "daily");
        };
    }

    private List<RevenueBucketDto> bucket(List<Order> orders, int count, String granularity) {
        // Pre-fill all buckets with zeros so the chart always has continuous data,
        // even on quiet days. Sum order totals into the right key.
        Map<String, double[]> agg = new HashMap<>();
        LocalDate today = LocalDate.now();

        List<String> keys = new ArrayList<>(count);
        for (int i = count - 1; i >= 0; i--) {
            String key = switch (granularity) {
                case "monthly" -> today.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM"));
                case "weekly"  -> weekKey(today.minusWeeks(i));
                default        -> today.minusDays(i).toString(); // yyyy-MM-dd
            };
            keys.add(key);
            agg.put(key, new double[]{0, 0}); // [orders, revenue]
        }

        for (Order o : orders) {
            if (o.getCreatedAt() == null) continue;
            if ("CANCELLED".equalsIgnoreCase(o.getStatus())) continue;
            LocalDate d = o.getCreatedAt().toLocalDate();
            String key = switch (granularity) {
                case "monthly" -> d.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                case "weekly"  -> weekKey(d);
                default        -> d.toString();
            };
            double[] bucket = agg.get(key);
            if (bucket != null) {
                bucket[0] += 1;
                bucket[1] += (o.getTotalAmount() == null ? 0 : o.getTotalAmount());
            }
        }

        return keys.stream()
                .map(k -> {
                    double[] b = agg.get(k);
                    return new RevenueBucketDto(k, (long) b[0], round2(b[1]));
                })
                .sorted(Comparator.comparing(RevenueBucketDto::period))
                .toList();
    }

    private static String weekKey(LocalDate d) {
        int year = d.get(IsoFields.WEEK_BASED_YEAR);
        int week = d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        // Snap to Monday so the same week always serialises identically.
        // (DayOfWeek import kept so future tweaks read clean.)
        DayOfWeek.MONDAY.getValue();
        return String.format("%d-W%02d", year, week);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
