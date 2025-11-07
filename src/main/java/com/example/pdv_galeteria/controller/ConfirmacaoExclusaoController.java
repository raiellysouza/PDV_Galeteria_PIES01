package com.example.pdv_galeteria.controller;

import javafx.fxml.FXML;
import javafx.stage.Stage;

public class ConfirmacaoExclusaoController {

    private Stage popupStage;
    private boolean confirmado = false;
    private Long produtoId;
    private Runnable onConfirmCallback;

    @FXML
    private void initialize() {
        System.out.println("✅ ConfirmacaoExclusaoController inicializado!");
    }

    @FXML
    private void confirmarExclusao() {
        confirmado = true;
        System.out.println("✅ Confirmação de exclusão para produto ID: " + produtoId);

        if (onConfirmCallback != null) {
            onConfirmCallback.run();
        }

        fecharPopup();
    }

    @FXML
    private void cancelarExclusao() {
        confirmado = false;
        System.out.println("❌ Exclusão cancelada");
        fecharPopup();
    }

    private void fecharPopup() {
        if (popupStage != null) {
            popupStage.close();
        }
    }

    public void setPopupStage(Stage popupStage) {
        this.popupStage = popupStage;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public void setOnConfirmCallback(Runnable onConfirmCallback) {
        this.onConfirmCallback = onConfirmCallback;
    }

    public boolean isConfirmado() {
        return confirmado;
    }
}