package com.example.pdv_galeteria.model;

import java.math.BigDecimal;
import java.util.Map;

public class Pagamento {
    private final BigDecimal totalDue;
    private final BigDecimal paid;
    private final BigDecimal change;
    private final Map<String, Integer> breakdown;
    private final boolean paymentComplete;

    public Pagamento(BigDecimal totalDue, BigDecimal paid, BigDecimal change,
                     Map<String, Integer> breakdown) {
        this.totalDue = totalDue;
        this.paid = paid;
        this.change = change;
        this.breakdown = breakdown;
        this.paymentComplete = change.compareTo(BigDecimal.ZERO) >= 0;
    }

    public Pagamento(BigDecimal totalDue, BigDecimal paid, BigDecimal change) {
        this(totalDue, paid, change, new java.util.LinkedHashMap<>());
    }

    public BigDecimal getTotalDue() { return totalDue; }
    public BigDecimal getPaid() { return paid; }
    public BigDecimal getChange() { return change; }
    public Map<String, Integer> getBreakdown() { return breakdown; }
    public boolean isPaymentComplete() { return paymentComplete; }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total: R$ ").append(String.format("%.2f", totalDue)).append("\n");
        sb.append("Pago: R$ ").append(String.format("%.2f", paid)).append("\n");

        if (change.compareTo(BigDecimal.ZERO) >= 0) {
            sb.append("Troco: R$ ").append(String.format("%.2f", change)).append("\n");

            if (!breakdown.isEmpty()) {
                sb.append("Detalhamento do troco:\n");
                for (Map.Entry<String, Integer> entry : breakdown.entrySet()) {
                    BigDecimal denom = new BigDecimal(entry.getKey());
                    BigDecimal totalForDenom = denom.multiply(new BigDecimal(entry.getValue()));
                    sb.append("  ")
                            .append(entry.getValue())
                            .append(" x R$ ")
                            .append(entry.getKey())
                            .append(" = R$ ")
                            .append(String.format("%.2f", totalForDenom))
                            .append("\n");
                }
            }
        } else {
            sb.append("Falta: R$ ").append(String.format("%.2f", change.abs()));
        }

        return sb.toString();
    }
}