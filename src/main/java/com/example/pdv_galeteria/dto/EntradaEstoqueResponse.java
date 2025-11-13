package com.example.pdv_galeteria.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EntradaEstoqueResponse {
    private Long id;
    private Long produtoId;
    private Integer quantidade;
    private String observacao;
    private LocalDateTime dataEntrada;
}
