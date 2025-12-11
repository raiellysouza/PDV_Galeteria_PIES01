package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.Combo;
import com.example.pdv_galeteria.model.ComboItem;
import com.example.pdv_galeteria.model.Produto;
import com.example.pdv_galeteria.service.ComboService;
import com.example.pdv_galeteria.service.ProdutoService;
import javafx.fxml.FXML;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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

    @FXML private VBox produtosListContainer;

    @FXML private VBox sugestoesContainer;

    @FXML private TextArea nomeComboField;
    @FXML private TextArea precoComboField;
    @FXML private TextArea nomeProdutoField;
    @FXML private TextArea quantidadeField;
    @FXML private Button btnEditarCombo;

    private Combo comboAtual;
    private final List<ComboItem> itensDoCombo = new ArrayList<>();


    public void setCombo(Combo combo) {
        this.comboAtual = combo;
        carregarDadosCombo();
    }

    @FXML
    public void initialize() {
        configurarAutoComplete();
    }

    private void carregarDadosCombo() {

        nomeComboField.setText(comboAtual.getNome());
        precoComboField.setText(String.valueOf(comboAtual.getPrecoTotal()));

        itensDoCombo.clear();
        for (ComboItem itemOriginal : comboAtual.getItensDoCombo()) {
            ComboItem copia = new ComboItem();
            copia.setProduto(itemOriginal.getProduto());
            copia.setQuantidade(itemOriginal.getQuantidade());
            copia.setCombo(comboAtual);

            itensDoCombo.add(copia);
        }

        atualizarListaDeProdutos();
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

        ComboItem existente = null;
        for (ComboItem item : itensDoCombo) {
            if (item.getProduto().getId().equals(produto.getId())) {
                existente = item;
                break;
            }
        }

        if (existente != null) {
            existente.setQuantidade(existente.getQuantidade() + quantidade);
        } else {
            ComboItem novoItem = new ComboItem();
            novoItem.setProduto(produto);
            novoItem.setQuantidade(quantidade);
            itensDoCombo.add(novoItem);
        }

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

            for (ComboItem item : itensDoCombo) {
                item.setCombo(comboAtual);
            }

            comboService.salvarCombo(comboAtual);

            mostrarAlerta("Sucesso", "Combo editado com sucesso!", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao editar combo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void atualizarListaDeProdutos() {

        produtosListContainer.getChildren().clear();

        for (int i = 0; i < itensDoCombo.size(); i++) {

            ComboItem item = itensDoCombo.get(i);

            HBox linha = new HBox();
            linha.setSpacing(10);
            linha.setStyle("""
                    -fx-background-color: #f7f7f7;
                    -fx-padding: 10;
                    -fx-background-radius: 8;
                    -fx-border-radius: 8;
                    -fx-border-color: #ddd;
                    """);

            Label nome = new Label(item.getQuantidade() + "x  " + item.getProduto().getNome());
            nome.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

            Region espaco = new Region();
            HBox.setHgrow(espaco, Priority.ALWAYS);

            Button btnExcluir = new Button("🗑");
            btnExcluir.setStyle("""
                    -fx-background-color: transparent;
                    -fx-font-size: 16px;
                    -fx-cursor: hand;
                    """);

            final int index = i;
            btnExcluir.setOnAction(e -> {
                itensDoCombo.remove(index);
                atualizarListaDeProdutos();
            });

            linha.getChildren().addAll(nome, espaco, btnExcluir);
            produtosListContainer.getChildren().add(linha);
        }
    }

   private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

 private void configurarAutoComplete() {

    sugestoesContainer.setVisible(false);
    sugestoesContainer.setManaged(false); 

    nomeProdutoField.textProperty().addListener((obs, oldValue, newValue) -> {

        sugestoesContainer.getChildren().clear();

        if (newValue == null || newValue.trim().isEmpty()) {
            sugestoesContainer.setVisible(false);
            sugestoesContainer.setManaged(false);
            return;
        }

        List<Produto> produtos = produtoService.buscarListaPorNome(newValue);

        if (produtos.isEmpty()) {
            sugestoesContainer.setVisible(false);
            sugestoesContainer.setManaged(false);
            return;
        }

        sugestoesContainer.setVisible(true);
        sugestoesContainer.setManaged(true);

        for (Produto p : produtos) {

            Label opcao = new Label(p.getNome());
            opcao.setMaxWidth(Double.MAX_VALUE); 
            opcao.setStyle("""
                    -fx-background-color: white;
                    -fx-padding: 8 12;
                    -fx-font-size: 14px;
                    -fx-border-width: 0;   /* SEM BORDA */
                    -fx-cursor: hand;
                    """);

            opcao.setOnMouseClicked(e -> {
                nomeProdutoField.setText(p.getNome());
                sugestoesContainer.setVisible(false);
                sugestoesContainer.setManaged(false);
            });

            opcao.setOnMouseEntered(e -> opcao.setStyle("""
                    -fx-background-color: #efefef;
                    -fx-padding: 8 12;
                    -fx-font-size: 14px;
                    -fx-border-width: 0;
                    -fx-cursor: hand;
                    """));

            opcao.setOnMouseExited(e -> opcao.setStyle("""
                    -fx-background-color: white;
                    -fx-padding: 8 12;
                    -fx-font-size: 14px;
                    -fx-border-width: 0;
                    -fx-cursor: hand;
                    """));

            sugestoesContainer.getChildren().add(opcao);
        }
    });
}

}