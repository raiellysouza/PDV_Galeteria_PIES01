package com.example.pdv_galeteria.model;

import jakarta.persistence.*;
import java.security.SecureRandom;
import java.util.Base64;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getCodigoRecuperacao() {
        return codigoRecuperacao;
    }

    public void setCodigoRecuperacao(String codigoRecuperacao) {
        this.codigoRecuperacao = codigoRecuperacao;
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

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", senha='[PROTEGIDA]'" +
                ", codigoRecuperacao='" + codigoRecuperacao + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Usuario usuario = (Usuario) o;
        return id != null && id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}