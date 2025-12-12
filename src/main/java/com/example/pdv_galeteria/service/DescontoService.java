package com.example.pdv_galeteria.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DiscountService {

    public BigDecimal applyPercentageDiscount(BigDecimal price, BigDecimal percent) {
        if (price == null || percent == null) throw new IllegalArgumentException("price and percent must not be null");
        if (percent.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("percent must be >= 0");
        BigDecimal discount = price.multiply(percent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal result = price.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }

    public BigDecimal applyFixedDiscount(BigDecimal price, BigDecimal discountValue) {
        if (price == null || discountValue == null) throw new IllegalArgumentException("price and discount must not be null");
        BigDecimal result = price.subtract(discountValue).setScale(2, RoundingMode.HALF_UP);
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }
}
