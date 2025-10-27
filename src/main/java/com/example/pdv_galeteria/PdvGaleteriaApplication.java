package com.example.pdv_galeteria;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@SpringBootApplication
public class PdvGaleteriaApplication extends Application {

	private ConfigurableApplicationContext springContext;

	@Override
	public void init() {
		System.out.println("=== INICIALIZANDO SPRING BOOT ===");
		this.springContext = new SpringApplicationBuilder(PdvGaleteriaApplication.class)
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
		this.springContext.close();
		System.out.println("=== APLICAÇÃO FINALIZADA ===");
	}

	public static void main(String[] args) {
		System.out.println("🚀 Iniciando PDV Galeteria...");
		launch(args); // ← CORRETO: método main do JavaFX
	}
}