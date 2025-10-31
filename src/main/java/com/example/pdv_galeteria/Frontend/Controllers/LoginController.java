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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import java.io.IOException;

@Controller
public class LoginController {

    @FXML
    private TextField campoUsuario;

    @FXML
    private PasswordField campoSenha;

    @Autowired
    private ApplicationContext applicationContext;

    private static final String USUARIO_PADRAO = "admin";
    private static final String SENHA_PADRAO = "admin";

    @FXML
    private void entrarNoSistema(ActionEvent event) {
        String usuario = campoUsuario.getText();
        String senha = campoSenha.getText();

        if (usuario.isEmpty() || senha.isEmpty()) {
            mostrarAlerta("Erro", "Por favor, preencha todos os campos!");
            return;
        }

        if (USUARIO_PADRAO.equals(usuario) && SENHA_PADRAO.equals(senha)) {
            try {
                // AQUI É ONDE VOCÊ COLOCA O setControllerFactory
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaProdutos.fxml"));
                loader.setControllerFactory(applicationContext::getBean); // ← AQUI

                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setMaximized(true);
                stage.setTitle("Galeteria do Irmão - Produtos");
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Erro", "Erro ao carregar a tela de produtos:\n" + e.getMessage());
            }
        } else {
            mostrarAlerta("Erro de Login",
                    "Usuário ou senha incorretos!\n\n" +
                            "Use:\n" +
                            "Usuário: admin\n" +
                            "Senha: admin");

            campoSenha.setText("");
            campoUsuario.requestFocus();
        }
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}