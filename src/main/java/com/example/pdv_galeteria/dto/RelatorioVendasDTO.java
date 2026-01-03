package com.example.pdv_galeteria.dto;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class RelatorioVendasDTO {
    private BigDecimal totalVendas;
    private long totalPedidos;
    private int quantidadeItens;
    private BigDecimal valorMedioPedido;

    public RelatorioVendasDTO(BigDecimal totalVendas, long totalPedidos) {
        this.totalVendas = totalVendas != null ? totalVendas : BigDecimal.ZERO;
        this.totalPedidos = totalPedidos;
        this.quantidadeItens = 0;
        this.valorMedioPedido = totalPedidos > 0 ?
                this.totalVendas.divide(BigDecimal.valueOf(totalPedidos), 2, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;
    }

    public RelatorioVendasDTO(BigDecimal totalVendas, long totalPedidos, int quantidadeItens) {
        this.totalVendas = totalVendas != null ? totalVendas : BigDecimal.ZERO;
        this.totalPedidos = totalPedidos;
        this.quantidadeItens = quantidadeItens;
        this.valorMedioPedido = totalPedidos > 0 ?
                this.totalVendas.divide(BigDecimal.valueOf(totalPedidos), 2, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;
    }

    public BigDecimal getTotalVendas() {
        return totalVendas;
    }

    public String getTotalVendasFormatado() {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return format.format(totalVendas);
    }

    public long getTotalPedidos() {
        return totalPedidos;
    }

    public String getTotalPedidosFormatado() {
        return totalPedidos + " pedido" + (totalPedidos != 1 ? "s" : "");
    }

    public int getQuantidadeItens() {
        return quantidadeItens;
    }

    public String getQuantidadeItensFormatada() {
        return quantidadeItens + " item" + (quantidadeItens != 1 ? "s" : "");
    }

    public BigDecimal getValorMedioPedido() {
        return valorMedioPedido;
    }

    public int getTotalItens() {
        return quantidadeItens;
    }

    public String getValorMedioPedidoFormatado() {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return format.format(valorMedioPedido);
    }

    @Override
    public String toString() {
        return "RelatorioVendasDTO{" +
                "totalVendas=" + getTotalVendasFormatado() +
                ", totalPedidos=" + totalPedidos +
                ", quantidadeItens=" + quantidadeItens +
                ", valorMedioPedido=" + getValorMedioPedidoFormatado() +
                '}';
    }
}