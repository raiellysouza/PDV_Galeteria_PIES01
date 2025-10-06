package com.example.pdv_galeteria.Frontend.Models;

import java.io.File;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carrega a tela de login do FXML
        FXMLLoader loader = new FXMLLoader(
                new File("src/main/java/com/example/pdv_galeteria/Frontend/views/TelaLogin.fxml").toURI().toURL());
        Parent root = loader.load();

        Scene scene = new Scene(root, 1350, 700);
        primaryStage.setTitle("PDV Galeteria - Sistema de Vendas");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}