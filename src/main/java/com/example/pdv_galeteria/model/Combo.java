package com.example.pdv_galeteria.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "combos")
public class Combo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_combo")
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "preco_total", nullable = false)
    private Double precoTotal; 


    @OneToMany(mappedBy = "combo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComboItem> itensDoCombo;
}