package com.example.pdv_galeteria.dto;

import com.example.pdv_galeteria.model.TipoMovimentoCaixa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RelatorioMovimentoCaixaDTO {

    private Long id;
    private LocalDateTime dataHora;
    private BigDecimal valor;
    private String tipo;
    private String descricao;

    public RelatorioMovimentoCaixaDTO(
            Long id,
            LocalDateTime dataHora,
            BigDecimal valor,
            TipoMovimentoCaixa tipo,
            String descricao
    ) {
        this.id = id;
        this.dataHora = dataHora;
        this.valor = valor;
        this.tipo = tipo != null ? tipo.name() : null;
        this.descricao = descricao;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDataMovimento() {
        return dataHora;
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
