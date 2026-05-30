package com.gotokart.service;

import com.gotokart.model.Coupon;
import com.gotokart.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public List<Coupon> getAll() {
        deactivateExpiredCoupons();
        return couponRepository.findAllByOrderByCreatedAtDesc();
    }

    /** Active, non-expired coupons for the storefront cart dropdown. */
    public List<Coupon> getActiveForStorefront() {
        deactivateExpiredCoupons();
        return couponRepository.findByActiveTrueOrderByCodeAsc().stream()
                .filter(c -> c.getValidUntil() == null
                        || c.getValidUntil().isAfter(LocalDateTime.now()))
                .filter(c -> c.getUsageLimit() == null
                        || c.getUsedCount() == null
                        || c.getUsedCount() < c.getUsageLimit())
                .toList();
    }

    @Transactional
    public void deactivateExpiredCoupons() {
        List<Coupon> expired = couponRepository
                .findByActiveTrueAndValidUntilIsNotNullAndValidUntilBefore(LocalDateTime.now());
        if (expired.isEmpty()) return;
        expired.forEach(c -> c.setActive(false));
        couponRepository.saveAll(expired);
    }

    public Coupon getById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
    }

    public Coupon create(Coupon coupon) {
        if (coupon.getCode() == null || coupon.getCode().isBlank()) {
            throw new RuntimeException("Coupon code is required");
        }
        if (coupon.getDiscountPercent() == null
                || coupon.getDiscountPercent() < 1
                || coupon.getDiscountPercent() > 100) {
            throw new RuntimeException("discountPercent must be between 1 and 100");
        }
        String normalised = coupon.getCode().trim().toUpperCase();
        if (couponRepository.findByCodeIgnoreCase(normalised).isPresent()) {
            throw new RuntimeException("Coupon code already exists");
        }
        coupon.setCode(normalised);
        coupon.setUsedCount(0);
        coupon.setActive(coupon.getActive() == null ? Boolean.TRUE : coupon.getActive());
        return couponRepository.save(coupon);
    }

    public Coupon update(Long id, Coupon patch) {
        Coupon existing = getById(id);
        if (patch.getDiscountPercent() != null) {
            if (patch.getDiscountPercent() < 1 || patch.getDiscountPercent() > 100) {
                throw new RuntimeException("discountPercent must be between 1 and 100");
            }
            existing.setDiscountPercent(patch.getDiscountPercent());
        }
        if (patch.getValidUntil() != null) existing.setValidUntil(patch.getValidUntil());
        if (patch.getUsageLimit() != null) existing.setUsageLimit(patch.getUsageLimit());
        if (patch.getActive()     != null) existing.setActive(patch.getActive());
        return couponRepository.save(existing);
    }

    public void delete(Long id) {
        couponRepository.deleteById(id);
    }

    /**
     * Public-facing redemption check. Doesn't actually consume a usage — the
     * cart/order flow can decide when to call markRedeemed(). Returned coupon
     * is the canonical row; the caller can read discountPercent off it.
     */
    public Coupon validateForRedemption(String code) {
        deactivateExpiredCoupons();
        if (code == null || code.isBlank()) {
            throw new RuntimeException("Coupon code is required");
        }
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        if (!Boolean.TRUE.equals(coupon.getActive())) {
            throw new RuntimeException("Coupon is inactive");
        }
        if (coupon.getValidUntil() != null
                && !coupon.getValidUntil().isAfter(LocalDateTime.now())) {
            coupon.setActive(false);
            couponRepository.save(coupon);
            throw new RuntimeException("Coupon has expired");
        }
        if (coupon.getUsageLimit() != null
                && coupon.getUsedCount() != null
                && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            coupon.setActive(false);
            couponRepository.save(coupon);
            throw new RuntimeException("Coupon usage limit reached");
        }
        return coupon;
    }
}
