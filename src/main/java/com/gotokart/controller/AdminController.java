package com.gotokart.controller;

import com.gotokart.dto.DashboardStatsDto;
import com.gotokart.dto.RevenueBucketDto;
import com.gotokart.model.User;
import com.gotokart.service.AdminService;
import com.gotokart.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Umbrella controller for admin-only operations that don't fit naturally on
 * the existing entity-specific controllers (e.g. aggregated stats, role
 * changes, deactivation). Everything here is gated by hasRole('ADMIN').
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST,
        RequestMethod.DELETE, RequestMethod.PUT,
        RequestMethod.PATCH, RequestMethod.OPTIONS
})
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService  userService;

    /** Dashboard overview KPIs + low-stock + recent-orders feed. */
    @GetMapping("/stats")
    public DashboardStatsDto stats() {
        return adminService.stats();
    }

    /** Time-bucketed revenue for the charts tab. period = daily | weekly | monthly. */
    @GetMapping("/revenue")
    public List<RevenueBucketDto> revenue(
            @RequestParam(defaultValue = "daily") String period) {
        return adminService.revenueByPeriod(period);
    }

    /** Promote / demote a user between ADMIN and USER. */
    @PatchMapping("/users/{id}/role")
    public User updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return userService.updateRole(id, body.get("role"));
    }

    /** Soft-deactivate (or reactivate) a user account. */
    @PatchMapping("/users/{id}/active")
    public User setActive(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object raw = body.get("active");
        boolean active = raw != null && Boolean.parseBoolean(raw.toString());
        return userService.setActive(id, active);
    }
}
