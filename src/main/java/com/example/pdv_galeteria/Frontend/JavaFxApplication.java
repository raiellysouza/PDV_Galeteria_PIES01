package com.example.pdv_galeteria.Frontend;

import com.example.pdv_galeteria.Frontend.Models.StageReadyEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;

@Component
public class JavaFXApplication implements ApplicationListener<StageReadyEvent> {

    private final ApplicationContext applicationContext;

    public JavaFXApplication(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        try {
            Stage stage = event.getStage();
        
            File fxmlFile = new File("src/main/java/com/example/pdv_galeteria/Frontend/views/TelaLogin.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlFile.toURI().toURL());
            
            fxmlLoader.setControllerFactory(applicationContext::getBean); 

            Parent root = fxmlLoader.load();
            
            stage.setTitle("Galeteria do Irmão - Login");
            stage.setScene(new Scene(root, 1350, 700));
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Falha ao carregar a tela de login (FXML).", e);
        }
    }
}