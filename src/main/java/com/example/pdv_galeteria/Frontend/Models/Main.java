package com.example.pdv_galeteria.Frontend.Models;

import com.example.pdv_galeteria.PdvGaleteriaApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

public class Main extends Application {

    private ApplicationContext applicationContext;

    @Override
    public void init() throws Exception {
        applicationContext = new SpringApplicationBuilder(PdvGaleteriaApplication.class)
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
         applicationContext.publishEvent(new StageReadyEvent(primaryStage));
    }

    @Override
    public void stop() throws Exception {
        ((Stage) applicationContext).close();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}