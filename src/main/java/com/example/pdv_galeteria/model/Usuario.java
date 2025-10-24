package com.example.pdv_galeteria.model;

public class Usuario {
    
}package com.example.pdv_galeteria.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) 
    private String nome;

    @Column(nullable = false)
    private String senha;
}
