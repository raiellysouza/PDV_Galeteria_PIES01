package com.example.pdv_galeteria.dto;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class ProdutoMaisVendidoDTO {
    private String nomeProduto;
    private String categoria;
    private int quantidadeVendida;
    private BigDecimal valorTotal;
    private BigDecimal valorMedio;

    public ProdutoMaisVendidoDTO(String nomeProduto, int quantidadeVendida, BigDecimal valorTotal) {
        this.nomeProduto = nomeProduto;
        this.categoria = "Não especificada";
        this.quantidadeVendida = quantidadeVendida;
        this.valorTotal = valorTotal != null ? valorTotal : BigDecimal.ZERO;
        this.valorMedio = quantidadeVendida > 0 ?
                this.valorTotal.divide(BigDecimal.valueOf(quantidadeVendida), 2, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;
    }

    public ProdutoMaisVendidoDTO(String nomeProduto, String categoria, int quantidadeVendida, BigDecimal valorTotal) {
        this.nomeProduto = nomeProduto;
        this.categoria = categoria != null ? categoria : "Não especificada";
        this.quantidadeVendida = quantidadeVendida;
        this.valorTotal = valorTotal != null ? valorTotal : BigDecimal.ZERO;
        this.valorMedio = quantidadeVendida > 0 ?
                this.valorTotal.divide(BigDecimal.valueOf(quantidadeVendida), 2, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getQuantidadeVendida() {
        return quantidadeVendida;
    }

    public String getQuantidadeVendidaFormatada() {
        return quantidadeVendida + " unidade" + (quantidadeVendida != 1 ? "s" : "");
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public String getNome() {
        return nomeProduto;
    }

    public String getValorTotalFormatado() {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return format.format(valorTotal);
    }

    public BigDecimal getValorMedio() {
        return valorMedio;
    }

    public String getValorMedioFormatado() {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return format.format(valorMedio);
    }

    @Override
    public String toString() {
        return "ProdutoMaisVendidoDTO{" +
                "nomeProduto='" + nomeProduto + '\'' +
                ", categoria='" + categoria + '\'' +
                ", quantidadeVendida=" + quantidadeVendida +
                ", valorTotal=" + getValorTotalFormatado() +
                '}';
    }
}