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
        File fxmlFile = new File("src/main/java/com/example/pdv_galeteria/Frontend/views/TelaLogin.fxml");
        System.out.println("Carregando: " + fxmlFile.getAbsolutePath());
        System.out.println("Arquivo existe: " + fxmlFile.exists());

        Parent root = FXMLLoader.load(fxmlFile.toURI().toURL());
        primaryStage.setTitle("Galeteria do Irmão - Login");
        primaryStage.setScene(new Scene(root, 1350, 700));
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
