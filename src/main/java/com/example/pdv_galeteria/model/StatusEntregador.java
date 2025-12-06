package com.example.pdv_galeteria.model;

public enum StatusEntregador {
    DISPONIVEL("Disponível"),
    EM_ENTREGA("Em entrega"),
    AUSENTE("Ausente"),
    DESLIGADO("Desligado");

    private final String descricao;

    StatusEntregador(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}