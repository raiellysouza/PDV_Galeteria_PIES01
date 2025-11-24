package com.example.pdv_galeteria.dto;

import com.example.pdv_galeteria.model.StatusCaixa;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CaixaResponseDTO {
    private Long id;
    private LocalDate dataCaixa;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private BigDecimal valorInicial;
    private BigDecimal valorFinal;
    private StatusCaixa status;
    private BigDecimal saldoAtual;
    private BigDecimal totalEntradas;
    private BigDecimal totalSaidas;
    private String observacoes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDataCaixa() { return dataCaixa; }
    public void setDataCaixa(LocalDate dataCaixa) { this.dataCaixa = dataCaixa; }

    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }

    public LocalDateTime getDataFechamento() { return dataFechamento; }
    public void setDataFechamento(LocalDateTime dataFechamento) { this.dataFechamento = dataFechamento; }

    public BigDecimal getValorInicial() { return valorInicial; }
    public void setValorInicial(BigDecimal valorInicial) { this.valorInicial = valorInicial; }

    public BigDecimal getValorFinal() { return valorFinal; }
    public void setValorFinal(BigDecimal valorFinal) { this.valorFinal = valorFinal; }

    public StatusCaixa getStatus() { return status; }
    public void setStatus(StatusCaixa status) { this.status = status; }

    public BigDecimal getSaldoAtual() { return saldoAtual; }
    public void setSaldoAtual(BigDecimal saldoAtual) { this.saldoAtual = saldoAtual; }

    public BigDecimal getTotalEntradas() { return totalEntradas; }
    public void setTotalEntradas(BigDecimal totalEntradas) { this.totalEntradas = totalEntradas; }

    public BigDecimal getTotalSaidas() { return totalSaidas; }
    public void setTotalSaidas(BigDecimal totalSaidas) { this.totalSaidas = totalSaidas; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
