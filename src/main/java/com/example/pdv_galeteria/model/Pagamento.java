package com.example.pdv_galeteria.model;

import java.math.BigDecimal;
import java.util.Map;

public class PaymentResult {
    private final BigDecimal totalDue;
    private final BigDecimal paid;
    private final BigDecimal change;
    private final Map<String, Integer> breakdown;

    public PaymentResult(BigDecimal totalDue, BigDecimal paid, BigDecimal change, Map<String, Integer> breakdown) {
        this.totalDue = totalDue;
        this.paid = paid;
        this.change = change;
        this.breakdown = breakdown;
    }

    public BigDecimal getTotalDue() { return totalDue; }
    public BigDecimal getPaid() { return paid; }
    public BigDecimal getChange() { return change; }
    public Map<String, Integer> getBreakdown() { return breakdown; }
}
