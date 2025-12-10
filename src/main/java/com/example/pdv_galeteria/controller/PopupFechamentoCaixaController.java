package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.Caixa;
import com.example.pdv_galeteria.service.CaixaService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PopupFechamentoCaixaController {

    @Autowired
    private CaixaService caixaService;

    @FXML
    private Label lblValorInicial;

    @FXML
    private Label lblTotalEntradas;

    @FXML
    private Label lblTotalSaidas;

    @FXML
    private Label lblSaldoAtual;

    @FXML
    private Label lblValorFinal;

    @FXML
    private Button btnConfirmarFechamento;

    @FXML
    private Button btnCancelarFechamento;

    private Stage popupStage;
    private Runnable onConfirmCallback;
    private Runnable onCancelCallback;

    public void setPopupStage(Stage popupStage) {
        this.popupStage = popupStage;
    }

    public void setOnConfirmCallback(Runnable callback) {
        this.onConfirmCallback = callback;
    }

    public void setOnCancelCallback(Runnable callback) {
        this.onCancelCallback = callback;
    }

    @FXML
    public void initialize() {
        System.out.println("PopupFechamentoCaixaController inicializado");
        carregarDadosCaixa();
    }

    private void carregarDadosCaixa() {
        try {
            Caixa caixa = caixaService.getCaixaDoDia()
                    .orElseThrow(() -> new RuntimeException("Não há caixa aberto para fechar!"));

            lblValorInicial.setText("R$ " + formatarValor(caixa.getValorInicial()));
            lblTotalEntradas.setText("R$ " + formatarValor(caixa.getTotalEntradas()));
            lblTotalSaidas.setText("R$ " + formatarValor(caixa.getTotalSaidas()));
            lblSaldoAtual.setText("R$ " + formatarValor(caixa.getSaldoAtual()));
            lblValorFinal.setText("R$ " + formatarValor(caixa.getSaldoAtual()));

            System.out.println("Dados do caixa carregados para fechamento:");
            System.out.println("- Valor Inicial: " + formatarValor(caixa.getValorInicial()));
            System.out.println("- Total Entradas: " + formatarValor(caixa.getTotalEntradas()));
            System.out.println("- Total Saídas: " + formatarValor(caixa.getTotalSaidas()));
            System.out.println("- Saldo Final: " + formatarValor(caixa.getSaldoAtual()));

        } catch (Exception e) {
            System.err.println("Erro ao carregar dados do caixa: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String formatarValor(BigDecimal valor) {
        if (valor == null) return "0,00";
        return String.format("%,.2f", valor).replace(",", "X").replace(".", ",").replace("X", ".");
    }

    @FXML
    private void confirmarFechamento() {
        System.out.println("Confirmar fechamento clicado");

        try {
            if (onConfirmCallback != null) {
                onConfirmCallback.run();
            }

            if (popupStage != null) {
                popupStage.close();
            }

        } catch (Exception e) {
            System.err.println("Erro ao confirmar fechamento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelar() {
        System.out.println("Cancelar fechamento clicado");

        if (onCancelCallback != null) {
            onCancelCallback.run();
        }

        if (popupStage != null) {
            popupStage.close();
        }
    }
}