package com.example.pdv_galeteria.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Table(name = "combos")
public class Combo {

    public Combo() {
    }

    public Combo(String nome, Double precoTotal) {
        this.nome = nome;
        this.precoTotal = precoTotal;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_combo")
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "preco", nullable = false)
    private Double precoTotal;

    @OneToMany(mappedBy = "combo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComboItem> itensDoCombo;

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

    public Double getPrecoTotal() {
        return precoTotal;
    }

    public void setPrecoTotal(Double precoTotal) {
        this.precoTotal = precoTotal;
    }

    public BigDecimal getPrecoTotalAsBigDecimal() {
        return BigDecimal.valueOf(precoTotal);
    }

    public List<ComboItem> getItensDoCombo() {
        return itensDoCombo;
    }

    public void setItensDoCombo(List<ComboItem> itensDoCombo) {
        this.itensDoCombo = itensDoCombo;
    }

    @Override
    public String toString() {
        return nome + " - R$ " + String.format("%.2f", precoTotal);
    }
}