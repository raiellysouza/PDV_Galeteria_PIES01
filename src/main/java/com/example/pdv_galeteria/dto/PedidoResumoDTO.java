package com.example.pdv_galeteria.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PedidoResumoDTO {
    private Long id;
    private String cliente;
    private String produtos;
    private BigDecimal valor;
    private String formaPagamento;
    private LocalDateTime horario;

    public PedidoResumoDTO() {}

    public PedidoResumoDTO(Long id, String cliente, String produtos,
                           BigDecimal valor, String formaPagamento, LocalDateTime horario) {
        this.id = id;
        this.cliente = cliente;
        this.produtos = produtos;
        this.valor = valor;
        this.formaPagamento = formaPagamento;
        this.horario = horario;
    }

    public PedidoResumoDTO(Long id, String cliente, String produtos, BigDecimal bigDecimal, String valorFormatado, String formaPagamento, String horaFormatada, LocalDateTime criadoEm) {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getProdutos() {
        return produtos;
    }

    public void setProdutos(String produtos) {
        this.produtos = produtos;
    }

    public BigDecimal getValor() {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getFormaPagamento() {
        return formaPagamento != null ? formaPagamento : "Não informado";
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public LocalDateTime getHorario() {
        return horario;
    }

    public void setHorario(LocalDateTime horario) {
        this.horario = horario;
    }

    public String getValorFormatado() {
        return String.format("R$ %.2f", getValor().doubleValue());
    }

    public String getHoraFormatada() {
        if (horario == null) return "00:00";
        return String.format("%02d:%02d", horario.getHour(), horario.getMinute());
    }

    @Override
    public String toString() {
        return "PedidoResumoDTO{" +
                "id=" + id +
                ", cliente='" + cliente + '\'' +
                ", produtos='" + produtos + '\'' +
                ", valor=" + getValorFormatado() +
                ", formaPagamento='" + formaPagamento + '\'' +
                ", horario=" + (horario != null ? horario.toString() : "null") +
                '}';
    }
}