package com.example.pdv_galeteria.controller;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.example.pdv_galeteria.PdvGaleteriaApplication;
import com.example.pdv_galeteria.model.UsuarioSessao;
import com.example.pdv_galeteria.service.RelatorioService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

@Controller
public class RelatoriosController {

    @FXML
    private javafx.scene.control.Button sairButton;

    @Autowired
    private UsuarioSessao usuarioSessao;

    @Autowired
    private RelatorioService relatorioService;

    @FXML
    private Label labelNomeUsuario;

    @FXML
    private void initialize() {

        if (labelNomeUsuario != null && usuarioSessao != null) {
            labelNomeUsuario.setText(usuarioSessao.getNomeUsuario());
        }

        atualizarNomeUsuarioNoMenu();
    }

    private void atualizarNomeUsuarioNoMenu() {
        if (labelNomeUsuario != null && usuarioSessao != null) {
            String nome = usuarioSessao.getNomeUsuario();
            System.out.println("Atualizando nome do usuário: " + nome);
            labelNomeUsuario.setText(nome);
        } else {
            System.out.println("Erro: labelNomeUsuario ou usuarioSessao é nulo");
            System.out.println("labelNomeUsuario: " + (labelNomeUsuario != null ? "OK" : "NULO"));
            System.out.println("usuarioSessao: " + (usuarioSessao != null ? "OK" : "NULO"));
        }
    }

    @FXML
    private void gerarRelatorioVendas(ActionEvent event) {
        try {
            Path caminho = Paths.get("relatorios/relatorio_vendas.pdf");

            relatorioService.gerarRelatorioVendas(
                    LocalDate.now().minusDays(7),
                    LocalDate.now(),
                    caminho
            );

            mostrarMensagemSucesso("Relatório gerado com sucesso.");
        } catch (Exception e) {
            mostrarMensagemErro("Erro ao gerar relatório.");
        }
    }

    @FXML
    private void sairParaLogin() {
        try {
            System.out.println("Abrindo pop-up de confirmação de saída...");

            if (usuarioSessao != null) {
                usuarioSessao.logout();
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaSairPrograma.fxml"));

            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();
            ConfirmacaoSaidaController controller = loader.getController();

            Stage popupStage = new Stage();
            controller.setPopupStage(popupStage);

            Stage currentStage = (Stage) labelNomeUsuario.getScene().getWindow();

            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Confirmação de Saída");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(currentStage);
            popupStage.setResizable(false);
            popupStage.centerOnScreen();

            popupStage.showAndWait();

            if (controller.isConfirmado()) {
                voltarParaTelaLogin(currentStage);
            }
        } catch (Exception e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmação de Saída");
            alert.setHeaderText("Deseja realmente sair?");
            alert.setContentText("Você será redirecionado para a tela de login.");

            Optional<ButtonType> resultado = alert.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                if (usuarioSessao != null) {
                    usuarioSessao.logout();
                }
                voltarParaTelaLogin((Stage) alert.getDialogPane().getScene().getWindow());
            }
        }
    }

    private void voltarParaTelaLogin(Stage currentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaLogin.fxml"));

            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();

            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Login");
            currentStage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
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

    @FXML
    private void abrirTelaCaixa(ActionEvent event) {
        try {
            System.out.println("Abrindo tela do caixa...");

            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaCaixa.fxml");

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Controle de Caixa");
            stage.centerOnScreen();

            System.out.println("Tela do caixa aberta com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao abrir tela do caixa: " + e.getMessage());
            e.printStackTrace();
        }
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
