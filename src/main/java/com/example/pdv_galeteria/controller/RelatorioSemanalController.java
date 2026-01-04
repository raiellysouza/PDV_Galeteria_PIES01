package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.dto.PedidoResumoDTO;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Controller
public class RelatorioSemanalController implements Initializable {

    @FXML private Label labelPedidosSemana;
    @FXML private Label labelTotalSemana;
    @FXML private Label labelTicketMedioSemana;
    @FXML private Label labelPeriodoSemana;

    @FXML private Label labelPixPercentSemana;
    @FXML private Label labelCreditoPercentSemana;
    @FXML private Label labelDebitoPercentSemana;
    @FXML private Label labelDinheiroPercentSemana;

    @FXML private VBox vboxDiasSemana;

    @FXML private Button btnExportar;
    @FXML private Button btnAtualizar;
    @FXML private Button btnFechar;

    @Autowired
    private RelatorioService relatorioService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("EEEE", new java.util.Locale("pt", "BR"));
    private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("RelatorioSemanalController inicializando...");
        carregarDadosRelatorioSemanal();
        configurarBotoes();
    }

    private void carregarDadosRelatorioSemanal() {
        try {
            System.out.println("Carregando dados do relatório semanal...");

            LocalDate hoje = LocalDate.now();
            LocalDate dataInicio = hoje.minusDays(6);
            LocalDate dataFim = hoje;

            System.out.println("Período: " + dataInicio + " a " + dataFim);

            carregarResumoPeriodo(dataInicio, dataFim);

            carregarDetalhamentoDias(dataInicio, dataFim);

            carregarDistribuicaoPagamento(dataInicio, dataFim);

            System.out.println("Dados do relatório semanal carregados com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro ao carregar dados do relatório semanal: " + e.getMessage());
            e.printStackTrace();
            mostrarMensagemErro("Erro ao carregar dados do relatório: " + e.getMessage());
            definirValoresPadrao();
        }
    }

    private void carregarResumoPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        try {
            RelatorioVendasDTO relatorio = relatorioService.getRelatorioVendasPeriodo(dataInicio, dataFim);

            if (relatorio != null) {
                System.out.println("Resumo do período carregado: " + relatorio);

                if (labelPedidosSemana != null) {
                    labelPedidosSemana.setText(String.valueOf(relatorio.getTotalPedidos()));
                }

                if (labelTotalSemana != null) {
                    labelTotalSemana.setText(relatorio.getTotalVendasFormatado());
                }

                if (labelTicketMedioSemana != null) {
                    labelTicketMedioSemana.setText(relatorio.getValorMedioPedidoFormatado());
                }

                if (labelPeriodoSemana != null) {
                    String periodo = dataInicio.format(SHORT_DATE_FORMATTER) + " a " + dataFim.format(SHORT_DATE_FORMATTER);
                    labelPeriodoSemana.setText(periodo);
                }
            } else {
                System.out.println("Nenhum dado encontrado para o período");
                definirResumoPadrao(dataInicio, dataFim);
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar resumo do período: " + e.getMessage());
            definirResumoPadrao(dataInicio, dataFim);
        }
    }

    private void carregarDetalhamentoDias(LocalDate dataInicio, LocalDate dataFim) {
        try {
            System.out.println("Carregando faturamento por dia...");
            vboxDiasSemana.getChildren().clear();

            Map<LocalDate, RelatorioVendasDTO> vendasPorDia = relatorioService.getVendasPorDia(dataInicio, dataFim);

            if (vendasPorDia != null && !vendasPorDia.isEmpty()) {
                BigDecimal maiorFaturamento = BigDecimal.ZERO;
                for (RelatorioVendasDTO relatorio : vendasPorDia.values()) {
                    if (relatorio.getTotalVendas().compareTo(maiorFaturamento) > 0) {
                        maiorFaturamento = relatorio.getTotalVendas();
                    }
                }

                LocalDate dataAtual = dataInicio;
                int id = 101;

                while (!dataAtual.isAfter(dataFim)) {
                    RelatorioVendasDTO relatorioDia = vendasPorDia.get(dataAtual);
                    BigDecimal faturamento = relatorioDia != null ? relatorioDia.getTotalVendas() : BigDecimal.ZERO;
                    int pedidos = relatorioDia != null ? (int) relatorioDia.getTotalPedidos() : 0;

                    String observacao = gerarObservacaoDia(dataAtual, faturamento,
                            maiorFaturamento.compareTo(BigDecimal.ZERO) > 0 &&
                                    faturamento.compareTo(maiorFaturamento) == 0);

                    String formasPagamento = obterFormasPagamentoDia(dataAtual);

                    HBox linhaDia = criarLinhaDia(
                            id,
                            dataAtual,
                            observacao,
                            faturamento,
                            pedidos,
                            formasPagamento,
                            "08:00 - 22:00"
                    );

                    vboxDiasSemana.getChildren().add(linhaDia);

                    dataAtual = dataAtual.plusDays(1);
                    id++;
                }
            } else {
                System.out.println("Nenhum dado de vendas por dia encontrado");
                criarLinhasVazias(dataInicio, dataFim);
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar faturamento por dia: " + e.getMessage());
            e.printStackTrace();
            criarLinhasVazias(dataInicio, dataFim);
        }
    }

    private String obterFormasPagamentoDia(LocalDate data) {
        try {
            Map<String, BigDecimal> distribuicao = relatorioService.getDistribuicaoPagamentoPeriodo(data, data);

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
            System.err.println("Erro ao obter formas de pagamento do dia: " + e.getMessage());
        }

        return "Diversos";
    }

    private void criarLinhasVazias(LocalDate dataInicio, LocalDate dataFim) {
        vboxDiasSemana.getChildren().clear();
        LocalDate dataAtual = dataInicio;
        int id = 101;

        while (!dataAtual.isAfter(dataFim)) {
            HBox linha = criarLinhaDia(
                    id,
                    dataAtual,
                    "Sem vendas registradas",
                    BigDecimal.ZERO,
                    0,
                    "-",
                    "08:00 - 22:00"
            );

            vboxDiasSemana.getChildren().add(linha);

            dataAtual = dataAtual.plusDays(1);
            id++;
        }
    }

    private String gerarObservacaoDia(LocalDate data, BigDecimal faturamento, boolean ehDiaMaiorMovimento) {
        if (faturamento.compareTo(BigDecimal.ZERO) == 0) {
            return "Sem vendas registradas";
        }

        if (ehDiaMaiorMovimento) {
            return "Dia com maior movimento";
        }

        java.time.DayOfWeek diaSemana = data.getDayOfWeek();

        switch (diaSemana) {
            case MONDAY:
                return "Movimento típico de segunda";
            case FRIDAY:
                return "Movimento intenso (sexta-feira)";
            case SATURDAY:
                return "Movimento forte (sábado)";
            case SUNDAY:
                return "Movimento de final de semana";
            default:
                return "Movimento moderado";
        }
    }

    private HBox criarLinhaDia(int id, LocalDate data, String observacao,
                               BigDecimal faturamento, int pedidos,
                               String formaPagamento, String horario) {
        HBox linha = new HBox(10);
        linha.setPadding(new Insets(12, 10, 12, 10));

        String corFundo = (id % 2 == 0) ? "#F9FAFB" : "white";
        linha.setStyle("-fx-background-color: " + corFundo + "; " +
                "-fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;");

        Label lblId = new Label("#" + id);
        lblId.setPrefWidth(40);
        lblId.setTextFill(Color.web("#4b5563"));

        String diaSemana = capitalizarPrimeiraLetra(data.format(DAY_FORMATTER));
        Label lblDia = new Label(diaSemana);
        lblDia.setPrefWidth(120);
        lblDia.setTextFill(Color.web("#1f2937"));

        Label lblObservacao = new Label(observacao);
        lblObservacao.setPrefWidth(180);
        lblObservacao.setTextFill(Color.web("#4b5563"));

        String textoFaturamento = formatarValor(faturamento);
        Label lblFaturamento = new Label(textoFaturamento);
        lblFaturamento.setPrefWidth(100);
        lblFaturamento.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        if (faturamento.compareTo(BigDecimal.ZERO) > 0) {
            lblFaturamento.setTextFill(Color.web("#16a34a"));
        } else {
            lblFaturamento.setTextFill(Color.web("#6b7280"));
        }

        Label lblPedidos = new Label(String.valueOf(pedidos));
        lblPedidos.setPrefWidth(80);
        lblPedidos.setTextFill(Color.web("#4b5563"));

        Label lblPagamento = new Label(formaPagamento);
        lblPagamento.setPrefWidth(120);
        lblPagamento.setTextFill(Color.web("#4b5563"));

        Label lblHorario = new Label(horario);
        lblHorario.setPrefWidth(100);
        lblHorario.setTextFill(Color.web("#4b5563"));

        linha.getChildren().addAll(lblId, lblDia, lblObservacao, lblFaturamento,
                lblPedidos, lblPagamento, lblHorario);

        return linha;
    }

    private void carregarDistribuicaoPagamento(LocalDate dataInicio, LocalDate dataFim) {
        try {
            System.out.println("Carregando distribuição de pagamento semanal...");
            Map<String, BigDecimal> distribuicao = relatorioService.getDistribuicaoPagamentoPeriodo(dataInicio, dataFim);

            if (distribuicao != null && !distribuicao.isEmpty()) {
                System.out.println("Distribuição de pagamento semanal: " + distribuicao);

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

                    if (labelPixPercentSemana != null) {
                        BigDecimal pixPercent = pixValor.divide(total, 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                        labelPixPercentSemana.setText(formatarPercentual(pixPercent));
                    }

                    if (labelCreditoPercentSemana != null) {
                        BigDecimal creditoPercent = creditoValor.divide(total, 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                        labelCreditoPercentSemana.setText(formatarPercentual(creditoPercent));
                    }

                    if (labelDebitoPercentSemana != null) {
                        BigDecimal debitoPercent = debitoValor.divide(total, 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                        labelDebitoPercentSemana.setText(formatarPercentual(debitoPercent));
                    }

                    if (labelDinheiroPercentSemana != null) {
                        BigDecimal dinheiroPercent = dinheiroValor.divide(total, 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                        labelDinheiroPercentSemana.setText(formatarPercentual(dinheiroPercent));
                    }
                } else {
                    System.out.println("Total zero na distribuição de pagamento");
                    definirDistribuicaoVazia();
                }
            } else {
                System.out.println("Nenhuma distribuição de pagamento encontrada para o período");
                definirDistribuicaoVazia();
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar distribuição de pagamento: " + e.getMessage());
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

    private String formatarValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) == 0) {
            return "R$ 0,00";
        }
        return String.format("R$ %.2f", valor.doubleValue());
    }

    private String formatarPercentual(BigDecimal percentual) {
        if (percentual == null) {
            return "0%";
        }
        return String.format("%.1f%%", percentual.doubleValue());
    }

    private void definirResumoPadrao(LocalDate dataInicio, LocalDate dataFim) {
        if (labelPedidosSemana != null) labelPedidosSemana.setText("0");
        if (labelTotalSemana != null) labelTotalSemana.setText("R$ 0,00");
        if (labelTicketMedioSemana != null) labelTicketMedioSemana.setText("R$ 0,00");
        if (labelPeriodoSemana != null) {
            String periodo = dataInicio.format(SHORT_DATE_FORMATTER) + " a " + dataFim.format(SHORT_DATE_FORMATTER);
            labelPeriodoSemana.setText(periodo);
        }
    }

    private void definirDistribuicaoVazia() {
        if (labelPixPercentSemana != null) labelPixPercentSemana.setText("0%");
        if (labelCreditoPercentSemana != null) labelCreditoPercentSemana.setText("0%");
        if (labelDebitoPercentSemana != null) labelDebitoPercentSemana.setText("0%");
        if (labelDinheiroPercentSemana != null) labelDinheiroPercentSemana.setText("0%");
    }

    private void definirValoresPadrao() {
        LocalDate hoje = LocalDate.now();
        LocalDate semanaPassada = hoje.minusDays(6);
        definirResumoPadrao(semanaPassada, hoje);
        definirDistribuicaoVazia();

        vboxDiasSemana.getChildren().clear();
    }

    private void configurarBotoes() {
        if (btnExportar != null) {
            btnExportar.setOnAction(event -> handleExportarRelatorioSemanal());
        }

        if (btnAtualizar != null) {
            btnAtualizar.setOnAction(event -> handleAtualizarRelatorio());
        }

        if (btnFechar != null) {
            btnFechar.setOnAction(event -> handleFechar());
        }
    }

    @FXML
    private void handleExportarRelatorioSemanal() {
        try {
            System.out.println("Exportando relatório semanal...");

            LocalDateTime agora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String nomeArquivo = "relatorio_semanal_" + agora.format(formatter) + ".pdf";

            java.nio.file.Path caminho = java.nio.file.Paths.get(
                    System.getProperty("user.home"),
                    "Downloads",
                    nomeArquivo
            );

            java.nio.file.Files.createDirectories(caminho.getParent());

            LocalDate dataFim = LocalDate.now();
            LocalDate dataInicio = dataFim.minusDays(6);

            relatorioService.gerarRelatorioVendas(dataInicio, dataFim, caminho);

            mostrarMensagem("Relatório Semanal Exportado",
                    "Relatório exportado com sucesso!",
                    "O arquivo foi salvo em:\n" + caminho.toAbsolutePath(),
                    Alert.AlertType.INFORMATION);

            System.out.println("Relatório semanal exportado para: " + caminho.toAbsolutePath());

        } catch (Exception e) {
            System.err.println("Erro ao exportar relatório semanal: " + e.getMessage());
            e.printStackTrace();

            mostrarMensagem("Erro ao Exportar",
                    "Não foi possível exportar o relatório semanal.",
                    "Erro: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAtualizarRelatorio() {
        System.out.println("Atualizando relatório semanal...");
        carregarDadosRelatorioSemanal();

        mostrarMensagem("Relatório Atualizado",
                null,
                "Relatório semanal atualizado com sucesso!",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleFechar() {
        System.out.println("Fechando tela de relatório semanal...");
        Stage stage = (Stage) (btnFechar != null ? btnFechar.getScene().getWindow() :
                (labelTotalSemana != null ? labelTotalSemana.getScene().getWindow() : null));
        if (stage != null) {
            stage.close();
        }
    }

    private void mostrarMensagem(String titulo, String cabecalho, String conteudo, Alert.AlertType tipo) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(tipo);
            alert.setTitle(titulo);
            alert.setHeaderText(cabecalho);
            alert.setContentText(conteudo);
            alert.showAndWait();
        });
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