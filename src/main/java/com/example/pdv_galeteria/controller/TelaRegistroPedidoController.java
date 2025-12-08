package com.example.pdv_galeteria.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.pdv_galeteria.PdvGaleteriaApplication;

@Component
public class TelaRegistroPedidoController implements Initializable {

    @FXML
    private AnchorPane contentPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Inicializando TelaRegistroPedidoController...");
        System.out.println("✅ Tela de Nova Venda carregada com sucesso");
    }

    @FXML
    private void abrirTelaEstoque() {
        try {
            System.out.println("Abrindo tela de estoque...");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaProdutos.fxml"));

            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();

            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Estoque");
            stage.centerOnScreen();

            System.out.println("Tela de estoque aberta com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir tela de estoque: " + e.getMessage());
        }
    }

    @FXML
    private void registrarPedido() {
        System.out.println("Registrando pedido...");
        mostrarAlerta("Pedido Registrado",
                "Pedido registrado com sucesso! Total: R$ 119,00",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    private void buscarProdutos() {
        System.out.println("Buscando produtos...");
        // Implementação futura de busca
    }

    @FXML
    private void adicionarProdutoAoCarrinho() {
        System.out.println("Adicionando produto ao carrinho...");
        // Implementação futura
    }

    @FXML
    private void incrementarQuantidade() {
        System.out.println("Incrementando quantidade...");
        // Implementação futura
    }

    @FXML
    private void decrementarQuantidade() {
        System.out.println("Decrementando quantidade...");
        // Implementação futura
    }

    // === MÉTODO DE SAIR PARA LOGIN ===
    @FXML
    private void sairParaLogin() {
        try {
            System.out.println("Iniciando processo de saída para login...");

            System.out.println("Iniciando processo de saída para login...");
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaSairPrograma.fxml"));

            Parent root = loader.load();
            ConfirmacaoSaidaController controller = loader.getController();

            Stage popupStage = new Stage();
            controller.setPopupStage(popupStage);

            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Confirmação de Saída");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(contentPane.getScene().getWindow());
            popupStage.setResizable(false);
            popupStage.centerOnScreen();

            popupStage.showAndWait();

            if (controller.isConfirmado()) {
                System.out.println("✅ Usuário confirmou saída, voltando para login...");
                voltarParaTelaLogin();
            } else {
                System.out.println("❌ Usuário cancelou a saída.");
            }

        } catch (Exception e) {
            System.err.println("❌ Erro ao abrir pop-up de confirmação: " + e.getMessage());
            e.printStackTrace();

            // Fallback: usar alerta de confirmação simples
            usarFallbackConfirmacao();
        }
    }

    // === FALLBACK CASO O POPUP FALHE ===
    private void usarFallbackConfirmacao() {
        System.out.println("🔄 Usando fallback de confirmação...");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação de Saída");
        alert.setHeaderText("Deseja realmente sair?");
        alert.setContentText("Você será redirecionado para a tela de login.");

        // Personalizar botões
        ButtonType btnSim = new ButtonType("Sim, Sair", ButtonBar.ButtonData.YES);
        ButtonType btnNao = new ButtonType("Cancelar", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(btnSim, btnNao);

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == btnSim) {
            System.out.println("✅ Usuário confirmou saída no fallback");
            voltarParaTelaLogin();
        } else {
            System.out.println("❌ Usuário cancelou saída no fallback");
        }
    }

    // === VOLTAR PARA TELA DE LOGIN ===
    private void voltarParaTelaLogin() {
        try {
            System.out.println("🔄 Iniciando volta para tela de login...");

            Stage stage = (Stage) contentPane.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaLogin.fxml"));

            // Usar o ControllerFactory do Spring para garantir injeção de dependências
            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();

            System.out.println("✅ Tela de login carregada com sucesso!");

        } catch (Exception e) {
            System.err.println("❌ Erro ao voltar para login: " + e.getMessage());
            e.printStackTrace();

            // Tentar reiniciar a aplicação completamente
            reiniciarAplicacaoCompleta();
        }
    }

    // === REINICIAR APLICAÇÃO EM CASO DE ERRO ===
    private void reiniciarAplicacaoCompleta() {
        try {
            System.out.println("🔄 Tentando reiniciar aplicação completamente...");

            // Fechar a janela atual
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.close();

            // Usar o método de reinício da aplicação principal
            PdvGaleteriaApplication.relaunchApplication();

        } catch (Exception e) {
            System.err.println("💥 Erro ao reiniciar aplicação: " + e.getMessage());

            // Último recurso
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao reiniciar aplicação");
            alert.setContentText("Por favor, feche e abra o programa manualmente.");
            alert.showAndWait();

            javafx.application.Platform.exit();
        }
    }

    @FXML
    private void abrirDashboard() {
        System.out.println("Abrindo Dashboard...");
        mostrarAlerta("Funcionalidade em Desenvolvimento", "Dashboard estará disponível em breve!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void abrirCaixa() {
        System.out.println("Abrindo Caixa...");
        mostrarAlerta("Funcionalidade em Desenvolvimento", "Caixa estará disponível em breve!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void abrirEntregadores() {
        System.out.println("Abrindo Entregadores...");
        mostrarAlerta("Funcionalidade em Desenvolvimento", "Entregadores estará disponível em breve!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void abrirRelatorios() {
        System.out.println("Abrindo Relatórios...");
        mostrarAlerta("Funcionalidade em Desenvolvimento", "Relatórios estará disponível em breve!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void abrirConfiguracoes() {
        System.out.println("Abrindo Configurações...");
        mostrarAlerta("Funcionalidade em Desenvolvimento", "Configurações estará disponível em breve!", Alert.AlertType.INFORMATION);
    }

    // === MÉTODO AUXILIAR PARA MOSTRAR ALERTAS ===
    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML
    private void handleAbrirTelaCaixa(ActionEvent event) {
        try {
            // Carrega o FXML da tela do caixa
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaCaixa.fxml"));
            Parent root = loader.load();

            // Obtém o stage atual a partir do evento
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            // Troca a cena no mesmo stage
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Controle de Caixa");

        } catch (IOException e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir tela do caixa: " + e.getMessage());
        }
    }

    private void mostrarErro(String mensagem) {
        System.err.println(mensagem);
    }
}