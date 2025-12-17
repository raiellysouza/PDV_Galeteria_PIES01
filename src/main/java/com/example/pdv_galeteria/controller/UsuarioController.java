package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.service.UsuarioService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class UsuarioController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtSenha;

    @FXML
    private PasswordField txtConfirmaSenha;

    @FXML
    private Button btnCadastrar;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    public void initialize() {
        System.out.println("UsuarioController carregado! Controladores FXML funcionando.");

        btnCadastrar.setOnAction(event -> cadastrarUsuario());
    }

    private void cadastrarUsuario() {
        String login = txtUsuario.getText();
        String senha = txtSenha.getText();
        String confirma = txtConfirmaSenha.getText();

        try {
            if (login.isBlank() || senha.isBlank() || confirma.isBlank()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Campos obrigatórios", "Preencha todos os campos.");
                return;
            }

            if (!senha.equals(confirma)) {
                mostrarAlerta(Alert.AlertType.ERROR, "Senhas diferentes", "A confirmação de senha não coincide.");
                return;
            }

            usuarioService.cadastrar(login, senha);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Usuário cadastrado com sucesso!");

            limparCampos();

        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao cadastrar", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro inesperado", "Ocorreu um erro ao cadastrar o usuário.");
            e.printStackTrace();
        }
    }

    private void limparCampos() {
        txtUsuario.clear();
        txtSenha.clear();
        txtConfirmaSenha.clear();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);
        alert.show();
    }

    @FXML
    private void voltarParaLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaLogin.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Scene loginScene = new Scene(root);
            Stage stage = (Stage) txtUsuario.getScene().getWindow();

            stage.setWidth(1400);
            stage.setHeight(750);
            stage.centerOnScreen();

            stage.setScene(loginScene);
            stage.setTitle("Login");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar a tela de login.");
        }
    }
}