package com.example.pdv_galeteria.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RelatorioMovimentoCaixaDTO {

    private Long id;
    private LocalDateTime dataMovimento;
    private BigDecimal valor;
    private String tipo;
    private String descricao;

    public RelatorioMovimentoCaixaDTO(
            Long id,
            LocalDateTime dataMovimento,
            BigDecimal valor,
            String tipo,
            String descricao
    ) {
        this.id = id;
        this.dataMovimento = dataMovimento;
        this.valor = valor;
        this.tipo = tipo;
        this.descricao = descricao;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDataMovimento() {
        return dataMovimento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDescricao() {
        return descricao;
    }
}
