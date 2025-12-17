package com.example.pdv_galeteria.model;

import org.springframework.stereotype.Component;

@Component
public class UsuarioSessao {
    private String nomeUsuario;
    private Integer usuarioId;
    private boolean logado = false;

    public void login(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
        this.logado = true;
        System.out.println("Sessão iniciada para: " + nomeUsuario);
    }

    public void login(String nomeUsuario, Integer usuarioId) {
        this.nomeUsuario = nomeUsuario;
        this.usuarioId = usuarioId;
        this.logado = true;
        System.out.println("Sessão iniciada para: " + nomeUsuario + " (ID: " + usuarioId + ")");
    }

    public void logout() {
        this.nomeUsuario = null;
        this.usuarioId = null;
        this.logado = false;
        System.out.println("Sessão encerrada");
    }

    public String getNomeUsuario() {
        return nomeUsuario != null ? nomeUsuario : "Usuário";
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public boolean isLogado() {
        return logado;
    }
}