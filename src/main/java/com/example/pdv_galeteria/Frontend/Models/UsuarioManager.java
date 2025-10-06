package com.example.pdv_galeteria.Frontend.Models;

import java.util.ArrayList;
import java.util.List;

public class UsuarioManager {
    private static List<Usuario> usuarios = new ArrayList<>();

    // Usuário padrão para testes (opcional)
    // static {
    // usuarios.add(new Usuario("Administrador", "admin@gmail.com", "1234"));
    // }

    public static boolean cadastrarUsuario(String nome, String email, String senha) {
        // Verifica se o email já existe
        for (Usuario usuario : usuarios) {
            if (usuario.getEmail().equals(email)) {
                return false; // Email já cadastrado
            }
        }

        // Adiciona novo usuário
        usuarios.add(new Usuario(nome, email, senha));
        return true;
    }

    public static Usuario fazerLogin(String email, String senha) {
        for (Usuario usuario : usuarios) {
            if (usuario.verificarLogin(email, senha)) {
                return usuario;
            }
        }
        return null; // Login falhou
    }

    public static List<Usuario> getUsuarios() {
        return new ArrayList<>(usuarios);
    }
}