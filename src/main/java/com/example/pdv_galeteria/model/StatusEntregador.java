package com.example.pdv_galeteria.model;

public enum StatusEntregador {
    DISPONIVEL("Disponível"),
    EM_ENTREGA("Em Entrega"),
    INATIVO("Inativo");

    private final String descricao;

    StatusEntregador(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static StatusEntregador fromString(String text) {
        for (StatusEntregador status : StatusEntregador.values()) {
            if (status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        return DISPONIVEL;
    }
}