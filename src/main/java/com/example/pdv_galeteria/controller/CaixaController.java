package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.*;
import com.example.pdv_galeteria.service.CaixaService;
import com.example.pdv_galeteria.PdvGaleteriaApplication;
import com.example.pdv_galeteria.service.MovimentoCaixaService;
import jakarta.annotation.PostConstruct;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.pdv_galeteria.service.MovimentoCaixaService;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

@Controller
public class CaixaController implements Initializable {

    @Autowired
    @Lazy
    private CaixaService caixaService;

    @Autowired
    private MovimentoCaixaService movimentoCaixaService;

    @FXML
    private Button btnAcaoCaixa;

    @FXML
    private TextField txtValorInicial;

    @FXML
    private TextField txtValorFinal;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblSaldo;

    @FXML
    private Label lblTotalEntradas;

    @FXML
    private Label lblTotalSaidas;

    @FXML
    private Label lblCaixaStatus;

    @FXML
    private Pane paneStatusCaixa;

    @FXML
    private ImageView imgCadeadoStatus;

    @FXML
    private Label lblDescricaoStatus;

    @FXML
    private Pane movimentacoesContainer;

    @FXML
    private ScrollPane scrollMovimentacoes;

    @FXML
    private VBox vboxMovimentacoes;

    @Autowired
    private UsuarioSessao usuarioSessao;

    @FXML
    private Label labelNomeUsuario;

    private Image graficoVerde;
    private Image graficoVermelho;

    private static Long contadorVendas = 1L;

    public CaixaController() {
        System.out.println("CaixaController instanciado!");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== CaixaController.initialize() INICIADO ===");

        System.out.println("labelNomeUsuario: " + (labelNomeUsuario != null ? "INJETADO" : "NULO"));
        System.out.println("usuarioSessao: " + (usuarioSessao != null ? "INJETADO" : "NULO"));

        atualizarNomeUsuario();

        System.out.println("caixaService: " + (caixaService != null ? "INJETADO" : "NULO"));
        System.out.println("movimentoCaixaService: " + (movimentoCaixaService != null ? "INJETADO" : "NULO"));

        if (btnAcaoCaixa == null) {
            System.err.println("ERRO: btnAcaoCaixa é NULL! Verifique o FXML.");
            return;
        }

        if (caixaService == null && PdvGaleteriaApplication.getSpringContext() != null) {
            caixaService = PdvGaleteriaApplication.getSpringContext().getBean(CaixaService.class);
            System.out.println("CaixaService obtido manualmente: " + (caixaService != null));
        }

        if (movimentoCaixaService == null && PdvGaleteriaApplication.getSpringContext() != null) {
            movimentoCaixaService = PdvGaleteriaApplication.getSpringContext().getBean(MovimentoCaixaService.class);
            System.out.println("MovimentoCaixaService obtido manualmente: " + (movimentoCaixaService != null));
        }

        carregarImagens();

        Platform.runLater(() -> {
            atualizarInterface();
            configurarAtualizacaoAutomatica();
        });
    }

    private void atualizarNomeUsuario() {
        System.out.println("=== atualizarNomeUsuario() ===");
        if (labelNomeUsuario != null && usuarioSessao != null) {
            String nome = usuarioSessao.getNomeUsuario();
            System.out.println("Configurando label para: " + nome);
            labelNomeUsuario.setText(nome);
            System.out.println("Label atualizado com sucesso!");
        } else {
            System.out.println("Falha ao atualizar nome do usuário:");
            System.out.println("- labelNomeUsuario: " + (labelNomeUsuario != null ? "OK" : "NULO"));
            System.out.println("- usuarioSessao: " + (usuarioSessao != null ? "OK" : "NULO"));
        }
    }

    private void carregarImagens() {
        try {
            InputStream streamVerde = getClass().getResourceAsStream("/assets/imgs/graficoVerde.png");
            if (streamVerde == null) {
                System.err.println("graficoVerde.png não encontrado no classpath");
                String caminho = System.getProperty("user.dir") + "/src/main/resources/assets/imgs/graficoVerde.png";
                File file = new File(caminho);
                if (file.exists()) {
                    graficoVerde = new Image(file.toURI().toString());
                    System.out.println("graficoVerde carregado do caminho absoluto");
                }
            } else {
                graficoVerde = new Image(streamVerde);
                streamVerde.close();
                System.out.println("graficoVerde carregado do classpath");
            }

            InputStream streamVermelho = getClass().getResourceAsStream("/assets/imgs/graficoVermelho.png");
            if (streamVermelho == null) {
                System.err.println("graficoVermelho.png não encontrado no classpath");
                String caminho = System.getProperty("user.dir") + "/src/main/resources/assets/imgs/graficoVermelho.png";
                File file = new File(caminho);
                if (file.exists()) {
                    graficoVermelho = new Image(file.toURI().toString());
                    System.out.println("graficoVermelho carregado do caminho absoluto");
                }
            } else {
                graficoVermelho = new Image(streamVermelho);
                streamVermelho.close();
                System.out.println("graficoVermelho carregado do classpath");
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar imagens: " + e.getMessage());
        }
    }

    public void atualizarInterface() {
        System.out.println("=== atualizarInterface() ===");

        try {
            if (caixaService == null) {
                System.err.println("ERRO: caixaService é nulo!");
                return;
            }

            if (btnAcaoCaixa != null) {
                String statusTexto = caixaService.getStatusTextoBotao();
                System.out.println("Texto do botão: " + statusTexto);
                btnAcaoCaixa.setText(statusTexto.trim());
            }

            Optional<Caixa> caixaOpt = caixaService.getCaixaAbertoDoDia();

            if (caixaOpt.isPresent()) {
                Caixa caixa = caixaOpt.get();
                StatusCaixa status = caixa.getStatus();

                System.out.println("Caixa encontrado - ID: " + caixa.getId() + ", Status: " + status);

                if (lblCaixaStatus != null) {
                    lblCaixaStatus.setText(status == StatusCaixa.ABERTO ? "Caixa Aberto" : "Caixa Fechado");
                }
                if (lblDescricaoStatus != null) {
                    lblDescricaoStatus.setText(status == StatusCaixa.ABERTO ? "O caixa está operando normalmente" : "Caixa Fechado");
                }

                System.out.println("Valores do Caixa:");
                System.out.println("  Valor Inicial: " + caixa.getValorInicial());
                System.out.println("  Total Entradas: " + caixa.getTotalEntradas());
                System.out.println("  Total Saídas: " + caixa.getTotalSaidas());
                System.out.println("  Saldo Atual: " + caixa.getSaldoAtual());

                if (lblTotalEntradas != null) {
                    BigDecimal totalEntradas = caixa.getTotalEntradas() != null ? caixa.getTotalEntradas() : BigDecimal.ZERO;
                    lblTotalEntradas.setText("R$ " + formatarValor(totalEntradas));
                    System.out.println("Total Entradas atualizado: R$ " + totalEntradas);
                }

                if (lblTotalSaidas != null) {
                    BigDecimal totalSaidas = caixa.getTotalSaidas() != null ? caixa.getTotalSaidas() : BigDecimal.ZERO;
                    lblTotalSaidas.setText("R$ " + formatarValor(totalSaidas));
                    System.out.println("Total Saídas atualizado: R$ " + totalSaidas);
                }

                if (lblSaldo != null) {
                    BigDecimal saldo = caixa.getSaldoAtual() != null ? caixa.getSaldoAtual() : BigDecimal.ZERO;
                    lblSaldo.setText("R$ " + formatarValor(saldo));
                    System.out.println("Saldo atualizado: R$ " + saldo);
                }

                if (paneStatusCaixa != null) {
                    String corBorda = status == StatusCaixa.ABERTO ? "#009A05" : "#FF0000";
                    paneStatusCaixa.setStyle("-fx-border-color: " + corBorda + "; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 2px;");
                }

                atualizarImagemCadeado(status);

                carregarMovimentacoesDoDia();

            } else {
                System.out.println("Nenhum caixa aberto encontrado");

                if (lblCaixaStatus != null) lblCaixaStatus.setText("Nenhum Caixa Aberto");
                if (lblDescricaoStatus != null) lblDescricaoStatus.setText("Abra um caixa para começar");
                if (lblTotalEntradas != null) lblTotalEntradas.setText("R$ 0,00");
                if (lblTotalSaidas != null) lblTotalSaidas.setText("R$ 0,00");
                if (lblSaldo != null) lblSaldo.setText("R$ 0,00");

                if (paneStatusCaixa != null) {
                    paneStatusCaixa.setStyle("-fx-border-color: #CCCCCC; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 2px;");
                }

                atualizarImagemCadeado(null);

                if (vboxMovimentacoes != null) {
                    vboxMovimentacoes.getChildren().clear();
                    Label lblVazio = new Label("Nenhuma movimentação hoje");
                    lblVazio.setStyle("-fx-text-fill: #666; -fx-font-size: 14px; -fx-padding: 20;");
                    vboxMovimentacoes.getChildren().add(lblVazio);
                }
            }

            System.out.println("Interface atualizada com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro ao atualizar interface: " + e.getMessage());
            e.printStackTrace();
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
            String acaoAtual = btnAcaoCaixa.getText().trim();
            System.out.println("Botão clicado: '" + acaoAtual + "'");

            if (acaoAtual.equals("Abrir Caixa")) {
                System.out.println("Abrindo popup de abertura...");
                abrirPopupAberturaCaixa();
            } else if (acaoAtual.equals("Fechar Caixa")) {
                System.out.println("Abrindo popup de fechamento...");
                abrirPopupFechamentoCaixa();
            } else {
                System.err.println("Ação não reconhecida: " + acaoAtual);
            }

        } catch (Exception e) {
            System.err.println("Erro em handleAcaoCaixa: " + e.getMessage());
            e.printStackTrace();
            mostrarErro(e.getMessage());
        }
    }

    private void abrirPopupAberturaCaixa() {
        try {
            System.out.println("Abrindo popup de abertura...");

            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/PopupAberturaCaixa.fxml");

            if (fxmlUrl == null) {
                System.err.println("Arquivo popupAberturaCaixa.fxml não encontrado!");
                abrirCaixaInterfaceFallback();
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
                System.out.println("Controller factory configurado com Spring");
            }

            Parent root = loader.load();

            PopupAberturaCaixaController controller = loader.getController();

            if (controller == null) {
                System.err.println("Controller do popup ainda é null após carregar!");
                abrirCaixaInterfaceFallback();
                return;
            }

            System.out.println("Controller do popup obtido com sucesso!");

            Stage popupStage = new Stage();
            controller.setPopupStage(popupStage);

            controller.setOnConfirmCallback(valorInicial -> {
                System.out.println("Valor inicial confirmado: " + valorInicial);
                abrirCaixa(valorInicial);
            });

            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Abertura de Caixa");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setResizable(false);

            controller.focarCampoValor();

            popupStage.showAndWait();

        } catch (Exception e) {
            System.err.println("Erro ao abrir popup de abertura: " + e.getMessage());
            e.printStackTrace();
            abrirCaixaInterfaceFallback();
        }
    }

    private void abrirCaixaInterfaceFallback() {
        try {
            if (txtValorInicial == null || txtValorInicial.getText().isEmpty()) {
                throw new RuntimeException("Informe o valor inicial!");
            }

            BigDecimal valorInicial = new BigDecimal(txtValorInicial.getText());
            Caixa caixa = caixaService.abrirCaixa(valorInicial);
            mostrarSucesso("Caixa aberto com sucesso! Valor inicial: R$ " + valorInicial);
            atualizarInterface();

        } catch (NumberFormatException e) {
            throw new RuntimeException("Valor inicial inválido!");
        }
    }

    private void abrirPopupFechamentoCaixa() {
        try {
            System.out.println("Abrindo popup de fechamento de caixa...");

            Optional<Caixa> caixaOpt = caixaService.getCaixaAbertoDoDia();
            if (!caixaOpt.isPresent()) {
                mostrarErro("Não há caixa aberto para fechar!");
                return;
            }

            Caixa caixa = caixaOpt.get();
            System.out.println("Caixa encontrado. ID: " + caixa.getId());

            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/PopupFechamentoCaixa.fxml");
            if (fxmlUrl == null) {
                System.err.println("Arquivo popupFechamentoCaixa.fxml não encontrado!");
                mostrarPopupFechamentoCustomizado();
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();
            PopupFechamentoCaixaController controller = loader.getController();

            Stage popupStage = new Stage();
            controller.setPopupStage(popupStage);

            controller.setOnConfirmCallback((BigDecimal valorFinalDigitado) -> {
                try {
                    System.out.println("=== FECHANDO CAIXA ===");
                    System.out.println("Valor recebido do popup: R$ " + valorFinalDigitado);
                    System.out.println("Caixa ID: " + caixa.getId());

                    Caixa caixaFechado = caixaService.fecharCaixa(valorFinalDigitado);

                    System.out.println("Caixa fechado com sucesso!");
                    System.out.println("Valor final salvo: R$ " + caixaFechado.getValorFinal());
                    System.out.println("======================");

                    atualizarInterface();

                    mostrarSucesso("Caixa fechado com sucesso!\n" +
                            "Valor final: R$ " + formatarValor(caixaFechado.getValorFinal()));

                } catch (Exception e) {
                    System.err.println("Erro ao fechar caixa: " + e.getMessage());
                    e.printStackTrace();
                    mostrarErro("Erro ao fechar caixa: " + e.getMessage());
                }
            });

            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Fechamento de Caixa");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(getCurrentStage());
            popupStage.setResizable(false);
            popupStage.centerOnScreen();
            popupStage.showAndWait();

        } catch (Exception e) {
            System.err.println("Erro ao abrir popup de fechamento: " + e.getMessage());
            e.printStackTrace();
            mostrarPopupFechamentoCustomizado();
        }
    }

    private void mostrarPopupFechamentoCustomizado() {
        try {
            Optional<Caixa> caixaOpt = caixaService.getCaixaAbertoDoDia();
            if (!caixaOpt.isPresent()) {
                mostrarErro("Não há caixa aberto para fechar!");
                return;
            }

            Caixa caixa = caixaOpt.get();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Fechamento de Caixa");
            alert.setHeaderText("Confirmar fechamento do caixa?");

            String contentText = "Resumo do Caixa:\n\n" +
                    "Valor inicial: R$ " + formatarValor(caixa.getValorInicial()) + "\n" +
                    "Total de entradas: R$ " + formatarValor(caixa.getTotalEntradas()) + "\n" +
                    "Total de saídas: R$ " + formatarValor(caixa.getTotalSaidas()) + "\n" +
                    "Saldo final: R$ " + formatarValor(caixa.getSaldoAtual()) + "\n\n" +
                    "Deseja realmente fechar o caixa?";

            alert.setContentText(contentText);

            Optional<ButtonType> resultado = alert.showAndWait();

            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                TextInputDialog dialog = new TextInputDialog(caixa.getSaldoAtual().toString());
                dialog.setTitle("Valor Final do Caixa");
                dialog.setHeaderText("Informe o valor final para fechamento:");
                dialog.setContentText("Valor final (R$):");

                Optional<String> resultadoValor = dialog.showAndWait();
                if (resultadoValor.isPresent() && !resultadoValor.get().isEmpty()) {
                    try {
                        BigDecimal valorFinal = new BigDecimal(resultadoValor.get());
                        Caixa caixaFechado = caixaService.fecharCaixa(valorFinal);
                        mostrarSucesso("Caixa fechado com sucesso!\n" +
                                "Valor final para retirada: R$ " + formatarValor(caixaFechado.getValorFinal()));
                        atualizarInterface();
                    } catch (NumberFormatException e) {
                        mostrarErro("Valor inválido! Digite um número válido.");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao fechar caixa: " + e.getMessage());
        }
    }

    private void abrirCaixa(BigDecimal valorInicial) {
        try {
            if (caixaService == null) {
                throw new RuntimeException("Serviço de caixa não disponível");
            }

            if (valorInicial == null || valorInicial.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Valor inicial inválido");
            }

            Caixa caixaAberto = caixaService.abrirCaixa(valorInicial);

            if (caixaAberto == null) {
                throw new RuntimeException("Falha ao abrir caixa");
            }

            System.out.println("Caixa aberto com sucesso! ID: " + caixaAberto.getId());

            atualizarInterface();

            mostrarSucesso("Caixa aberto com sucesso!\n" +
                    "Valor inicial: R$ " + formatarValor(valorInicial));

        } catch (Exception e) {
            System.err.println("Erro ao abrir caixa: " + e.getMessage());
            mostrarErro("Erro ao abrir caixa: " + e.getMessage());
        }
    }

    private String formatarValor(BigDecimal valor) {
        if (valor == null) {
            return "0,00";
        }
        try {
            DecimalFormat df = new DecimalFormat("#,##0.00",
                    new DecimalFormatSymbols(new Locale("pt", "BR")));
            return df.format(valor);
        } catch (Exception e) {
            return "0,00";
        }
    }

    private void atualizarImagemCadeado(StatusCaixa status) {
        if (imgCadeadoStatus == null) {
            System.err.println("imgCadeadoStatus é nulo!");
            return;
        }

        try {
            String nomeArquivo;

            if (status == StatusCaixa.ABERTO) {
                nomeArquivo = "cadeado2.png";
                System.out.println("Carregando imagem para CAIXA ABERTO: " + nomeArquivo);
            } else if (status == StatusCaixa.FECHADO) {
                nomeArquivo = "cadeadoFvermelho.png";
                System.out.println("Carregando imagem para CAIXA FECHADO: " + nomeArquivo);
            } else {
                nomeArquivo = "cadeadoFvermelho.png";
                System.out.println("Carregando imagem DEFAULT (status null): " + nomeArquivo);
            }

            System.out.println("Tentando carregar imagem: " + nomeArquivo);

            String projectPath = System.getProperty("user.dir");
            String imagePath = projectPath + "/src/main/resources/assets/imgs/" + nomeArquivo;

            System.out.println("Caminho absoluto: " + imagePath);

            File file = new File(imagePath);
            if (file.exists()) {
                Image imagem = new Image(file.toURI().toString());
                imgCadeadoStatus.setImage(imagem);
                System.out.println("Imagem carregada via caminho absoluto!");
                return;
            } else {
                System.err.println("Arquivo não encontrado no caminho absoluto: " + imagePath);
            }

            InputStream stream = getClass().getResourceAsStream("/assets/imgs/" + nomeArquivo);

            if (stream == null) {
                System.err.println("Método 2 falhou. Tentando método 3...");
                stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("assets/imgs/" + nomeArquivo);
            }

            if (stream == null) {
                System.err.println("Método 3 falhou. Imagem não encontrada: " + nomeArquivo);

                if (status == StatusCaixa.ABERTO) {
                    imgCadeadoStatus.setStyle("-fx-effect: dropshadow(gaussian, #009A05, 10, 0, 0, 0);");
                } else if (status == StatusCaixa.FECHADO) {
                    imgCadeadoStatus.setStyle("-fx-effect: dropshadow(gaussian, #FF0000, 10, 0, 0, 0);");
                } else {
                    imgCadeadoStatus.setStyle("-fx-effect: none;");
                }

                System.out.println("Usando fallback de estilo (sombra colorida)");
                return;
            }

            Image imagem = new Image(stream);
            imgCadeadoStatus.setImage(imagem);
            imgCadeadoStatus.setStyle("");

            System.out.println("Imagem '" + nomeArquivo + "' carregada com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro fatal ao carregar imagem: " + e.getMessage());
            e.printStackTrace();
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

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
                System.out.println("Spring configurado");
            }

            System.out.println("Carregando FXML...");
            Parent root = loader.load();
            System.out.println("FXML carregado com sucesso!");

            Stage stage = null;

            if (lblSaldo != null && lblSaldo.getScene() != null) {
                stage = (Stage) lblSaldo.getScene().getWindow();
                System.out.println("Usando campoBusca para obter stage");
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

    public MovimentoCaixa registrarEntrada(BigDecimal valor, String descricao, String referenciaExterna) {
        if (caixaService == null) {
            throw new RuntimeException("Serviço de caixa não disponível");
        }
        return caixaService.registrarEntrada(valor, descricao, referenciaExterna);
    }

    public MovimentoCaixa registrarSaida(BigDecimal valor, String descricao, String referenciaExterna) {
        if (caixaService == null) {
            throw new RuntimeException("Serviço de caixa não disponível");
        }
        return caixaService.registrarSaida(valor, descricao, referenciaExterna);
    }

    public BigDecimal getSaldoAtualDoDia() {
        if (caixaService == null) {
            System.err.println("ERRO: caixaService é nulo ao obter saldo!");
            return BigDecimal.ZERO;
        }
        return caixaService.getSaldoAtualDoDia();
    }

    public BigDecimal getTotalEntradasDoDia() {
        if (caixaService == null) {
            System.err.println("ERRO: caixaService é nulo ao obter total de entradas!");
            return BigDecimal.ZERO;
        }
        return caixaService.getTotalEntradasDoDia();
    }

    public BigDecimal getTotalSaidasDoDia() {
        if (caixaService == null) {
            System.err.println("ERRO: caixaService é nulo ao obter total de saídas!");
            return BigDecimal.ZERO;
        }
        return caixaService.getTotalSaidasDoDia();
    }

    public void setCaixaService(CaixaService caixaService) {
        System.out.println("setCaixaService() chamado: " + (caixaService != null ? "OK" : "NULL"));
        this.caixaService = caixaService;

        Platform.runLater(() -> {
            atualizarNomeUsuario();
        });
    }

    private void carregarMovimentacoesDoDia() {
        try {
            if (vboxMovimentacoes == null) {
                System.err.println("vboxMovimentacoes é nulo!");
                return;
            }

            vboxMovimentacoes.getChildren().clear();

            Optional<Caixa> caixaOpt = caixaService.getCaixaAbertoDoDia();

            if (!caixaOpt.isPresent()) {
                Optional<Caixa> caixaFechadoOpt = caixaService.getUltimoCaixaFechadoDoDia();

                Label lblTitulo = new Label("Histórico de Movimentações");
                lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0 15 10;");
                vboxMovimentacoes.getChildren().add(lblTitulo);

                if (caixaFechadoOpt.isPresent()) {
                    Caixa caixaFechado = caixaFechadoOpt.get();
                    Label lblSubtitulo = new Label("Caixa fechado em: " +
                            caixaFechado.getDataFechamento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    lblSubtitulo.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px; -fx-padding: 0 0 20 10;");
                    vboxMovimentacoes.getChildren().add(lblSubtitulo);

                    List<MovimentoCaixa> movimentacoes = movimentoCaixaService.listarMovimentosDoCaixa(caixaFechado.getId());
                    adicionarMovimentacoesNaTela(movimentacoes);
                } else {
                    Label lblSubtitulo = new Label("Nenhum caixa aberto hoje");
                    lblSubtitulo.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px; -fx-padding: 0 0 20 10;");
                    vboxMovimentacoes.getChildren().add(lblSubtitulo);

                    Label lblVazio = new Label("Abra um caixa para ver as movimentações");
                    lblVazio.setStyle("-fx-text-fill: #666; -fx-font-size: 14px; -fx-padding: 40px;");
                    vboxMovimentacoes.getChildren().add(lblVazio);
                }
            } else {
                Caixa caixa = caixaOpt.get();

                Label lblTitulo = new Label("Movimentações do Caixa Atual");
                lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0 15 10;");
                vboxMovimentacoes.getChildren().add(lblTitulo);

                Label lblSubtitulo = new Label("Aberto em: " +
                        caixa.getDataAbertura().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                lblSubtitulo.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px; -fx-padding: 0 0 20 10;");
                vboxMovimentacoes.getChildren().add(lblSubtitulo);

                System.out.println("Carregando movimentações do caixa atual ID: " + caixa.getId());

                List<MovimentoCaixa> movimentacoes = movimentoCaixaService.listarMovimentosDoCaixa(caixa.getId());
                adicionarMovimentacoesNaTela(movimentacoes);
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar movimentações: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void adicionarMovimentacoesNaTela(List<MovimentoCaixa> movimentacoes) {
        if (movimentacoes.isEmpty()) {
            Label lblVazio = new Label("Nenhuma movimentação registrada");
            lblVazio.setStyle("-fx-text-fill: #666; -fx-font-size: 14px; -fx-padding: 40px;");
            vboxMovimentacoes.getChildren().add(lblVazio);
        } else {
            for (MovimentoCaixa movimento : movimentacoes) {
                Pane itemMovimentacao = criarItemMovimentacao(movimento);
                vboxMovimentacoes.getChildren().add(itemMovimentacao);

                Pane espacamento = new Pane();
                espacamento.setPrefHeight(15);
                vboxMovimentacoes.getChildren().add(espacamento);
            }
        }
    }

    private Pane criarItemMovimentacao(MovimentoCaixa movimento) {
        Pane itemPane = new Pane();
        itemPane.setPrefSize(925, 80);
        itemPane.setStyle("-fx-background-color: white; -fx-background-radius: 8px; " +
                "-fx-border-color: #E5E7EB; -fx-border-radius: 8px; -fx-border-width: 1px;");

        String descricao = movimento.getDescricao() != null ? movimento.getDescricao() : "Movimentação";
        String descricaoLower = descricao.toLowerCase();
        TipoMovimentoCaixa tipo = movimento.getTipo();

        System.out.println("Criando item para: " + descricao + " | Tipo: " + tipo);

        boolean isTroco = descricaoLower.contains("troco");
        boolean isEntrada = tipo == TipoMovimentoCaixa.ENTRADA;

        String corFundo, corTexto, prefixoValor, titulo;
        Image imagemGrafico;

        if (isEntrada && !isTroco) {
            corFundo = "#E3F0DF";
            corTexto = "#009A05";
            prefixoValor = "+R$ ";
            titulo = extrairTituloVenda(descricao);
            imagemGrafico = graficoVerde;
        } else if (!isEntrada || isTroco) {
            corFundo = "#FFE0E0";
            corTexto = "#E02525";
            prefixoValor = "-R$ ";
            titulo = isTroco ? "Troco" : "Saída";
            imagemGrafico = graficoVermelho;
        } else {
            corFundo = "#E5E7EB";
            corTexto = "#6B7280";
            prefixoValor = "";
            titulo = descricao;
            imagemGrafico = null;
        }

        Pane iconContainer = new Pane();
        iconContainer.setPrefSize(40, 40);
        iconContainer.setLayoutX(15);
        iconContainer.setLayoutY(20);
        iconContainer.setStyle("-fx-background-color: " + corFundo + "; -fx-background-radius: 20px;");

        if (imagemGrafico != null) {
            ImageView imageView = new ImageView(imagemGrafico);
            imageView.setFitWidth(24);
            imageView.setFitHeight(24);
            imageView.setLayoutX(8);
            imageView.setLayoutY(8);
            iconContainer.getChildren().add(imageView);
        } else {
            String simbolo = isEntrada ? "↑" : "↓";
            Label lblSimbolo = new Label(simbolo);
            lblSimbolo.setStyle("-fx-text-fill: " + corTexto + "; -fx-font-size: 18px; -fx-font-weight: bold;");
            lblSimbolo.setPrefSize(40, 40);
            lblSimbolo.setAlignment(Pos.CENTER);
            iconContainer.getChildren().add(lblSimbolo);
        }

        Label lblTitulo = new Label(titulo);
        lblTitulo.setLayoutX(70);
        lblTitulo.setLayoutY(15);
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblTitulo.setStyle("-fx-text-fill: #111827;");
        lblTitulo.setPrefWidth(500);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String horaFormatada = movimento.getDataHora().format(formatter);

        Label lblHora = new Label(horaFormatada);
        lblHora.setLayoutX(70);
        lblHora.setLayoutY(40);
        lblHora.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");

        Label lblValor = new Label();
        lblValor.setLayoutX(760);
        lblValor.setLayoutY(25);
        lblValor.setFont(Font.font("System", FontWeight.BOLD, 20));
        lblValor.setPrefWidth(150);
        lblValor.setAlignment(Pos.CENTER_RIGHT);

        String valorFormatado = formatarValor(movimento.getValor());
        String textoCompleto = prefixoValor + valorFormatado;
        lblValor.setText(textoCompleto);
        lblValor.setStyle("-fx-text-fill: " + corTexto + ";");

        itemPane.getChildren().addAll(iconContainer, lblTitulo, lblHora, lblValor);

        return itemPane;
    }

    private String extrairTituloVenda(String descricao) {
        if (descricao == null) return "Venda";

        if (descricao.toLowerCase().contains("troco")) {
            return "Troco";
        }

        if (descricao.contains(" - ")) {
            String[] partes = descricao.split(" - ");
            if (partes.length > 0) {
                return partes[0].trim();
            }
        }

        if (descricao.contains("#")) {
            return descricao;
        }

        return "Venda";
    }

    private ImageView criarImageViewIcone(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
        imageView.setLayoutX(10);
        imageView.setLayoutY(10);
        return imageView;
    }

    private String formatarValorSimples(java.math.BigDecimal valor) {
        if (valor == null) {
            return "0,00";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "BR"));
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');

        DecimalFormat df = new DecimalFormat("#,##0.00", symbols);

        return df.format(valor.abs());
    }

    private void configurarAtualizacaoAutomatica() {
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.seconds(10),
                ae -> {
                    System.out.println("Atualização automática do caixa");
                    atualizarInterface();
                }
        ));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void atualizarCaixa() {
        Platform.runLater(() -> {
            atualizarInterface();
        });
    }

    @FXML
    private void abrirTelaRelatorios() {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaRelatorio.fxml");
            if (fxmlUrl == null) {
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            Stage stage = (Stage) lblDescricaoStatus.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Relatórios");
            stage.centerOnScreen();

        } catch (Exception e) {
        }
    }

    @FXML
    private void abrirTelaConfiguracoes() {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaConfiguracao.fxml");
            if (fxmlUrl == null) {
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            Stage stage = (Stage) lblDescricaoStatus.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Configurações");
            stage.centerOnScreen();

        } catch (Exception e) {
        }
    }

    @FXML
    private void abrirTelaDashboard() {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaDashBoard.fxml");
            if (fxmlUrl == null) {
                mostrarErro("Arquivo da tela de dashboard não encontrado!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");
            stage.centerOnScreen();
        } catch (Exception e) {
            mostrarErro("Erro ao abrir tela de dashboard: " + e.getMessage());
        }
    }
}

