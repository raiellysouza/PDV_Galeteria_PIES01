package com.example.pdv_galeteria.model;

public enum TipoMovimentoCaixa {
    ENTRADA("Entrada"),
    SAIDA("Saída");

    private final String descricao;

    TipoMovimentoCaixa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}