package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.dto.PedidoResumoDTO;
import com.example.pdv_galeteria.dto.RelatorioVendasDTO;
import com.example.pdv_galeteria.service.RelatorioService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Controller
public class RelatorioHojeController implements Initializable {

    @FXML private Label labelTotalHoje;
    @FXML private Label labelPedidosHoje;
    @FXML private Label labelTicketMedio;
    @FXML private Label labelPeriodo;

    @FXML private Button btnExportar;
    @FXML private Button btnFechar;

    @FXML private Label labelPixPercent;
    @FXML private Label labelCreditoPercent;
    @FXML private Label labelDebitoPercent;
    @FXML private Label labelDinheiroPercent;

    @FXML private VBox containerVendas;

    @Autowired
    private RelatorioService relatorioService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("RelatorioHojeController inicializando...");
        carregarDadosRelatorioHoje();
        configurarBotoes();
    }

    private void carregarDadosRelatorioHoje() {
        try {
            System.out.println("Carregando dados do relatório de hoje...");

            carregarDadosBasicos();

            carregarUltimasVendas();

            carregarDistribuicaoPagamento();

            System.out.println("Dados do relatório carregados com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro ao carregar dados do relatório: " + e.getMessage());
            e.printStackTrace();
            definirValoresPadrao();
        }
    }

    private void carregarDadosBasicos() {
        try {
            RelatorioVendasDTO relatorio = relatorioService.getRelatorioVendasHoje();
            System.out.println("Dados básicos carregados: " + relatorio);

            if (labelTotalHoje != null) {
                labelTotalHoje.setText(formatarValor(relatorio.getTotalVendas()));
            }

            if (labelPedidosHoje != null) {
                labelPedidosHoje.setText(String.valueOf(relatorio.getTotalPedidos()));
            }

            if (labelTicketMedio != null) {
                double ticketMedio = calcularTicketMedio(relatorio);
                labelTicketMedio.setText(formatarValor(ticketMedio));
            }

            if (labelPeriodo != null) {
                labelPeriodo.setText(formatarData(LocalDate.now()));
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar dados básicos: " + e.getMessage());
            definirValoresBasicosPadrao();
        }
    }

    private void carregarUltimasVendas() {
        try {
            System.out.println("Carregando últimas vendas...");
            List<PedidoResumoDTO> ultimasVendas = relatorioService.getUltimasVendasHoje(5);

            containerVendas.getChildren().clear();

            if (ultimasVendas != null && !ultimasVendas.isEmpty()) {
                System.out.println("Encontradas " + ultimasVendas.size() + " vendas recentes");

                for (PedidoResumoDTO venda : ultimasVendas) {
                    adicionarLinhaVenda(venda);
                }

            } else {
                System.out.println("Nenhuma venda encontrada para hoje");
                adicionarLinhaVazia();
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar últimas vendas: " + e.getMessage());
            e.printStackTrace();
            adicionarLinhaVazia();
        }
    }

    private void adicionarLinhaVenda(PedidoResumoDTO venda) {
        HBox linha = new HBox();
        linha.setSpacing(10.0);
        linha.setStyle("-fx-padding: 12 10; -fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;");

        Label labelId = new Label("#" + venda.getId());
        labelId.setPrefWidth(40);
        labelId.setTextFill(javafx.scene.paint.Color.web("#4b5563"));

        Label labelCliente = new Label(venda.getCliente());
        labelCliente.setPrefWidth(120);
        labelCliente.setTextFill(javafx.scene.paint.Color.web("#1f2937"));

        String produtos = venda.getProdutos();
        if (produtos.length() > 30) {
            produtos = produtos.substring(0, 27) + "...";
        }
        Label labelProdutos = new Label(produtos);
        labelProdutos.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(labelProdutos, javafx.scene.layout.Priority.ALWAYS);
        labelProdutos.setTextFill(javafx.scene.paint.Color.web("#4b5563"));

        Label labelValor = new Label(venda.getValorFormatado());
        labelValor.setPrefWidth(100);
        labelValor.setTextFill(javafx.scene.paint.Color.web("#16a34a"));
        labelValor.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Label labelPagamento = new Label(venda.getFormaPagamento());
        labelPagamento.setPrefWidth(120);
        labelPagamento.setTextFill(javafx.scene.paint.Color.web("#4b5563"));

        Label labelHorario = new Label(venda.getHoraFormatada());
        labelHorario.setPrefWidth(60);
        labelHorario.setTextFill(javafx.scene.paint.Color.web("#4b5563"));

        linha.getChildren().addAll(labelId, labelCliente, labelProdutos, labelValor, labelPagamento, labelHorario);
        containerVendas.getChildren().add(linha);
    }

    private void adicionarLinhaVazia() {
        HBox linha = new HBox();
        linha.setSpacing(10.0);
        linha.setStyle("-fx-padding: 12 10; -fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;");

        Label labelVazio = new Label("Nenhuma venda encontrada hoje");
        labelVazio.setTextFill(javafx.scene.paint.Color.web("#6b7280"));
        labelVazio.setPrefWidth(600);

        linha.getChildren().add(labelVazio);
        containerVendas.getChildren().add(linha);
    }

    private void carregarDistribuicaoPagamento() {
        try {
            System.out.println("Carregando distribuição de pagamento...");
            Map<String, BigDecimal> distribuicao = relatorioService.getDistribuicaoPagamentoHoje();

            if (distribuicao != null && !distribuicao.isEmpty()) {
                System.out.println("Distribuição de pagamento: " + distribuicao);

                if (labelPixPercent != null) {
                    BigDecimal pixPercent = distribuicao.getOrDefault("PIX", BigDecimal.ZERO);
                    labelPixPercent.setText(formatarPercentual(pixPercent));
                }

                if (labelCreditoPercent != null) {
                    BigDecimal creditoPercent = distribuicao.getOrDefault("CARTÃO CRÉDITO", BigDecimal.ZERO);
                    labelCreditoPercent.setText(formatarPercentual(creditoPercent));
                }

                if (labelDebitoPercent != null) {
                    BigDecimal debitoPercent = distribuicao.getOrDefault("CARTÃO DÉBITO", BigDecimal.ZERO);
                    labelDebitoPercent.setText(formatarPercentual(debitoPercent));
                }

                if (labelDinheiroPercent != null) {
                    BigDecimal dinheiroPercent = distribuicao.getOrDefault("DINHEIRO", BigDecimal.ZERO);
                    labelDinheiroPercent.setText(formatarPercentual(dinheiroPercent));
                }
            } else {
                System.out.println("Nenhuma distribuição de pagamento encontrada");
                definirDistribuicaoVazia();
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar distribuição de pagamento: " + e.getMessage());
            e.printStackTrace();
            definirDistribuicaoVazia();
        }
    }

    private double calcularTicketMedio(RelatorioVendasDTO relatorio) {
        if (relatorio.getTotalPedidos() > 0) {
            return relatorio.getTotalVendas().doubleValue() / relatorio.getTotalPedidos();
        }
        return 0.0;
    }

    private String formatarValor(double valor) {
        return String.format("R$ %.2f", valor);
    }

    private String formatarValor(BigDecimal valor) {
        if (valor == null) {
            return "R$ 0,00";
        }
        return String.format("R$ %.2f", valor.doubleValue());
    }

    private String formatarData(LocalDate data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return data.format(formatter);
    }

    private String formatarPercentual(BigDecimal percentual) {
        if (percentual == null) {
            return "0%";
        }
        return String.format("%.1f%%", percentual.doubleValue());
    }

    private void definirValoresBasicosPadrao() {
        if (labelTotalHoje != null) labelTotalHoje.setText("R$ 0,00");
        if (labelPedidosHoje != null) labelPedidosHoje.setText("0");
        if (labelTicketMedio != null) labelTicketMedio.setText("R$ 0,00");
        if (labelPeriodo != null) {
            labelPeriodo.setText(formatarData(LocalDate.now()));
        }
    }

    private void definirDistribuicaoVazia() {
        if (labelPixPercent != null) labelPixPercent.setText("0%");
        if (labelCreditoPercent != null) labelCreditoPercent.setText("0%");
        if (labelDebitoPercent != null) labelDebitoPercent.setText("0%");
        if (labelDinheiroPercent != null) labelDinheiroPercent.setText("0%");
    }

    private void definirValoresPadrao() {
        definirValoresBasicosPadrao();
        adicionarLinhaVazia();
        definirDistribuicaoVazia();
    }

    private void configurarBotoes() {
        if (btnExportar != null) {
            btnExportar.setOnAction(event -> handleExportarRelatorio());
        }

        if (btnFechar != null) {
            btnFechar.setOnAction(event -> handleFechar());
        }
    }

    @FXML
    private void handleExportarRelatorio() {
        try {
            System.out.println("Exportando relatório de hoje...");

            LocalDateTime agora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String nomeArquivo = "relatorio_hoje_" + agora.format(formatter) + ".pdf";

            java.nio.file.Path caminho = java.nio.file.Paths.get(
                    System.getProperty("user.home"),
                    "Downloads",
                    nomeArquivo
            );

            java.nio.file.Files.createDirectories(caminho.getParent());

            LocalDate hoje = LocalDate.now();
            relatorioService.gerarRelatorioVendas(hoje, hoje, caminho);

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Relatório Exportado",
                    "Relatório exportado com sucesso!",
                    "O arquivo foi salvo em:\n" + caminho.toAbsolutePath()
            );

            System.out.println("Relatório exportado para: " + caminho.toAbsolutePath());

        } catch (Exception e) {
            System.err.println("Erro ao exportar relatório: " + e.getMessage());
            e.printStackTrace();

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro ao Exportar",
                    "Não foi possível exportar o relatório.",
                    "Erro: " + e.getMessage()
            );
        }
    }

    @FXML
    private void handleFechar() {
        System.out.println("Fechando tela de relatório...");
        Stage stage = (Stage) (btnFechar != null ? btnFechar.getScene().getWindow() :
                labelTotalHoje.getScene().getWindow());
        stage.close();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }
}