package com.example.pdv_galeteria.Frontend.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import com.example.pdv_galeteria.Frontend.Models.UsuarioManager;

public class CadastroController {

    @FXML
    private TextField campoNome;

    @FXML
    private TextField campoEmail;

    @FXML
    private PasswordField campoSenha;

    @FXML
    private PasswordField campoConfirmarSenha;

    @FXML
    private void cadastrarUsuario(ActionEvent event) {
        String nome = campoNome.getText();
        String email = campoEmail.getText();
        String senha = campoSenha.getText();
        String confirmarSenha = campoConfirmarSenha.getText();

        // Validações básicas
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
            mostrarErro("Todos os campos são obrigatórios!");
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            mostrarErro("As senhas não coincidem!");
            campoSenha.setText("");
            campoConfirmarSenha.setText("");
            campoSenha.requestFocus();
            return;
        }

        if (senha.length() < 4) {
            mostrarErro("A senha deve ter pelo menos 4 caracteres!");
            campoSenha.setText("");
            campoConfirmarSenha.setText("");
            campoSenha.requestFocus();
            return;
        }

        if (!email.contains("@")) {
            mostrarErro("Por favor, insira um email válido!");
            campoEmail.requestFocus();
            return;
        }

        // Tenta cadastrar o usuário
        boolean cadastrado = UsuarioManager.cadastrarUsuario(nome, email, senha);

        if (cadastrado) {
            mostrarSucesso("Cadastro realizado com sucesso!\n\nNome: " + nome + "\nEmail: " + email
                    + "\n\nAgora você pode fazer login!");
            limparCampos();

            // Salva a referência do stage ANTES do timer
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Volta para a tela de login após 2 segundos
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            javafx.application.Platform.runLater(() -> {
                                voltarParaLogin(stage);
                            });
                        }
                    },
                    2000);
        } else {
            mostrarErro("Este email já está cadastrado!\nUse outro email ou faça login.");
            campoEmail.requestFocus();
        }
    }

    @FXML
    private void voltarParaLogin(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        voltarParaLogin(stage);
    }

    private void voltarParaLogin(Stage stage) {
        try {
            Parent root = FXMLLoader.load(
                    new File("src/main/java/com/example/pdv_galeteria/Frontend/views/TelaLogin.fxml").toURI().toURL());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void limparCampos() {
        campoNome.setText("");
        campoEmail.setText("");
        campoSenha.setText("");
        campoConfirmarSenha.setText("");
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro no Cadastro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarSucesso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cadastro Realizado");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}