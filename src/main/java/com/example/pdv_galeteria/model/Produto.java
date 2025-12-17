package com.example.pdv_galeteria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private Double preco;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    public Produto() {
        this.preco = 0.0;
        this.quantidade = 0;
        this.ativo = true;
        this.categoria = null;

    }

    public Produto(String nome, Double preco, Integer quantidade) {
        this();
        this.nome = nome;
        this.preco = preco != null ? preco : 0.0;
        this.quantidade = quantidade != null ? quantidade : 0;
        this.ativo = true;
    }

    public Produto(String nome, BigDecimal preco, Integer quantidade) {
        this();
        this.nome = nome;
        this.preco = preco != null ? preco.doubleValue() : 0.0;
        this.quantidade = quantidade != null ? quantidade : 0;
        this.ativo = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Double getPreco() {
        return preco != null ? preco : 0.0;
    }

    public void setPreco(Double preco) {
        this.preco = preco != null ? preco : 0.0;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco != null ? preco.doubleValue() : 0.0;
    }

    public Integer getQuantidade() {
        return quantidade != null ? quantidade : 0;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade != null ? quantidade : 0;
    }

    public Boolean getAtivo() {
        return ativo != null ? ativo : true;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo != null ? ativo : true;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        return nome + " - R$ " + String.format("%.2f", getPreco());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Produto produto = (Produto) o;
        return id != null && id.equals(produto.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}