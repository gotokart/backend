package com.gotokart.repository;

import com.gotokart.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeIgnoreCase(String code);

    List<Coupon> findAllByOrderByCreatedAtDesc();

    List<Coupon> findByActiveTrueAndValidUntilIsNotNullAndValidUntilBefore(LocalDateTime cutoff);

    List<Coupon> findByActiveTrueOrderByCodeAsc();
}
