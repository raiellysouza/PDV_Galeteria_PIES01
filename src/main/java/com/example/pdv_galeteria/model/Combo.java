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
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private Double preco;
    

    @ManyToMany 
    @JoinTable(
        name = "combos_produtos",
        joinColumns = @JoinColumn(name = "combo_id"), 
        inverseJoinColumns = @JoinColumn(name = "produto_id") 
    )
    private List<Produto> produtos;
}