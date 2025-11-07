package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.Produto;
import com.example.pdv_galeteria.service.ProdutoService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtPreco;

    @FXML
    private TextField txtEstoque;

    @FXML
    private CheckBox checkPrincipal;

    @FXML
    private CheckBox checkBebida;

    @FXML
    private CheckBox checkAcompanhamento;

    @FXML
    private TextField txtOutraCategoria;

    @FXML
    private void initialize() {
        System.out.println("✅ ProdutoController inicializado!");
        System.out.println("✅ ProdutoService: " + (produtoService != null ? "INJETADO" : "NULO"));

        configurarCheckboxes();
    }

    private void configurarCheckboxes() {
        if (checkPrincipal != null) {
            checkPrincipal.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) desmarcarOutrosCheckboxes();
            });
        }

        if (checkBebida != null) {
            checkBebida.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) desmarcarOutrosCheckboxes();
            });
        }

        if (checkAcompanhamento != null) {
            checkAcompanhamento.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) desmarcarOutrosCheckboxes();
            });
        }

        if (txtOutraCategoria != null) {
            txtOutraCategoria.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.trim().isEmpty()) {
                    desmarcarTodosCheckboxes();
                }
            });
        }
    }

    private void desmarcarOutrosCheckboxes() {
        if (checkPrincipal != null && !checkPrincipal.isSelected()) {
            if (checkBebida != null) checkBebida.setSelected(false);
            if (checkAcompanhamento != null) checkAcompanhamento.setSelected(false);
            if (txtOutraCategoria != null) txtOutraCategoria.setText("");
        }
    }

    private void desmarcarTodosCheckboxes() {
        if (checkPrincipal != null) checkPrincipal.setSelected(false);
        if (checkBebida != null) checkBebida.setSelected(false);
        if (checkAcompanhamento != null) checkAcompanhamento.setSelected(false);
    }

    @FXML
    private void adicionarProduto() {
        try {
            System.out.println("🎯 Tentando adicionar produto...");
            System.out.println("🎯 ProdutoService: " + produtoService);

            if (produtoService == null) {
                mostrarAlerta("Erro", "Sistema não inicializado. Reinicie a aplicação.");
                return;
            }

            // Validar campos obrigatórios
            if (txtNome.getText().isEmpty() || txtPreco.getText().isEmpty() || txtEstoque.getText().isEmpty()) {
                mostrarAlerta("Erro", "Preencha todos os campos obrigatórios!");
                return;
            }

            // Criar e salvar produto
            Produto produto = new Produto();
            produto.setNome(txtNome.getText().trim());

            String precoText = txtPreco.getText().replace(",", ".");
            produto.setPreco(Double.parseDouble(precoText));

            produto.setQuantidade(Integer.parseInt(txtEstoque.getText().trim()));

            // Salvar no banco
            Produto produtoSalvo = produtoService.salvar(produto);
            System.out.println("✅ Produto salvo com ID: " + produtoSalvo.getId());

            mostrarAlerta("Sucesso", "Produto cadastrado com sucesso!");
            limparCampos();
            fecharJanela();

        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "Preço e estoque devem ser números válidos!\nEx: 25.50 ou 25,50");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao cadastrar produto: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void limparCampos() {
        if (txtNome != null) txtNome.clear();
        if (txtPreco != null) txtPreco.clear();
        if (txtEstoque != null) txtEstoque.clear();
        if (checkPrincipal != null) checkPrincipal.setSelected(false);
        if (checkBebida != null) checkBebida.setSelected(false);
        if (checkAcompanhamento != null) checkAcompanhamento.setSelected(false);
        if (txtOutraCategoria != null) txtOutraCategoria.clear();
    }

    private void fecharJanela() {
        if (txtNome != null && txtNome.getScene() != null) {
            Stage stage = (Stage) txtNome.getScene().getWindow();
            stage.close();
        }
    }
}