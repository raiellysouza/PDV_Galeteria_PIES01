package com.example.pdv_galeteria.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

@Component
public class PopupAberturaCaixaController implements Initializable {

    @FXML
    private TextField txtValorInicialPopup;

    @FXML
    private Button btnConfirmarAbertura;

    @FXML
    private Button btnCancelarAbertura;

    private Stage popupStage;
    private Consumer<BigDecimal> onConfirmCallback;
    private Runnable onCancelCallback;
    private boolean confirmado = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarValidacaoCampo();
        configurarEventos();
    }

    private void configurarValidacaoCampo() {
        if (txtValorInicialPopup != null) {
            txtValorInicialPopup.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.matches("\\d*([\\.,]\\d{0,2})?")) {
                    txtValorInicialPopup.setText(oldValue);
                }
            });
        }
    }

    private void configurarEventos() {
        if (txtValorInicialPopup != null) {
            txtValorInicialPopup.setOnAction(event -> confirmarAbertura());
        }
    }

    @FXML
    private void confirmarAbertura() {
        try {
            if (txtValorInicialPopup == null) {
                mostrarErro("Erro: Campo de valor não carregado corretamente!");
                return;
            }

            String textoValor = txtValorInicialPopup.getText();
            if (textoValor == null || textoValor.trim().isEmpty()) {
                mostrarErro("Informe o valor inicial!");
                return;
            }

            String valorStr = textoValor.replace(",", ".");
            BigDecimal valorInicial = new BigDecimal(valorStr);

            if (valorInicial.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarErro("O valor deve ser maior que zero!");
                return;
            }

            confirmado = true;

            if (onConfirmCallback != null) {
                onConfirmCallback.accept(valorInicial);
            }

            fecharPopup();
        } catch (NumberFormatException e) {
            mostrarErro("Valor inválido! Use números com até 2 casas decimais.");
        } catch (Exception e) {
            mostrarErro("Erro: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        confirmado = false;

        if (onCancelCallback != null) {
            onCancelCallback.run();
        }

        fecharPopup();
    }

    private void fecharPopup() {
        if (popupStage != null) {
            popupStage.close();
        }
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    public void setPopupStage(Stage popupStage) {
        this.popupStage = popupStage;
    }

    public void setOnConfirmCallback(Consumer<BigDecimal> onConfirmCallback) {
        this.onConfirmCallback = onConfirmCallback;
    }

    public void setOnCancelCallback(Runnable onCancelCallback) {
        this.onCancelCallback = onCancelCallback;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public BigDecimal getValorInicial() {
        try {
            if (txtValorInicialPopup != null) {
                String valorStr = txtValorInicialPopup.getText().replace(",", ".");
                return new BigDecimal(valorStr);
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public void focarCampoValor() {
        javafx.application.Platform.runLater(() -> {
            if (txtValorInicialPopup != null) {
                txtValorInicialPopup.requestFocus();
            }
        });
    }

    public void limparCampo() {
        if (txtValorInicialPopup != null) {
            txtValorInicialPopup.setText("");
        }
    }
}