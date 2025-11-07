package com.example.pdv_galeteria;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import javafx.application.Application;
import javafx.application.Platform; // ADICIONE ESTE IMPORT
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@SpringBootApplication
public class PdvGaleteriaApplication extends Application {

    private static ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        System.out.println("=== INICIALIZANDO SPRING BOOT ===");
        springContext = new SpringApplicationBuilder(PdvGaleteriaApplication.class)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("=== INICIANDO JAVAFX ===");

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaLogin.fxml"));
        loader.setControllerFactory(springContext::getBean);

        Parent root = loader.load();
        primaryStage.setTitle("Galeteria do Irmão - PDV");
        primaryStage.setScene(new Scene(root, 1350, 700));
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();

        System.out.println("✅ Spring Boot + JavaFX integrados com sucesso!");
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
        System.out.println("=== APLICAÇÃO FINALIZADA ===");
    }

    public static void main(String[] args) {
        System.out.println("🚀 Iniciando PDV Galeteria...");
        launch(args);
    }

    // Método estático para acessar o contexto do Spring
    public static ConfigurableApplicationContext getSpringContext() {
        return springContext;
    }

    // Método de debug estático
    public static void debugSpringContext() {
        if (springContext != null) {
            System.out.println("=== DEBUG SPRING CONTEXT ===");
            System.out.println("Contexto ativo: " + springContext.isActive());
            System.out.println("Beans do ProdutoService: " + springContext.getBeanNamesForType(com.example.pdv_galeteria.service.ProdutoService.class).length);
            System.out.println("Beans do TelaProdutosController: " + springContext.getBeanNamesForType(com.example.pdv_galeteria.controller.TelaProdutosController.class).length);
        } else {
            System.out.println("❌ Spring Context é nulo!");
        }
    }

    // Método corrigido para reiniciar a aplicação
    public static void relaunchApplication() {
        try {
            System.out.println("Reiniciando aplicação...");

            // Fechar o contexto Spring atual
            if (springContext != null && springContext.isActive()) {
                springContext.close(); // CORRIGIDO: springContext em vez de context
            }

            // Recriar a aplicação
            Platform.runLater(() -> {
                try {
                    System.out.println("Criando nova instância da aplicação...");
                    launch(new String[]{});
                } catch (Exception e) {
                    System.err.println("Erro ao reiniciar aplicação: " + e.getMessage());
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            System.err.println("Erro no relaunchApplication: " + e.getMessage());
            e.printStackTrace();
        }
    }
}