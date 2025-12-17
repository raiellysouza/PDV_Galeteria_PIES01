package com.example.pdv_galeteria.Frontend.Controllers;

import com.example.pdv_galeteria.model.UsuarioSessao;
import com.example.pdv_galeteria.service.UsuarioService;
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
    private UsuarioSessao usuarioSessao;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private void entrarNoSistema(ActionEvent event) {
        String usuario = campoUsuario.getText().trim();
        String senha = campoSenha.getText();

        if (usuario.isEmpty() || senha.isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Por favor, preencha todos os campos!");
            return;
        }

        try {
            boolean autenticado = usuarioService.autenticar(usuario, senha);

            if (autenticado) {
                usuarioSessao.login(usuario);
                System.out.println("Usuário autenticado com sucesso: " + usuario);

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaProdutos.fxml"));
                loader.setControllerFactory(applicationContext::getBean);

                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setMaximized(true);
                stage.setTitle("Galeteria do Irmão - Produtos");
                stage.show();

            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro de Login",
                        "Usuário ou senha incorretos!\n\n" +
                                "Verifique suas credenciais ou cadastre-se.");

                campoSenha.setText("");
                campoUsuario.requestFocus();
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Erro",
                    "Erro ao autenticar: " + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML
    private void abrirTelaCadastro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pdv_galeteria/Frontend/views/Tela-Cadastro-Usuario.fxml"));

            loader.setControllerFactory(applicationContext::getBean);

            Parent root = loader.load();

            Scene cadastroScene = new Scene(root);

            Stage stage = (Stage) campoUsuario.getScene().getWindow();

            stage.setWidth(1400);
            stage.setHeight(750);
            stage.centerOnScreen();

            stage.setScene(cadastroScene);
            stage.setTitle("Cadastro de Usuário");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar a tela de cadastro.");
        }
    }
}