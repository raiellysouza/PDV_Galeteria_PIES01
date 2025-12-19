package com.example.pdv_galeteria.model;

public class ItemEstoque {
    private final String productId;
    private int quantity;

    public ItemEstoque(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
