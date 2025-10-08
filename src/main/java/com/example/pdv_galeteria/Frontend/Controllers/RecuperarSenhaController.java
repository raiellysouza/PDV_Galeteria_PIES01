package com.example.pdv_galeteria.Frontend.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class RecuperarSenhaController {

    @FXML
    private TextField campoEmail;
    @FXML
    private PasswordField campoSenhaNova;
    @FXML
    private PasswordField campoConfirmarSenha;

    // Regex para validar formato de email
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @FXML
    private void recuperarSenha(ActionEvent event) {
        String email = campoEmail.getText().trim();
        String senhaNova = campoSenhaNova.getText();
        String confirmarSenha = campoConfirmarSenha.getText();

        // Validações
        if (email.isEmpty()) {
            mostrarAlerta("Erro", "Email não pode estar vazio!");
            campoEmail.requestFocus();
            return;
        }

        if (!validarEmail(email)) {
            mostrarAlerta("Erro", "Por favor, digite um email válido!");
            campoEmail.requestFocus();
            return;
        }

        if (senhaNova.isEmpty()) {
            mostrarAlerta("Erro", "Nova senha não pode estar vazia!");
            campoSenhaNova.requestFocus();
            return;
        }

        if (confirmarSenha.isEmpty()) {
            mostrarAlerta("Erro", "Confirmação de senha não pode estar vazia!");
            campoConfirmarSenha.requestFocus();
            return;
        }

        if (!senhaNova.equals(confirmarSenha)) {
            mostrarAlerta("Erro", "As senhas não coincidem!");
            campoSenhaNova.clear();
            campoConfirmarSenha.clear();
            campoSenhaNova.requestFocus();
            return;
        }

        if (senhaNova.length() < 4) {
            mostrarAlerta("Erro", "A senha deve ter pelo menos 4 caracteres!");
            campoSenhaNova.requestFocus();
            return;
        }

        // Verificar se o email existe nos usuários cadastrados
        boolean emailExiste = verificarEmailCadastrado(email);

        if (emailExiste) {
            // Atualizar a senha no sistema
            boolean senhaAtualizada = atualizarSenhaNoSistema(email, senhaNova);

            if (senhaAtualizada) {
                mostrarAlerta("Sucesso",
                        "Senha atualizada com sucesso!\n\n" +
                                "Agora você pode fazer login com sua nova senha.");
                limparCampos();
                voltarLogin(event);
            } else {
                mostrarAlerta("Erro", "Erro ao atualizar a senha. Tente novamente.");
            }
        } else {
            mostrarAlerta("Erro", "Email não encontrado em nossa base de dados!\n\n" +
                    "Verifique se digitou o email corretamente ou cadastre-se primeiro.");
            campoEmail.requestFocus();
        }
    }

    @FXML
    private void voltarLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    new File("src/main/java/com/example/pdv_galeteria/Frontend/views/TelaLogin.fxml").toURI().toURL());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao voltar para login: " + e.getMessage());
        }
    }

    private boolean validarEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    // NOVO MÉTODO: Verificar se email existe nos usuários cadastrados
    private boolean verificarEmailCadastrado(String email) {
        // Usar o UsuarioManager para verificar se o email existe
        // Isso vai verificar na lista real de usuários cadastrados
        return com.example.pdv_galeteria.Frontend.Models.UsuarioManager.verificarEmailExistente(email);
    }

    // MÉTODO ATUALIZADO: Agora usa o UsuarioManager para atualizar a senha
    private boolean atualizarSenhaNoSistema(String email, String novaSenha) {
        // Usar o UsuarioManager para atualizar a senha do usuário
        return com.example.pdv_galeteria.Frontend.Models.UsuarioManager.atualizarSenha(email, novaSenha);
    }

    private void limparCampos() {
        campoEmail.clear();
        campoSenhaNova.clear();
        campoConfirmarSenha.clear();
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}