package com.example.pdv_galeteria.Frontend.Controllers;

import java.io.File;
import java.io.IOException;

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
    @SuppressWarnings("unused")
    private void mudarPagina(ActionEvent event) throws IOException {
        String usuario = campoUsuario.getText();
        String senha = campoSenha.getText();

        // Validação simples - usuário e senha temporários
        if ("admin".equals(usuario) && "1234".equals(senha)) {
            // Login bem-sucedido - vai para tela inicial
            Parent root = FXMLLoader.load(
                    new File("src/main/java/com/example/pdv_galeteria/Frontend/views/TelaInicial.fxml").toURI()
                            .toURL());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } else {
            // Login falhou - mostra mensagem de erro
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro de Login");
            alert.setHeaderText(null);
            alert.setContentText("Usuário ou senha incorretos!\nUse: admin / 1234");
            alert.showAndWait();

            // Limpa os campos
            campoUsuario.setText("");
            campoSenha.setText("");
            campoUsuario.requestFocus(); // Volta o foco para o campo de usuário
        }
    }
}
