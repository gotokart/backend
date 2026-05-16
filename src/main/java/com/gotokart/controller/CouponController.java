package com.gotokart.controller;

import com.gotokart.model.Coupon;
import com.gotokart.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST,
        RequestMethod.DELETE, RequestMethod.PUT,
        RequestMethod.PATCH, RequestMethod.OPTIONS
})
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Coupon> getAll() {
        return couponService.getAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Coupon create(@RequestBody Coupon coupon) {
        return couponService.create(coupon);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Coupon update(@PathVariable Long id, @RequestBody Coupon coupon) {
        return couponService.update(id, coupon);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        couponService.delete(id);
    }

    /** Public — used by the storefront's cart to check a coupon before checkout. */
    @GetMapping("/validate")
    public Coupon validate(@RequestParam String code) {
        return couponService.validateForRedemption(code);
    }
}
