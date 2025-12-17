package com.example.pdv_galeteria.model;

import com.example.pdv_galeteria.model.UsuarioSessao;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessaoUtils {

    @Autowired
    private UsuarioSessao usuarioSessao;

    public void atualizarNomeUsuario(Label labelNomeUsuario) {
        if (labelNomeUsuario != null && usuarioSessao != null) {
            String nome = usuarioSessao.getNomeUsuario();
            System.out.println("Atualizando label para: " + nome);
            labelNomeUsuario.setText(nome);
        }
    }

    public String getNomeUsuario() {
        return usuarioSessao != null ? usuarioSessao.getNomeUsuario() : "Usuário";
    }
}