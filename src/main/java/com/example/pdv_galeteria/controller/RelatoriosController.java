package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.PdvGaleteriaApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

@Controller
public class RelatoriosController {

    @FXML
    private javafx.scene.control.Button sairButton;

    @FXML
    private void initialize() {
    }

    public void sairParaLogin(ActionEvent actionEvent) {
        try {
            System.out.println("Abrindo pop-up de confirmação de saída...");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaSairPrograma.fxml"));

            Parent root = loader.load();

            if (loader.getController() instanceof ConfirmacaoSaidaController) {
                ConfirmacaoSaidaController controller = loader.getController();

                Stage popupStage = new Stage();
                controller.setPopupStage(popupStage);

                popupStage.setScene(new Scene(root));
                popupStage.setTitle("Confirmação de Saída");
                popupStage.initModality(Modality.APPLICATION_MODAL);

                Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                popupStage.initOwner(currentStage);
                popupStage.setResizable(false);
                popupStage.centerOnScreen();

                popupStage.showAndWait();

                if (controller.isConfirmado()) {
                    System.out.println("Usuário confirmou saída, voltando para login...");
                    voltarParaTelaLogin(actionEvent);
                } else {
                    System.out.println("Usuário cancelou a saída.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir pop-up de confirmação: " + e.getMessage());

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmação de Saída");
            alert.setHeaderText("Deseja realmente sair?");
            alert.setContentText("Você será redirecionado para a tela de login.");

            Optional<javafx.scene.control.ButtonType> resultado = alert.showAndWait();
            if (resultado.isPresent() && resultado.get() == javafx.scene.control.ButtonType.OK) {
                voltarParaTelaLogin(actionEvent);
            }
        }
    }

    private void voltarParaTelaLogin(ActionEvent actionEvent) {
        try {
            System.out.println("Iniciando processo de volta para login...");

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

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
            e.printStackTrace();
            System.err.println("Erro ao voltar para login: " + e.getMessage());

            reiniciarAplicacaoCompleta(actionEvent);
        }
    }

    private void reiniciarAplicacaoCompleta(ActionEvent actionEvent) {
        try {
            System.out.println("Tentando reiniciar aplicação completamente...");

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
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

    public void abrirTelaDashboard(ActionEvent actionEvent) {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaDashBoard.fxml", "Dashboard", actionEvent);
    }

    public void abrirTelaEstoque(ActionEvent actionEvent) {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaProdutos.fxml", "Estoque", actionEvent);
    }

    public void abrirTelaCaixa(ActionEvent actionEvent) {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaCaixa.fxml", "Controle de Caixa", actionEvent);
    }

    public void abrirTelaVendas(ActionEvent actionEvent) {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaRegistroPedido.fxml", "Registro de Pedidos", actionEvent);
    }

    public void abrirTelaEntregadores(ActionEvent actionEvent) {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaEntregadores.fxml", "Entregadores", actionEvent);
    }

    public void abrirTelaConfiguracoes(ActionEvent actionEvent) {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaConfiguracao.fxml", "Configurações", actionEvent);
    }

    private void navegarParaTela(String fxmlPath, String titulo, ActionEvent actionEvent) {
        try {
            System.out.println("Abrindo " + titulo + "...");

            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("Arquivo FXML não encontrado: " + fxmlPath);
                mostrarMensagemErro("Arquivo da tela não encontrado: " + titulo);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.centerOnScreen();

            System.out.println(titulo + " aberto com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro ao abrir " + titulo + ": " + e.getMessage());
            e.printStackTrace();

            String errorDetails = "Erro ao abrir " + titulo + ":\n";
            if (e.getCause() != null) {
                errorDetails += "Causa: " + e.getCause().getMessage();
            } else {
                errorDetails += e.getMessage();
            }
            mostrarMensagemErro(errorDetails);
        }
    }

    private void mostrarMensagemErro(String mensagem) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText(mensagem);
            alert.showAndWait();
        });
    }

    private void mostrarMensagemSucesso(String mensagem) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sucesso");
            alert.setHeaderText(null);
            alert.setContentText(mensagem);
            alert.showAndWait();
        });
    }
}