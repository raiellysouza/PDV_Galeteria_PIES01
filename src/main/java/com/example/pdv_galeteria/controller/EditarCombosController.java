package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.Combo;
import com.example.pdv_galeteria.model.ComboItem;
import com.example.pdv_galeteria.model.Produto;
import com.example.pdv_galeteria.service.ComboService;
import com.example.pdv_galeteria.service.ProdutoService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class EditarCombosController {

    @Autowired
    private ComboService comboService;

    @Autowired
    private ProdutoService produtoService;

    @FXML private TextArea nomeComboField;
    @FXML private TextArea precoComboField;
    @FXML private TextArea nomeProdutoField;
    @FXML private TextArea quantidadeField;
    @FXML private TextArea produtosTextArea;
    @FXML private Button btnEditarCombo;

    private Combo comboAtual;
    private final List<ComboItem> itensDoCombo = new ArrayList<>();

    public void setCombo(Combo combo) {
        this.comboAtual = combo;
        carregarDadosCombo();
    }

    private void carregarDadosCombo() {
        if (comboAtual != null) {
            nomeComboField.setText(comboAtual.getNome());
            precoComboField.setText(String.valueOf(comboAtual.getPrecoTotal()));
            itensDoCombo.clear();
            itensDoCombo.addAll(comboAtual.getItensDoCombo());
            atualizarListaDeProdutos();
        }
    }

    @FXML
    private void adicionarProduto() {
        try {
            String nomeProduto = nomeProdutoField.getText().trim();
            String qtdStr = quantidadeField.getText().trim();

            if (nomeProduto.isEmpty() || qtdStr.isEmpty()) {
                mostrarAlerta("Aviso", "Preencha o nome do produto e a quantidade.", Alert.AlertType.WARNING);
                return;
            }

            int quantidade = Integer.parseInt(qtdStr);

            Produto produto = produtoService.buscarPrimeiroPorNome(nomeProduto);
            if (produto == null) {
                mostrarAlerta("Erro", "Produto não encontrado: " + nomeProduto, Alert.AlertType.ERROR);
                return;
            }
        Produto produto = produtoService.buscarPrimeiroPorNome(nomeProduto);
        if (produto == null) {
            mostrarAlerta("Erro", "Produto não encontrado: " + nomeProduto, Alert.AlertType.ERROR);
            return;
        }

            ComboItem item = new ComboItem();
            item.setProduto(produto);
            item.setQuantidade(quantidade);

            itensDoCombo.add(item);
            atualizarListaDeProdutos();

            nomeProdutoField.clear();
            quantidadeField.clear();

        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "Quantidade inválida. Digite um número inteiro.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void editarCombo() {
        try {
            if (comboAtual == null) {
                mostrarAlerta("Erro", "Nenhum combo selecionado para edição.", Alert.AlertType.ERROR);
                return;
            }

            String nome = nomeComboField.getText().trim();
            String precoStr = precoComboField.getText().trim();

            if (nome.isEmpty() || precoStr.isEmpty()) {
                mostrarAlerta("Aviso", "Preencha todos os campos obrigatórios.", Alert.AlertType.WARNING);
                return;
            }

            BigDecimal preco = new BigDecimal(precoStr.replace(",", "."));

            comboAtual.setNome(nome);
            comboAtual.setPrecoTotal(preco.doubleValue());
            comboAtual.setItensDoCombo(itensDoCombo);

            comboService.salvarCombo(comboAtual);

            mostrarAlerta("Sucesso", "Combo editado com sucesso!", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao editar combo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void atualizarListaDeProdutos() {
        StringBuilder sb = new StringBuilder();
        for (ComboItem item : itensDoCombo) {
            sb.append(item.getProduto().getNome())
                    .append(" - Quantidade: ").append(item.getQuantidade())
                    .append("\n");
        }
        produtosTextArea.setText(sb.toString());
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}