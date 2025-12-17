package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.Combo;
import com.example.pdv_galeteria.model.Produto;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import java.util.function.Consumer;

@Component
public class ConfirmacaoExclusaoController {

    @FXML
    private Label lblNomeProduto;

    @FXML
    private Label lblDetalhes;

    private Produto produto;
    private Combo combo;
    private Consumer<Boolean> onConfirmacaoListener;
    private Stage popupStage;

    @FXML
    public void initialize() {
        System.out.println("ConfirmacaoExclusaoController inicializado");
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
        this.combo = null;

        if (lblNomeProduto != null && produto != null) {
            lblNomeProduto.setText("Desativar '" + produto.getNome() + "'?");
            if (lblDetalhes != null) {
                lblDetalhes.setText(
                        "Este produto está sendo usado em combos!\n\n" +
                                "Ao desativar:\n" +
                                "• Estoque será zerado (0 unidades)\n" +
                                "• Produto sairá da lista de produtos\n" +
                                "• Continuará disponível nos combos existentes"
                );
            }
        }
    }

    public void setCombo(Combo combo) {
        this.combo = combo;
        this.produto = null;

        if (lblNomeProduto != null && combo != null) {
            lblNomeProduto.setText("Excluir combo '" + combo.getNome() + "'?");
            if (lblDetalhes != null) {
                lblDetalhes.setText(
                        "Esta ação não pode ser desfeita!\n\n" +
                                "Ao excluir:\n" +
                                "• O combo será removido permanentemente\n" +
                                "• Não afeta os produtos individuais\n" +
                                "• Histórico de vendas com este combo será mantido"
                );
            }
        }
    }

    public void setOnConfirmacaoListener(Consumer<Boolean> listener) {
        this.onConfirmacaoListener = listener;
    }

    @FXML
    private void confirmarExclusao() {
        System.out.println("Confirmação de exclusão clicada");

        if (onConfirmacaoListener != null) {
            onConfirmacaoListener.accept(true);
        }

        fecharPopup();
    }

    @FXML
    private void cancelarExclusao() {
        System.out.println("Exclusão cancelada");

        if (onConfirmacaoListener != null) {
            onConfirmacaoListener.accept(false);
        }

        fecharPopup();
    }

    private void fecharPopup() {
        if (popupStage != null) {
            popupStage.close();
        } else if (lblNomeProduto != null && lblNomeProduto.getScene() != null) {
            Stage stage = (Stage) lblNomeProduto.getScene().getWindow();
            stage.close();
        }
    }

    public void setPopupStage(Stage stage) {
        this.popupStage = stage;
    }
}