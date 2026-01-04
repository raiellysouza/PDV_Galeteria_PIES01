package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.dto.RelatorioVendasDTO;
import com.example.pdv_galeteria.service.RelatorioService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

@Controller
public class RelatorioMensalController implements Initializable {

    @FXML private Label labelPedidosMes;
    @FXML private Label labelTotalMes;
    @FXML private Label labelTicketMedioMes;
    @FXML private Label labelPeriodoMes;

    @FXML private Label labelPixPercentMes;
    @FXML private Label labelCreditoPercentMes;
    @FXML private Label labelDebitoPercentMes;
    @FXML private Label labelDinheiroPercentMes;

    @FXML private VBox containerSemanas;

    @FXML private Button btnExportar;
    @FXML private Button btnFechar;

    @Autowired
    private RelatorioService relatorioService;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", new Locale("pt", "BR"));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter SHORT_MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("RelatorioMensalController inicializando...");
        carregarDadosRelatorioMensal();
        configurarBotoes();
    }

    private void carregarDadosRelatorioMensal() {
        try {
            System.out.println("Carregando dados do relatório mensal...");

            LocalDate hoje = LocalDate.now();
            YearMonth anoMesAtual = YearMonth.from(hoje);
            LocalDate primeiroDiaMes = anoMesAtual.atDay(1);
            LocalDate ultimoDiaMes = anoMesAtual.atEndOfMonth();

            System.out.println("Período mensal: " + primeiroDiaMes + " a " + ultimoDiaMes);

            carregarResumoMensal(primeiroDiaMes, ultimoDiaMes);

            carregarFaturamentoSemanas(primeiroDiaMes, ultimoDiaMes);

            carregarDistribuicaoPagamentoMensal(primeiroDiaMes, ultimoDiaMes);

            System.out.println("Dados do relatório mensal carregados com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro ao carregar dados do relatório mensal: " + e.getMessage());
            e.printStackTrace();
            definirValoresPadrao();
        }
    }

    private void carregarResumoMensal(LocalDate dataInicio, LocalDate dataFim) {
        try {
            RelatorioVendasDTO relatorio = relatorioService.getRelatorioVendasPeriodo(dataInicio, dataFim);

            if (relatorio != null) {
                System.out.println("Resumo mensal carregado: " + relatorio);

                if (labelPedidosMes != null) {
                    labelPedidosMes.setText(String.format("%,d", relatorio.getTotalPedidos()));
                }

                if (labelTotalMes != null) {
                    labelTotalMes.setText(formatarValorGrande(relatorio.getTotalVendas()));
                }

                if (labelTicketMedioMes != null) {
                    labelTicketMedioMes.setText(relatorio.getValorMedioPedidoFormatado());
                }

                if (labelPeriodoMes != null) {
                    String nomeMes = dataInicio.format(MONTH_FORMATTER);
                    nomeMes = capitalizarPrimeiraLetra(nomeMes);
                    labelPeriodoMes.setText(nomeMes);
                }
            } else {
                System.out.println("Nenhum dado encontrado para o mês");
                definirResumoPadrao(dataInicio);
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar resumo mensal: " + e.getMessage());
            definirResumoPadrao(dataInicio);
        }
    }

    private void carregarFaturamentoSemanas(LocalDate dataInicio, LocalDate dataFim) {
        try {
            System.out.println("Carregando faturamento por semana...");
            containerSemanas.getChildren().clear();

            LocalDate dataAtual = dataInicio;
            int semanaNumero = 1;
            int id = 201;

            while (!dataAtual.isAfter(dataFim)) {
                LocalDate inicioSemana = dataAtual;
                LocalDate fimSemana = dataAtual.plusDays(6);

                if (fimSemana.isAfter(dataFim)) {
                    fimSemana = dataFim;
                }

                System.out.println("Semana " + semanaNumero + ": " + inicioSemana + " a " + fimSemana);

                RelatorioVendasDTO relatorioSemana = relatorioService.getRelatorioVendasPeriodo(inicioSemana, fimSemana);
                BigDecimal faturamento = relatorioSemana != null ? relatorioSemana.getTotalVendas() : BigDecimal.ZERO;
                int pedidos = relatorioSemana != null ? (int) relatorioSemana.getTotalPedidos() : 0;

                String formasPagamento = obterFormasPagamentoSemana(inicioSemana, fimSemana);
                String status = gerarStatusSemana(inicioSemana, fimSemana);

                HBox linhaSemana = criarLinhaSemana(
                        id,
                        semanaNumero,
                        inicioSemana,
                        fimSemana,
                        pedidos,
                        faturamento,
                        formasPagamento,
                        status
                );

                containerSemanas.getChildren().add(linhaSemana);

                dataAtual = fimSemana.plusDays(1);
                semanaNumero++;
                id++;
            }

            if (containerSemanas.getChildren().isEmpty()) {
                criarLinhasSemanasVazias(dataInicio, dataFim);
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar faturamento por semana: " + e.getMessage());
            e.printStackTrace();
            criarLinhasSemanasVazias(dataInicio, dataFim);
        }
    }

    private String obterFormasPagamentoSemana(LocalDate inicioSemana, LocalDate fimSemana) {
        try {
            Map<String, BigDecimal> distribuicao = relatorioService.getDistribuicaoPagamentoPeriodo(inicioSemana, fimSemana);

            if (distribuicao != null && !distribuicao.isEmpty()) {
                String formaMaisUsada = "Diversos";
                BigDecimal maiorPercentual = BigDecimal.ZERO;

                for (Map.Entry<String, BigDecimal> entry : distribuicao.entrySet()) {
                    if (entry.getValue().compareTo(maiorPercentual) > 0) {
                        maiorPercentual = entry.getValue();
                        formaMaisUsada = entry.getKey();
                    }
                }

                return formaMaisUsada;
            }
        } catch (Exception e) {
            System.err.println("Erro ao obter formas de pagamento da semana: " + e.getMessage());
        }

        return "Diversos";
    }

    private String gerarStatusSemana(LocalDate inicioSemana, LocalDate fimSemana) {
        LocalDate hoje = LocalDate.now();

        if (fimSemana.isBefore(hoje)) {
            return "Período completo";
        } else if (inicioSemana.isBefore(hoje) && fimSemana.isAfter(hoje)) {
            return "Em andamento";
        } else if (inicioSemana.isAfter(hoje)) {
            return "Futuro";
        } else {
            return "Período completo";
        }
    }

    private HBox criarLinhaSemana(int id, int semanaNumero, LocalDate inicioSemana, LocalDate fimSemana,
                                  int pedidos, BigDecimal faturamento, String formasPagamento, String status) {
        HBox linha = new HBox(10);
        linha.setPadding(new Insets(12, 10, 12, 10));

        String corFundo = (id % 2 == 0) ? "#F9FAFB" : "white";
        linha.setStyle("-fx-background-color: " + corFundo + "; " +
                "-fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;");

        Label lblId = new Label("#" + id);
        lblId.setPrefWidth(40);
        lblId.setTextFill(Color.web("#4b5563"));

        String periodoFormatado = "Semana " + semanaNumero + " (" +
                inicioSemana.format(DATE_FORMATTER) + " - " +
                fimSemana.format(DATE_FORMATTER) + ")";
        Label lblPeriodo = new Label(periodoFormatado);
        lblPeriodo.setPrefWidth(150);
        lblPeriodo.setTextFill(Color.web("#1f2937"));

        String resumo = pedidos + " pedidos realizados";
        Label lblResumo = new Label(resumo);
        lblResumo.setPrefWidth(200);
        lblResumo.setTextFill(Color.web("#4b5563"));

        String textoFaturamento = formatarValorGrande(faturamento);
        Label lblFaturamento = new Label(textoFaturamento);
        lblFaturamento.setPrefWidth(120);
        lblFaturamento.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        if (faturamento.compareTo(BigDecimal.ZERO) > 0) {
            lblFaturamento.setTextFill(Color.web("#16a34a"));
        } else {
            lblFaturamento.setTextFill(Color.web("#6b7280"));
        }

        Label lblPagamento = new Label(formasPagamento);
        lblPagamento.setPrefWidth(100);
        lblPagamento.setTextFill(Color.web("#4b5563"));

        Label lblStatus = new Label(status);
        lblStatus.setPrefWidth(120);

        switch (status) {
            case "Período completo":
                lblStatus.setTextFill(Color.web("#059669"));
                lblStatus.setStyle("-fx-font-weight: bold;");
                break;
            case "Em andamento":
                lblStatus.setTextFill(Color.web("#d97706"));
                lblStatus.setStyle("-fx-font-weight: bold;");
                break;
            case "Futuro":
                lblStatus.setTextFill(Color.web("#6b7280"));
                break;
            default:
                lblStatus.setTextFill(Color.web("#4b5563"));
        }

        linha.getChildren().addAll(lblId, lblPeriodo, lblResumo, lblFaturamento, lblPagamento, lblStatus);

        return linha;
    }

    private void criarLinhasSemanasVazias(LocalDate dataInicio, LocalDate dataFim) {
        containerSemanas.getChildren().clear();
        LocalDate dataAtual = dataInicio;
        int semanaNumero = 1;
        int id = 201;

        while (!dataAtual.isAfter(dataFim)) {
            LocalDate inicioSemana = dataAtual;
            LocalDate fimSemana = dataAtual.plusDays(6);

            if (fimSemana.isAfter(dataFim)) {
                fimSemana = dataFim;
            }

            HBox linha = criarLinhaSemana(
                    id,
                    semanaNumero,
                    inicioSemana,
                    fimSemana,
                    0,
                    BigDecimal.ZERO,
                    "-",
                    "Sem dados"
            );

            containerSemanas.getChildren().add(linha);

            dataAtual = fimSemana.plusDays(1);
            semanaNumero++;
            id++;
        }
    }

    private void carregarDistribuicaoPagamentoMensal(LocalDate dataInicio, LocalDate dataFim) {
        try {
            System.out.println("Carregando distribuição de pagamento mensal...");
            Map<String, BigDecimal> distribuicao = relatorioService.getDistribuicaoPagamentoPeriodo(dataInicio, dataFim);

            if (distribuicao != null && !distribuicao.isEmpty()) {
                System.out.println("Distribuição de pagamento mensal: " + distribuicao);

                BigDecimal total = BigDecimal.ZERO;
                for (BigDecimal valor : distribuicao.values()) {
                    total = total.add(valor != null ? valor : BigDecimal.ZERO);
                }

                if (total.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal pixValor = distribuicao.getOrDefault("PIX", BigDecimal.ZERO);
                    BigDecimal creditoValor = distribuicao.getOrDefault("CREDITO",
                            distribuicao.getOrDefault("CARTÃO CRÉDITO", BigDecimal.ZERO));
                    BigDecimal debitoValor = distribuicao.getOrDefault("DEBITO",
                            distribuicao.getOrDefault("CARTÃO DÉBITO", BigDecimal.ZERO));
                    BigDecimal dinheiroValor = distribuicao.getOrDefault("DINHEIRO", BigDecimal.ZERO);

                    if (labelPixPercentMes != null) {
                        BigDecimal pixPercent = pixValor.divide(total, 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                        labelPixPercentMes.setText(formatarPercentual(pixPercent));
                    }

                    if (labelCreditoPercentMes != null) {
                        BigDecimal creditoPercent = creditoValor.divide(total, 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                        labelCreditoPercentMes.setText(formatarPercentual(creditoPercent));
                    }

                    if (labelDebitoPercentMes != null) {
                        BigDecimal debitoPercent = debitoValor.divide(total, 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                        labelDebitoPercentMes.setText(formatarPercentual(debitoPercent));
                    }

                    if (labelDinheiroPercentMes != null) {
                        BigDecimal dinheiroPercent = dinheiroValor.divide(total, 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                        labelDinheiroPercentMes.setText(formatarPercentual(dinheiroPercent));
                    }
                } else {
                    System.out.println("Total zero na distribuição de pagamento mensal");
                    definirDistribuicaoVazia();
                }
            } else {
                System.out.println("Nenhuma distribuição de pagamento encontrada para o mês");
                definirDistribuicaoVazia();
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar distribuição de pagamento mensal: " + e.getMessage());
            e.printStackTrace();
            definirDistribuicaoVazia();
        }
    }

    private String capitalizarPrimeiraLetra(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }

    private String formatarValorGrande(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) == 0) {
            return "R$ 0,00";
        }

        double valorDouble = valor.doubleValue();
        if (valorDouble >= 1000) {
            return String.format("R$ %,.2f", valorDouble).replace(",", ".");
        } else {
            return String.format("R$ %.2f", valorDouble);
        }
    }

    private String formatarPercentual(BigDecimal percentual) {
        if (percentual == null) {
            return "0%";
        }
        return String.format("%.1f%%", percentual.doubleValue());
    }

    private void definirResumoPadrao(LocalDate primeiroDiaMes) {
        if (labelPedidosMes != null) labelPedidosMes.setText("0");
        if (labelTotalMes != null) labelTotalMes.setText("R$ 0,00");
        if (labelTicketMedioMes != null) labelTicketMedioMes.setText("R$ 0,00");
        if (labelPeriodoMes != null) {
            String nomeMes = primeiroDiaMes.format(MONTH_FORMATTER);
            nomeMes = capitalizarPrimeiraLetra(nomeMes);
            labelPeriodoMes.setText(nomeMes);
        }
    }

    private void definirDistribuicaoVazia() {
        if (labelPixPercentMes != null) labelPixPercentMes.setText("0%");
        if (labelCreditoPercentMes != null) labelCreditoPercentMes.setText("0%");
        if (labelDebitoPercentMes != null) labelDebitoPercentMes.setText("0%");
        if (labelDinheiroPercentMes != null) labelDinheiroPercentMes.setText("0%");
    }

    private void definirValoresPadrao() {
        LocalDate hoje = LocalDate.now();
        YearMonth anoMesAtual = YearMonth.from(hoje);
        LocalDate primeiroDiaMes = anoMesAtual.atDay(1);

        definirResumoPadrao(primeiroDiaMes);
        definirDistribuicaoVazia();
        containerSemanas.getChildren().clear();
    }

    private void configurarBotoes() {
        if (btnExportar != null) {
            btnExportar.setOnAction(event -> handleExportarRelatorioMensal());
        }

        if (btnFechar != null) {
            btnFechar.setOnAction(event -> handleFechar());
        }
    }

    @FXML
    private void handleExportarRelatorioMensal() {
        try {
            System.out.println("Exportando relatório mensal...");

            LocalDateTime agora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String nomeArquivo = "relatorio_mensal_" + agora.format(formatter) + ".pdf";

            java.nio.file.Path caminho = java.nio.file.Paths.get(
                    System.getProperty("user.home"),
                    "Downloads",
                    nomeArquivo
            );

            java.nio.file.Files.createDirectories(caminho.getParent());

            LocalDate hoje = LocalDate.now();
            YearMonth anoMesAtual = YearMonth.from(hoje);
            LocalDate primeiroDiaMes = anoMesAtual.atDay(1);
            LocalDate ultimoDiaMes = anoMesAtual.atEndOfMonth();

            relatorioService.gerarRelatorioVendas(primeiroDiaMes, ultimoDiaMes, caminho);

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Relatório Mensal Exportado",
                    "Relatório exportado com sucesso!",
                    "O arquivo foi salvo em:\n" + caminho.toAbsolutePath()
            );

            System.out.println("Relatório mensal exportado para: " + caminho.toAbsolutePath());

        } catch (Exception e) {
            System.err.println("Erro ao exportar relatório mensal: " + e.getMessage());
            e.printStackTrace();

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro ao Exportar",
                    "Não foi possível exportar o relatório mensal.",
                    "Erro: " + e.getMessage()
            );
        }
    }

    @FXML
    private void handleFechar() {
        System.out.println("Fechando tela de relatório mensal...");
        Stage stage = (Stage) (btnFechar != null ? btnFechar.getScene().getWindow() :
                labelTotalMes.getScene().getWindow());
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