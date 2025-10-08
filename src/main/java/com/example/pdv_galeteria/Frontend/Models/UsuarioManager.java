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
            if (usuario.getEmail().equalsIgnoreCase(email)) {
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

    // NOVO MÉTODO: Verificar se email existe na lista de usuários cadastrados
    public static boolean verificarEmailExistente(String email) {
        for (Usuario usuario : usuarios) {
            if (usuario.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    // NOVO MÉTODO: Atualizar senha do usuário
    public static boolean atualizarSenha(String email, String novaSenha) {
        for (Usuario usuario : usuarios) {
            if (usuario.getEmail().equalsIgnoreCase(email)) {
                usuario.setSenha(novaSenha);
                return true;
            }
        }
        return false;
    }

    // MÉTODO AUXILIAR: Buscar usuário por email (pode ser útil para outras
    // funcionalidades)
    public static Usuario buscarUsuarioPorEmail(String email) {
        for (Usuario usuario : usuarios) {
            if (usuario.getEmail().equalsIgnoreCase(email)) {
                return usuario;
            }
        }
        return null;
    }

    // MÉTODO AUXILIAR: Ver quantidade de usuários cadastrados (para debug)
    public static int getQuantidadeUsuarios() {
        return usuarios.size();
    }
}