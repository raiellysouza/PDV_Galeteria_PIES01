/**package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.model.PaymentResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChangeCalculator {

    private static final BigDecimal[] DENOMINATIONS = new BigDecimal[] {
        new BigDecimal("100.00"),
        new BigDecimal("50.00"),
        new BigDecimal("20.00"),
        new BigDecimal("10.00"),
        new BigDecimal("5.00"),
        new BigDecimal("2.00"),
        new BigDecimal("1.00"),
        new BigDecimal("0.50"),
        new BigDecimal("0.25"),
        new BigDecimal("0.10"),
        new BigDecimal("0.05"),
        new BigDecimal("0.01")
    };

    public PaymentResult calculate(BigDecimal totalDue, BigDecimal paid) {
        if (totalDue == null || paid == null) {
            throw new IllegalArgumentException("totalDue and paid must not be null");
        }
        totalDue = totalDue.setScale(2, RoundingMode.HALF_UP);
        paid = paid.setScale(2, RoundingMode.HALF_UP);

        if (paid.compareTo(totalDue) < 0) {
            BigDecimal negative = paid.subtract(totalDue);
            return new PaymentResult(totalDue, paid, negative, new LinkedHashMap<>());
        }

        BigDecimal change = paid.subtract(totalDue).setScale(2, RoundingMode.HALF_UP);
        BigDecimal remainder = change;
        Map<String, Integer> breakdown = new LinkedHashMap<>();

        for (BigDecimal denom : DENOMINATIONS) {
            int count = remainder.divide(denom, 0, RoundingMode.FLOOR).intValue();
            if (count > 0) {
                breakdown.put(denom.setScale(2, RoundingMode.HALF_UP).toPlainString(), count);
                remainder = remainder.subtract(denom.multiply(new BigDecimal(count))).setScale(2, RoundingMode.HALF_UP);
            }
        }

        if (remainder.abs().compareTo(new BigDecimal("0.001")) <= 0) {
            remainder = BigDecimal.ZERO;
        }

        return new PaymentResult(totalDue, paid, change, breakdown);
    }
}
**/