package com.example.pdv_galeteria.dto;
import java.math.BigDecimal;

public class AbrirCaixaDTO {
    private BigDecimal valorInicial;
    private String observacoes;

    public BigDecimal getValorInicial() { return valorInicial; }
    public void setValorInicial(BigDecimal valorInicial) { this.valorInicial = valorInicial; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}

