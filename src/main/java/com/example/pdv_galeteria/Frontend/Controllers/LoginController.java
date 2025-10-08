package com.example.pdv_galeteria.Frontend.Controllers;

import java.io.File;
import java.io.IOException;

import com.example.pdv_galeteria.Frontend.Models.Usuario;
import com.example.pdv_galeteria.Frontend.Models.UsuarioManager;

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

public class LoginController {
    @FXML
    private TextField campoUsuario;
    @FXML
    private PasswordField campoSenha;

    @FXML
    private void mudarPagina(ActionEvent event) throws IOException {
        String usuario = campoUsuario.getText();
        String senha = campoSenha.getText();

        // Tenta fazer login
        Usuario usuarioLogado = UsuarioManager.fazerLogin(usuario, senha);

        if (usuarioLogado != null) {
            // Login bem-sucedido
            mostrarSucesso("Login realizado com sucesso!\n\nBem-vindo, " + usuarioLogado.getNome() + "!");

            // Vai para tela inicial
            Parent root = FXMLLoader.load(
                    new File("src/main/java/com/example/pdv_galeteria/Frontend/views/TelaInicial.fxml").toURI()
                            .toURL());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } else {
            // Login falhou
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro de Login");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Email ou senha incorretos!\n\nCadastre-se ou tente novamente.");
            alert.showAndWait();

            // Limpa os campos
            campoUsuario.setText("");
            campoSenha.setText("");
            campoUsuario.requestFocus();
        }
    }

    @FXML
    private void abrirCadastro(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(
                new File("src/main/java/com/example/pdv_galeteria/Frontend/views/Cadastro.fxml").toURI().toURL());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    // NOVO MÉTODO: Abrir tela de recuperação de senha
    @FXML
    private void abrirRecuperarSenha(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(
                new File("src/main/java/com/example/pdv_galeteria/Frontend/views/RecuperarSenha.fxml").toURI().toURL());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void mostrarSucesso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Bem-sucedido");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}