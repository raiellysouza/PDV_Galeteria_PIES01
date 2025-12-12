package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.Caixa;
import com.example.pdv_galeteria.service.CaixaService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.Consumer;

@Component
public class PopupFechamentoCaixaController {

    @Autowired
    private CaixaService caixaService;

    @FXML
    private TextField txtValorFinal;

    @FXML
    private Button btnConfirmar;

    private Stage popupStage;
    private Consumer<BigDecimal> onConfirmCallback;
    private Caixa caixaAtual;

    private DecimalFormat df;

    public void setPopupStage(Stage popupStage) {
        this.popupStage = popupStage;
    }

    public void setOnConfirmCallback(Consumer<BigDecimal> callback) {
        this.onConfirmCallback = callback;
    }

    @FXML
    public void initialize() {
        System.out.println("PopupFechamentoCaixaController inicializado");

        df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(new Locale("pt", "BR")));

        carregarDadosCaixa();

        Platform.runLater(() -> {
            txtValorFinal.requestFocus();
            txtValorFinal.selectAll();
        });
    }

    private void carregarDadosCaixa() {
        try {
            caixaAtual = caixaService.getCaixaAbertoDoDia()
                    .orElseThrow(() -> new RuntimeException("Não há caixa aberto para fechar!"));

            BigDecimal valorInicial = caixaAtual.getValorInicial() != null ?
                    caixaAtual.getValorInicial() : BigDecimal.ZERO;
            BigDecimal totalEntradas = caixaAtual.getTotalEntradas() != null ?
                    caixaAtual.getTotalEntradas() : BigDecimal.ZERO;
            BigDecimal totalSaidas = caixaAtual.getTotalSaidas() != null ?
                    caixaAtual.getTotalSaidas() : BigDecimal.ZERO;

            BigDecimal saldoCalculado = valorInicial.add(totalEntradas).subtract(totalSaidas);

            txtValorFinal.setText(df.format(saldoCalculado));

            System.out.println("=== DADOS PARA FECHAMENTO ===");
            System.out.println("Valor Inicial: R$ " + formatarValor(valorInicial));
            System.out.println("Total Entradas: R$ " + formatarValor(totalEntradas));
            System.out.println("Total Saídas: R$ " + formatarValor(totalSaidas));
            System.out.println("Saldo Calculado: R$ " + formatarValor(saldoCalculado));
            System.out.println("Valor sugerido no campo: " + txtValorFinal.getText());
            System.out.println("=============================");

        } catch (Exception e) {
            System.err.println("Erro ao carregar dados do caixa: " + e.getMessage());
            e.printStackTrace();
            txtValorFinal.setText("0,00");
        }
    }

    private String formatarValor(BigDecimal valor) {
        if (valor == null) return "0,00";
        return df.format(valor);
    }

    private String formatarValorParaInput(BigDecimal valor) {
        if (valor == null) return "0,00";
        return df.format(valor);
    }

    private BigDecimal parseValorDoTextField(String texto) {
        try {
            if (texto == null || texto.trim().isEmpty()) {
                return BigDecimal.ZERO;
            }
            texto = texto.trim();
            texto = texto.replace(".", "").replace(",", ".");

            return new BigDecimal(texto);
        } catch (Exception e) {
            System.err.println("Erro ao converter valor: '" + texto + "' - " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @FXML
    private void confirmarFechamento() {
        System.out.println("Confirmar fechamento clicado");

        try {
            String textoDigitado = txtValorFinal.getText();
            System.out.println("Texto digitado: '" + textoDigitado + "'");

            BigDecimal valorFinalDigitado = parseValorDoTextField(textoDigitado);
            System.out.println("Valor convertido: R$ " + formatarValor(valorFinalDigitado));

            if (valorFinalDigitado.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarAlerta("Valor inválido", "Digite um valor maior que zero!");
                txtValorFinal.requestFocus();
                txtValorFinal.selectAll();
                return;
            }

            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Confirmar Fechamento");
            confirmacao.setHeaderText("Deseja fechar o caixa?");
            confirmacao.setContentText("Valor final informado: R$ " + formatarValor(valorFinalDigitado));

            if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            if (onConfirmCallback != null) {
                System.out.println("Executando callback com valor: R$ " + formatarValor(valorFinalDigitado));
                onConfirmCallback.accept(valorFinalDigitado);
            }

            if (popupStage != null) {
                popupStage.close();
            }

        } catch (Exception e) {
            System.err.println("Erro ao confirmar fechamento: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Erro", "Valor inválido! Use números (ex: 1500,50)");
            txtValorFinal.requestFocus();
            txtValorFinal.selectAll();
        }
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}