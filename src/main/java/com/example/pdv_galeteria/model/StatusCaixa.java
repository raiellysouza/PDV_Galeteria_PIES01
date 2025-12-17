package com.example.pdv_galeteria.model;

public enum StatusCaixa {
    ABERTO("Aberto"),
    FECHADO("Fechado");

    private final String descricao;

    StatusCaixa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}