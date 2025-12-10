package com.example.pdv_galeteria.dto;

public class EntradaEstoqueRequest {
    private Long produtoId;
    private Integer quantidade;
    private String observacao;

    // Construtor padrão
    public EntradaEstoqueRequest() {
    }

    // Construtor com parâmetros
    public EntradaEstoqueRequest(Long produtoId, Integer quantidade, String observacao) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.observacao = observacao;
    }

    // Getters e Setters MANUAIS
    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}