package com.example.pdv_galeteria.controller;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class ConfirmacaoSaidaController {

    private boolean confirmado = false;
    private Stage popupStage;

    @FXML
    public void initialize() {
        System.out.println("ConfirmacaoSaidaController inicializado - TelaSairPrograma");
    }

    public void setPopupStage(Stage stage) {
        this.popupStage = stage;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    @FXML
    private void confirmarSaida() {
        System.out.println("=== CONFIRMAR SAÍDA DO SISTEMA ===");
        this.confirmado = true;
        fecharJanela();
    }

    @FXML
    private void cancelarSaida() {
        System.out.println("Saída do sistema cancelada pelo usuário");
        this.confirmado = false;
        fecharJanela();
    }

    private void fecharJanela() {
        try {
            if (popupStage != null) {
                popupStage.close();
            }
        } catch (Exception e) {
            System.err.println("Erro ao fechar janela: " + e.getMessage());
        }
    }
}