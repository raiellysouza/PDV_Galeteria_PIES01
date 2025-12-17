package com.example.pdv_galeteria.model;

import java.math.BigDecimal;

public class PrecoProduto {
    private final String productId;
    private final BigDecimal price;

    public PrecoProduto(String productId, BigDecimal price) {
        this.productId = productId;
        this.price = price;
    }

    public String getProductId() { return productId; }
    public BigDecimal getPrice() { return price; }
}