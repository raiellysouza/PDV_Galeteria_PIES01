package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.Caixa;
import com.example.pdv_galeteria.service.CaixaService;
import com.example.pdv_galeteria.PdvGaleteriaApplication;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class CaixaController implements Initializable {

    @Autowired
    private CaixaService caixaService;

    @FXML
    private Button btnAcaoCaixa;

    @FXML
    private TextField txtValorInicial;

    @FXML
    private TextArea txtObservacoes;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblSaldo;

    @FXML
    private Label lblTotalEntradas;

    @FXML
    private Label lblTotalSaidas;

    public void setCaixaService(CaixaService caixaService) {
        this.caixaService = caixaService;
    }

    public CaixaController() {
        System.out.println("CaixaController instanciado!");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("CaixaController.initialize() chamado");

        javafx.application.Platform.runLater(() -> {
            atualizarInterface();
        });
    }

    private void atualizarInterface() {
        System.out.println("atualizarInterface() chamado");

        if (btnAcaoCaixa == null) {
            System.err.println("btnAcaoCaixa é nulo! Verifique o FXML");
            return;
        }

        if (caixaService == null) {
            System.err.println("caixaService é nulo! A injeção do Spring falhou");
            btnAcaoCaixa.setText("Erro - Service");

            try {
                if (PdvGaleteriaApplication.getSpringContext() != null) {
                    caixaService = PdvGaleteriaApplication.getSpringContext().getBean(CaixaService.class);
                    System.out.println("CaixaService obtido manualmente do contexto Spring");
                }
            } catch (Exception e) {
                System.err.println("Falha ao obter CaixaService manualmente: " + e.getMessage());
            }
            return;
        }

        try {
            String statusTexto = caixaService.getStatusTextoBotao();
            btnAcaoCaixa.setText(statusTexto);

            boolean podeAbrir = caixaService.podeAbrirCaixa();
            if (txtValorInicial != null) {
                txtValorInicial.setDisable(!podeAbrir);
            }

            Optional<Caixa> caixaOpt = caixaService.getCaixaDoDia();
            if (caixaOpt.isPresent()) {
                Caixa caixa = caixaOpt.get();
                if (lblStatus != null) lblStatus.setText("Status: " + caixa.getStatus());
                if (lblSaldo != null) lblSaldo.setText("Saldo: R$ " + caixa.getSaldoAtual());
                if (lblTotalEntradas != null) lblTotalEntradas.setText("Entradas: R$ " + caixa.getTotalEntradas());
                if (lblTotalSaidas != null) lblTotalSaidas.setText("Saídas: R$ " + caixa.getTotalSaidas());
            } else {
                if (lblStatus != null) lblStatus.setText("Status: Nenhum caixa aberto");
                if (lblSaldo != null) lblSaldo.setText("Saldo: R$ 0,00");
                if (lblTotalEntradas != null) lblTotalEntradas.setText("Entradas: R$ 0,00");
                if (lblTotalSaidas != null) lblTotalSaidas.setText("Saídas: R$ 0,00");
            }

            System.out.println("Interface atualizada com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro ao atualizar interface: " + e.getMessage());
            e.printStackTrace();
            if (lblStatus != null) lblStatus.setText("Status: Erro");
        }
    }

    private void abrirCaixaInterface() {
        if (txtValorInicial.getText().isEmpty()) {
            throw new RuntimeException("Informe o valor inicial!");
        }

        try {
            BigDecimal valorInicial = new BigDecimal(txtValorInicial.getText());
            String observacoes = txtObservacoes.getText();

            Caixa caixa = caixaService.abrirCaixa(valorInicial, observacoes);
            mostrarSucesso("Caixa aberto com sucesso! Valor inicial: R$ " + valorInicial);

        } catch (NumberFormatException e) {
            throw new RuntimeException("Valor inicial inválido!");
        }
    }

    private void fecharCaixaInterface() {
        try {
            Caixa caixa = caixaService.fecharCaixa("");
            mostrarSucesso("Caixa fechado com sucesso! Valor final: R$ " + caixa.getValorFinal());

        } catch (Exception e) {
            mostrarErro("Erro ao fechar caixa: " + e.getMessage());
        }
    }

    @FXML
    private void abrirTelaVendas() {
        try {
            System.out.println("Abrindo tela de vendas...");

            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaRegistroPedido.fxml");
            if (fxmlUrl == null) {
                System.err.println("Arquivo FXML não encontrado: TelaRegistroPedido.fxml");
                mostrarMensagemErro("Arquivo da tela de vendas não encontrado!");
                return;
            }
            System.out.println("Arquivo FXML encontrado: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() == null) {
                System.err.println("Contexto do Spring não disponível");
                try {
                    Parent root = loader.load();
                    Stage stage = getCurrentStage();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Registro de Pedidos");
                    stage.centerOnScreen();
                    System.out.println("Tela de vendas aberta sem Spring");
                    return;
                } catch (Exception fallbackException) {
                    fallbackException.printStackTrace();
                    mostrarMensagemErro("Erro ao abrir tela: " + fallbackException.getMessage());
                    return;
                }
            }

            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            Parent root = loader.load();
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setTitle("Registro de Pedidos");
            stage.centerOnScreen();
            System.out.println("Tela de vendas aberta com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir tela de vendas: " + e.getMessage());
            String errorDetails = "Erro ao abrir tela de vendas:\n";
            if (e.getCause() != null) {
                errorDetails += "Causa: " + e.getCause().getMessage();
            } else {
                errorDetails += e.getMessage();
            }
            mostrarMensagemErro(errorDetails);
        }
    }

    @FXML
    private void abrirTelaEstoque() {
        System.out.println("BOTÃO ESTOQUE CLICADO - Iniciando...");

        try {
            System.out.println("Abrindo tela de estoque...");

            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaProdutos.fxml");
            if (fxmlUrl == null) {
                System.err.println("Arquivo FXML não encontrado: TelaProdutos.fxml");
                mostrarMensagemErro("Arquivo da tela de estoque não encontrado!");
                return;
            }
            System.out.println("Arquivo FXML encontrado: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() == null) {
                System.err.println("Contexto do Spring não disponível");
                try {
                    Parent root = loader.load();
                    Stage stage = getCurrentStage();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Estoque");
                    stage.centerOnScreen();
                    System.out.println("Tela de estoque aberta sem Spring");
                    return;
                } catch (Exception fallbackException) {
                    fallbackException.printStackTrace();
                    mostrarMensagemErro("Erro ao abrir tela: " + fallbackException.getMessage());
                    return;
                }
            }

            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            Parent root = loader.load();
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setTitle("Estoque");
            stage.centerOnScreen();
            System.out.println("Tela de estoque aberta com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir tela de estoque: " + e.getMessage());
            mostrarMensagemErro("Erro ao abrir tela de estoque: " + e.getMessage());
        }
    }

    @FXML
    private void sairParaLogin() {
        try {
            System.out.println("Abrindo pop-up de confirmação de saída...");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaSairPrograma.fxml"));

            Parent root = loader.load();
            ConfirmacaoSaidaController controller = loader.getController();

            Stage popupStage = new Stage();
            controller.setPopupStage(popupStage);

            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Confirmação de Saída");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(getCurrentStage());
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
            e.printStackTrace();
            System.err.println("Erro ao abrir pop-up de confirmação: " + e.getMessage());

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmação de Saída");
            alert.setHeaderText("Deseja realmente sair?");
            alert.setContentText("Você será redirecionado para a tela de login.");

            Optional<ButtonType> resultado = alert.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                voltarParaTelaLogin();
            }
        }
    }

    private void voltarParaTelaLogin() {
        try {
            System.out.println("Iniciando processo de volta para login...");

            Stage stage = getCurrentStage();
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
            reiniciarAplicacaoCompleta();
        }
    }

    private Stage getCurrentStage() {
        try {
            for (Window window : Window.getWindows()) {
                if (window instanceof Stage && window.isShowing()) {
                    return (Stage) window;
                }
            }

            Stage primaryStage = (Stage) Stage.getWindows().get(0);
            if (primaryStage != null) {
                return primaryStage;
            }

            System.err.println("Nenhum stage encontrado, criando novo...");
            return new Stage();
        } catch (Exception e) {
            System.err.println("Erro ao obter stage atual: " + e.getMessage());
            return new Stage();
        }
    }

    private void reiniciarAplicacaoCompleta() {
        try {
            Stage stage = getCurrentStage();
            stage.close();
            PdvGaleteriaApplication.main(new String[]{});
        } catch (Exception e) {
            System.err.println("Erro ao reiniciar aplicação: " + e.getMessage());
            mostrarMensagemErro("Erro crítico. Feche e abra o aplicativo manualmente.");
        }
    }

    private void mostrarSucesso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarMensagemErro(String mensagem) {
        mostrarErro(mensagem);
    }

    @FXML
    private void handleAcaoCaixa() {
        try {
            String acaoAtual = btnAcaoCaixa.getText();

            if (acaoAtual.equals("Abrir Caixa")) {
                abrirPopupAberturaCaixa();
            } else if (acaoAtual.equals("Fechar Caixa")) {
                fecharCaixaInterface();
            }

            atualizarInterface();

        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    private void abrirPopupAberturaCaixa() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/PopupAberturaCaixa.fxml")
            );

            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            Parent root = loader.load();
            PopupAberturaCaixaController controller = loader.getController();

            Stage popupStage = new Stage();
            controller.setPopupStage(popupStage);

            controller.setOnConfirmCallback(valorInicial -> {
                String observacoes = txtObservacoes != null ? txtObservacoes.getText() : "";
                try {
                    Caixa caixa = caixaService.abrirCaixa(valorInicial, observacoes);
                    mostrarSucesso("Caixa aberto com sucesso! Valor inicial: R$ " + valorInicial);
                    atualizarInterface();
                } catch (Exception e) {
                    mostrarErro("Erro ao abrir caixa: " + e.getMessage());
                }
            });

            controller.setOnCancelCallback(() -> {
                System.out.println("Abertura de caixa cancelada pelo usuário");
            });

            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Abertura de Caixa");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(getCurrentStage());
            popupStage.setResizable(false);
            popupStage.centerOnScreen();

            controller.limparCampo();
            controller.focarCampoValor();
            popupStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir popup: " + e.getMessage());
            abrirCaixaInterfaceFallback();
        }
    }

    private void abrirCaixaInterfaceFallback() {
        try {
            if (txtValorInicial == null || txtValorInicial.getText().isEmpty()) {
                throw new RuntimeException("Informe o valor inicial!");
            }

            BigDecimal valorInicial = new BigDecimal(txtValorInicial.getText());
            Caixa caixa = caixaService.abrirCaixa(valorInicial, "");
            mostrarSucesso("Caixa aberto com sucesso! Valor inicial: R$ " + valorInicial);

        } catch (NumberFormatException e) {
            throw new RuntimeException("Valor inicial inválido!");
        }
    }

    private void processarAberturaCaixa(BigDecimal valorInicial) {
        try {
            Caixa caixa = caixaService.abrirCaixa(valorInicial, "");
            mostrarSucesso("Caixa aberto com sucesso! Valor inicial: R$ " + valorInicial);
            atualizarInterface();
        } catch (Exception e) {
            mostrarErro("Erro ao abrir caixa: " + e.getMessage());
        }
    }
}