package com.example.pdv_galeteria.model;

public class ItemEmPedido {
    private final String productId;
    private final int quantity;

    public ItemEmPedido(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
} 
