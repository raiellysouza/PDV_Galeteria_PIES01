package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.Caixa;
import com.example.pdv_galeteria.service.CaixaService;
import com.example.pdv_galeteria.PdvGaleteriaApplication;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import com.example.pdv_galeteria.model.StatusCaixa;

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

    @FXML
    private Label lblCaixaStatus;

    @FXML
    private Pane paneStatusCaixa;

    @FXML
    private ImageView imgCadeadoStatus;

    @FXML
    private Label lblDescricaoStatus;

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

        try {
            String statusTexto = caixaService.getStatusTextoBotao();
            btnAcaoCaixa.setText(statusTexto);

            Optional<Caixa> caixaOpt = caixaService.getCaixaDoDia();
            if (caixaOpt.isPresent()) {
                Caixa caixa = caixaOpt.get();
                StatusCaixa status = caixa.getStatus();

                System.out.println("Status atual do caixa: " + status);

                if (lblCaixaStatus != null) {
                    String titulo = status == StatusCaixa.ABERTO ? "Caixa Aberto" :
                            status == StatusCaixa.FECHADO ? "Caixa Fechado" :
                                    "Caixa " + status.toString();
                    lblCaixaStatus.setText(titulo);
                    System.out.println("Título atualizado: " + titulo);
                }

                if (lblDescricaoStatus != null) {
                    String descricao = status == StatusCaixa.ABERTO ? "O caixa está operando normalmente" :
                            status == StatusCaixa.FECHADO ? "Caixa Fechado" :
                                    "Status desconhecido";
                    lblDescricaoStatus.setText(descricao);
                    System.out.println("Descrição atualizada: " + descricao);
                }

                if (paneStatusCaixa != null) {
                    String corBorda = status == StatusCaixa.ABERTO ? "#009A05" :
                            status == StatusCaixa.FECHADO ? "#FF0000" :
                                    "#CCCCCC";
                    paneStatusCaixa.setStyle("-fx-border-color: " + corBorda + "; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 2px;");
                    System.out.println("Borda atualizada: " + corBorda);
                }

                atualizarImagemCadeado(status);

                if (lblTotalEntradas != null) {
                    lblTotalEntradas.setText("R$ " + formatarValor(caixa.getTotalEntradas()));
                }
                if (lblTotalSaidas != null) {
                    lblTotalSaidas.setText("R$ " + formatarValor(caixa.getTotalSaidas()));
                }
                if (lblSaldo != null) {
                    lblSaldo.setText("R$ " + formatarValor(caixa.getSaldoAtual()));
                }

            } else {
                System.out.println("Nenhum caixa encontrado");

                if (lblCaixaStatus != null) {
                    lblCaixaStatus.setText("Nenhum Caixa Aberto");
                }
                if (lblDescricaoStatus != null) {
                    lblDescricaoStatus.setText("Abra um caixa para começar");
                }
                if (paneStatusCaixa != null) {
                    paneStatusCaixa.setStyle("-fx-border-color: #CCCCCC; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 2px;");
                }

                atualizarImagemCadeado(null);

                if (lblTotalEntradas != null) lblTotalEntradas.setText("R$ 0,00");
                if (lblTotalSaidas != null) lblTotalSaidas.setText("R$ 0,00");
                if (lblSaldo != null) lblSaldo.setText("R$ 0,00");
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
            System.out.println("Botão clicado: " + acaoAtual);

            if (acaoAtual.equals("Abrir Caixa")) {
                System.out.println("Abrindo popup de abertura...");
                abrirPopupAberturaCaixa();
            } else if (acaoAtual.equals("Fechar Caixa")) {
                System.out.println("Abrindo popup de fechamento...");
                abrirPopupFechamentoCaixa();
            }

        } catch (Exception e) {
            System.err.println("Erro em handleAcaoCaixa: " + e.getMessage());
            e.printStackTrace();
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

    private void abrirPopupFechamentoCaixa() {
        System.out.println("\n=== TENTANDO ABRIR POPUP FECHAMENTO ===");

        try {
            Optional<Caixa> caixaOpt = caixaService.getCaixaDoDia();
            System.out.println("Caixa encontrado no banco: " + caixaOpt.isPresent());

            if (!caixaOpt.isPresent()) {
                System.err.println("ERRO: Não há caixa para hoje no banco de dados!");
                mostrarErro("Não há caixa aberto para fechar!");
                return;
            }

            Caixa caixa = caixaOpt.get();
            System.out.println("Status do caixa: " + caixa.getStatus());
            System.out.println("É ABERTO? " + (caixa.getStatus() == StatusCaixa.ABERTO));

            if (caixa.getStatus() != StatusCaixa.ABERTO) {
                System.err.println("ERRO: Caixa não está com status ABERTO!");
                mostrarErro("O caixa não está aberto!");
                return;
            }

            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/PopupFechamentoCaixa.fxml");
            System.out.println("URL do FXML de fechamento: " + fxmlUrl);

            if (fxmlUrl == null) {
                System.err.println("ERRO: Arquivo FXML não encontrado no caminho especificado!");
                System.err.println("Procurando em: /com/example/pdv_galeteria/Frontend/views/PopupFechamentoCaixa.fxml");

                try {
                    System.out.println("Recursos disponíveis:");
                    java.util.Enumeration<URL> resources = getClass().getClassLoader().getResources("");
                    while (resources.hasMoreElements()) {
                        System.out.println("- " + resources.nextElement());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                mostrarPopupFechamentoCustomizado();
                return;
            }

            System.out.println("Carregando FXML do popup de fechamento...");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            System.out.println("Contexto Spring disponível: " + (PdvGaleteriaApplication.getSpringContext() != null));
            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();
            System.out.println("FXML carregado com sucesso!");

            PopupFechamentoCaixaController controller = loader.getController();
            Stage popupStage = new Stage();
            controller.setPopupStage(popupStage);

            controller.setOnConfirmCallback(() -> {
                System.out.println("Callback de confirmação do popup chamado!");
                try {
                    System.out.println("Chamando service para fechar caixa...");
                    Caixa caixaFechado = caixaService.fecharCaixa("");

                    System.out.println("Caixa fechado com sucesso!");
                    System.out.println("Novo status: " + caixaFechado.getStatus());
                    System.out.println("Valor final: " + caixaFechado.getValorFinal());

                    mostrarSucesso("Caixa fechado com sucesso!\n" +
                            "Valor final para retirada: R$ " + formatarValor(caixaFechado.getValorFinal()));

                    System.out.println("Atualizando interface principal...");
                    Platform.runLater(() -> {
                        atualizarInterface();
                    });

                } catch (Exception e) {
                    System.err.println("Erro ao fechar caixa no callback: " + e.getMessage());
                    e.printStackTrace();
                    mostrarErro("Erro ao fechar caixa: " + e.getMessage());
                }
            });

            controller.setOnCancelCallback(() -> {
                System.out.println("Usuário cancelou o fechamento");
            });

            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Fechamento de Caixa");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(getCurrentStage());
            popupStage.setResizable(false);
            popupStage.centerOnScreen();

            System.out.println("Mostrando popup de fechamento...");
            popupStage.showAndWait();
            System.out.println("Popup de fechamento fechado");

        } catch (Exception e) {
            System.err.println("ERRO ao abrir popup de fechamento: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao abrir tela de fechamento: " + e.getMessage());
        }

        System.out.println("=== FIM TENTATIVA ABRIR POPUP FECHAMENTO ===\n");
    }

    private void mostrarPopupFechamentoCustomizado() {
        try {
            Optional<Caixa> caixaOpt = caixaService.getCaixaDoDia();
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
                Caixa caixaFechado = caixaService.fecharCaixa("");
                mostrarSucesso("Caixa fechado com sucesso!\n" +
                        "Valor final para retirada: R$ " + formatarValor(caixaFechado.getValorFinal()));
                atualizarInterface();
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao fechar caixa: " + e.getMessage());
        }
    }

    private String formatarValor(BigDecimal valor) {
        if (valor == null) {
            return "0,00";
        }

        try {
            String valorFormatado = String.format("%,.2f", valor);

            valorFormatado = valorFormatado.replace(",", "X").replace(".", ",").replace("X", ".");

            return valorFormatado;
        } catch (Exception e) {
            System.err.println("Erro ao formatar valor: " + valor + " - " + e.getMessage());
            return "0,00";
        }
    }

    private void atualizarEstiloCaixa(StatusCaixa status) {
        if (paneStatusCaixa == null) {
            System.err.println("paneStatusCaixa é nulo!");
            return;
        }

        String estiloBase = "-fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 2px;";

        if (status == StatusCaixa.ABERTO) {
            paneStatusCaixa.setStyle(estiloBase + " -fx-border-color: #009A05;");

            if (lblDescricaoStatus != null) {
                lblDescricaoStatus.setText("O caixa está operando normalmente");
                lblDescricaoStatus.setStyle("-fx-text-fill: #666666;");
            }

            System.out.println("Status: CAIXA ABERTO - Borda verde, operando normalmente");

        } else if (status == StatusCaixa.FECHADO) {
            paneStatusCaixa.setStyle(estiloBase + " -fx-border-color: #FF0000;");

            if (lblDescricaoStatus != null) {
                lblDescricaoStatus.setText("Caixa Fechado");
                lblDescricaoStatus.setStyle("-fx-text-fill: #666666;");
            }

            System.out.println("Status: CAIXA FECHADO - Borda vermelha, Caixa Fechado");

        } else {
            paneStatusCaixa.setStyle(estiloBase + " -fx-border-color: #CCCCCC;");

            if (lblDescricaoStatus != null) {
                lblDescricaoStatus.setText("Nenhum caixa disponível");
                lblDescricaoStatus.setStyle("-fx-text-fill: #666666;");
            }

            System.out.println("Status: NENHUM CAIXA - Borda cinza");
        }

        atualizarImagemCadeado(status);
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
            Stage stageAtual = (Stage) javafx.stage.Window.getWindows().stream()
                    .filter(window -> window instanceof Stage && window.isShowing())
                    .findFirst()
                    .orElse(null);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaEntregadores.fxml")
            );

            if (com.example.pdv_galeteria.PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(
                        com.example.pdv_galeteria.PdvGaleteriaApplication.getSpringContext()::getBean
                );
            }

            Parent root = loader.load();

            stageAtual.setScene(new Scene(root));
            stageAtual.setTitle("Entregadores");
            stageAtual.centerOnScreen();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}