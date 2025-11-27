package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.Caixa;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

@Component
public class PopupAberturaCaixaController implements Initializable {

    // 🔥 ADICIONE esta linha - campo faltante
    @FXML private TextField txtValorInicialPopup;
    @FXML private Button btnConfirmarAbertura;
    @FXML private Button btnCancelarAbertura;

    @Setter
    private Stage popupStage;

    @Setter
    private Consumer<BigDecimal> onConfirmCallback;

    @Setter
    private Runnable onCancelCallback;

    private boolean confirmado = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar validação do campo numérico
        if (txtValorInicialPopup != null) {
            txtValorInicialPopup.textProperty().addListener((observable, oldValue, newValue) -> {
                // 🔥 CORREÇÃO: Verificar se newValue não é null antes de usar matches
                if (newValue != null && !newValue.matches("\\d*([\\.,]\\d{0,2})?")) {
                    txtValorInicialPopup.setText(oldValue);
                }
            });

            // Enter no campo confirma
            txtValorInicialPopup.setOnAction(event -> confirmarAbertura());
        }
    }

    @FXML
    private void confirmarAbertura() {
        try {
            // 🔥 CORREÇÃO: Verificação mais robusta
            if (txtValorInicialPopup == null) {
                mostrarErro("Erro: Campo de valor não carregado corretamente!");
                return;
            }

            String textoValor = txtValorInicialPopup.getText();
            if (textoValor == null || textoValor.trim().isEmpty()) {
                mostrarErro("Informe o valor inicial!");
                return;
            }

            // Converter para BigDecimal
            String valorStr = textoValor.replace(",", ".");
            BigDecimal valorInicial = new BigDecimal(valorStr);

            if (valorInicial.compareTo(BigDecimal.ZERO) < 0) {
                mostrarErro("O valor não pode ser negativo!");
                return;
            }

            confirmado = true;

            // Chamar callback se existir
            if (onConfirmCallback != null) {
                onConfirmCallback.accept(valorInicial);
            }

            // Fechar popup
            if (popupStage != null) {
                popupStage.close();
            }

        } catch (NumberFormatException e) {
            mostrarErro("Valor inválido! Use números com até 2 casas decimais.\nEx: 50,00 ou 100.50");
        } catch (Exception e) {
            mostrarErro("Erro: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        confirmado = false;

        // Chamar callback se existir
        if (onCancelCallback != null) {
            onCancelCallback.run();
        }

        if (popupStage != null) {
            popupStage.close();
        }
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

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    // Método para focar no campo automaticamente
    public void focarCampoValor() {
        javafx.application.Platform.runLater(() -> {
            if (txtValorInicialPopup != null) {
                txtValorInicialPopup.requestFocus();
            }
        });
    }

    // Método para limpar o campo
    public void limparCampo() {
        if (txtValorInicialPopup != null) {
            txtValorInicialPopup.setText("");
        }
    }
}