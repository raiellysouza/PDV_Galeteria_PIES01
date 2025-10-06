package com.example.pdv_galeteria.Frontend.Controllers;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class InicialController {

    @FXML
    private void voltarLogin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(
                new File("src/main/java/com/example/pdv_galeteria/Frontend/views/TelaLogin.fxml").toURI().toURL());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
