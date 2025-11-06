package com.example.pdv_galeteria.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "combo_item")
public class ComboItem {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_combo", nullable = false)
    private Combo combo;

 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private Integer quantidade; 
}
