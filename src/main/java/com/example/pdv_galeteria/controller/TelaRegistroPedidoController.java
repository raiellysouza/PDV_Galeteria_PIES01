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
        System.out.println("Tela de Nova Venda carregada com sucesso");
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
    }

    @FXML
    private void adicionarProdutoAoCarrinho() {
        System.out.println("Adicionando produto ao carrinho...");
    }

    @FXML
    private void incrementarQuantidade() {
        System.out.println("Incrementando quantidade...");
    }

    @FXML
    private void decrementarQuantidade() {
        System.out.println("Decrementando quantidade...");
    }

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
                System.out.println("Usuário confirmou saída, voltando para login...");
                voltarParaTelaLogin();
            } else {
                System.out.println("Usuário cancelou a saída.");
            }

        } catch (Exception e) {
            System.err.println("Erro ao abrir pop-up de confirmação: " + e.getMessage());
            e.printStackTrace();

            usarFallbackConfirmacao();
        }
    }

    private void usarFallbackConfirmacao() {
        System.out.println("Usando fallback de confirmação...");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação de Saída");
        alert.setHeaderText("Deseja realmente sair?");
        alert.setContentText("Você será redirecionado para a tela de login.");

        ButtonType btnSim = new ButtonType("Sim, Sair", ButtonBar.ButtonData.YES);
        ButtonType btnNao = new ButtonType("Cancelar", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(btnSim, btnNao);

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == btnSim) {
            System.out.println("Usuário confirmou saída no fallback");
            voltarParaTelaLogin();
        } else {
            System.out.println("Usuário cancelou saída no fallback");
        }
    }

    private void voltarParaTelaLogin() {
        try {
            System.out.println("Iniciando volta para tela de login...");

            Stage stage = (Stage) contentPane.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaLogin.fxml"));

            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();

            System.out.println("Tela de login carregada com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro ao voltar para login: " + e.getMessage());
            e.printStackTrace();

            reiniciarAplicacaoCompleta();
        }
    }

    private void reiniciarAplicacaoCompleta() {
        try {
            System.out.println("Tentando reiniciar aplicação completamente...");

            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.close();

            PdvGaleteriaApplication.relaunchApplication();

        } catch (Exception e) {
            System.err.println("Erro ao reiniciar aplicação: " + e.getMessage());

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao reiniciar aplicação");
            alert.setContentText("Por favor, feche e abra o programa manualmente.");
            alert.showAndWait();

            javafx.application.Platform.exit();
        }
    }

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaCaixa.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

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

    @FXML
    private void abrirTelaCaixa() {
        try {
            System.out.println("Abrindo tela de caixa...");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaCaixa.fxml"));

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Caixa");
            stage.centerOnScreen();

            System.out.println("Tela de caixa aberta com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir tela de caixa: " + e.getMessage());
            mostrarAlerta("Erro", "Não foi possível abrir a tela do caixa: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void abrirTelaEntregadores() {
        try {
            System.out.println("=== ABRINDO TELA ENTREGADORES ===");

            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaEntregadores.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERRO: Arquivo não encontrado!");

                return;
            }

            System.out.println("FXML encontrado: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            // Configurar Spring se disponível
            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
                System.out.println("Spring configurado");
            }

            System.out.println("Carregando FXML...");
            Parent root = loader.load();
            System.out.println("FXML carregado com sucesso!");

            Stage stage = null;


            if (contentPane != null && contentPane.getScene() != null) {
                stage = (Stage) contentPane.getScene().getWindow();
            }

            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle("Entregadores");
                stage.centerOnScreen();
                System.out.println("Tela de entregadores aberta com SUCESSO!");
            }

        } catch (Exception e) {
            System.err.println("=== ERRO AO ABRIR TELA ENTREGADORES ===");
            System.err.println("Mensagem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirTelaRelatorios() {
        try {
            System.out.println("Abrindo tela de relatórios...");

            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaRelatorios.fxml");
            if (fxmlUrl == null) {
                System.err.println("Arquivo TelaRelatorios.fxml não encontrado!");
                mostrarAlerta("Funcionalidade em Desenvolvimento",
                        "Tela de relatórios será implementada em breve!",
                        Alert.AlertType.INFORMATION);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Relatórios");
            stage.centerOnScreen();

            System.out.println("Tela de relatórios aberta com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir tela de relatórios: " + e.getMessage());
            mostrarAlerta("Funcionalidade em Desenvolvimento",
                    "Tela de relatórios será implementada em breve!",
                    Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void abrirTelaConfiguracoes() {
        try {
            System.out.println("Abrindo tela de configurações...");

            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaConfiguracoes.fxml");
            if (fxmlUrl == null) {
                System.err.println("Arquivo TelaConfiguracoes.fxml não encontrado!");
                mostrarAlerta("Funcionalidade em Desenvolvimento",
                        "Tela de configurações será implementada em breve!",
                        Alert.AlertType.INFORMATION);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Configurações");
            stage.centerOnScreen();

            System.out.println("Tela de configurações aberta com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir tela de configurações: " + e.getMessage());
            mostrarAlerta("Funcionalidade em Desenvolvimento",
                    "Tela de configurações será implementada em breve!",
                    Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void abrirTelaDashboard() {
        try {
            System.out.println("Abrindo tela de dashboard...");

            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaDashboard.fxml");
            if (fxmlUrl == null) {
                System.err.println("Arquivo TelaDashboard.fxml não encontrado!");
                mostrarAlerta("Funcionalidade em Desenvolvimento",
                        "Tela de dashboard será implementada em breve!",
                        Alert.AlertType.INFORMATION);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");
            stage.centerOnScreen();

            System.out.println("Tela de dashboard aberta com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir tela de dashboard: " + e.getMessage());
            mostrarAlerta("Funcionalidade em Desenvolvimento",
                    "Tela de dashboard será implementada em breve!",
                    Alert.AlertType.INFORMATION);
        }
    }
}