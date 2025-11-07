package com.example.pdv_galeteria.dto;

import lombok.Data;

@Data
public class EntradaEstoqueRequest {
    private Long produtoId;
    private Integer quantidade;
    private String observacao;
}
