package com.gotokart.dto;

/**
 * One bar in the revenue chart.
 *
 * @param period  Human-readable label for the bucket (e.g. "2026-05-16", "Week 19", "May 2026").
 * @param orders  How many orders fell inside the bucket.
 * @param revenue Sum of order totals inside the bucket.
 */
public record RevenueBucketDto(String period, long orders, double revenue) {}
