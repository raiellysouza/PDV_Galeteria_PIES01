package com.example.pdv_galeteria.model;

import jakarta.persistence.*;
import lombok.Data;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;


@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String login;


    @Column(nullable = false)
    private String senha;


    @Column(nullable = false, unique = true, length = 16)
    private String codigoRecuperacao;

    public Usuario() {
        this.codigoRecuperacao = gerarCodigoRecuperacao();
    }

    public Usuario(String login, String senhaCriptografada) {
        this.login = login;
        this.senha = senhaCriptografada;
        this.codigoRecuperacao = gerarCodigoRecuperacao();
    }

    private String gerarCodigoRecuperacao() {
        byte[] random = new byte[6]; 
        new SecureRandom().nextBytes(random);
        String base64 = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        return base64;
    }


    public String regenerarCodigo() {
        this.codigoRecuperacao = gerarCodigoRecuperacao();
        return this.codigoRecuperacao;
    }
    
}
