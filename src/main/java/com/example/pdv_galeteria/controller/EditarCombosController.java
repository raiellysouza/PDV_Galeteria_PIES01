package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.Combo;
import com.example.pdv_galeteria.model.ComboItem;
import com.example.pdv_galeteria.model.Produto;
import com.example.pdv_galeteria.service.ComboService;
import com.example.pdv_galeteria.service.ProdutoService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class EditarCombosController implements Initializable {

    @Autowired
    private ComboService comboService;

    @Autowired
    private ProdutoService produtoService;

    @FXML private TextArea nomeComboField;
    @FXML private TextField precoComboField;
    @FXML private TextArea nomeProdutoField;
    @FXML private TextArea quantidadeField;
    @FXML private VBox produtosListContainer;
    @FXML private VBox sugestoesContainer;
    @FXML private Button btnEditarCombo;
    @FXML private Button btnAdicionar;

    private Combo comboAtual;
    private final List<ComboItem> itensDoCombo = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarAutoComplete();
    }

    public void setCombo(Combo combo) {
        this.comboAtual = combo;
        Platform.runLater(this::carregarDadosCombo);
    }

    private void carregarDadosCombo() {

        if (comboAtual == null) return;

        nomeComboField.setText(comboAtual.getNome());
        precoComboField.setText(String.valueOf(comboAtual.getPrecoTotal()));

        itensDoCombo.clear();
        if (comboAtual.getItensDoCombo() != null) {
            itensDoCombo.addAll(comboAtual.getItensDoCombo());
        }

        atualizarListaDeProdutos();
    }

    private void atualizarListaDeProdutos() {

        produtosListContainer.getChildren().clear();

        if (itensDoCombo.isEmpty()) {
            Label vazio = new Label("Nenhum produto adicionado ao combo.");
            vazio.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
            produtosListContainer.getChildren().add(vazio);
            return;
        }

        for (ComboItem item : itensDoCombo) {
            produtosListContainer.getChildren().add(criarCardProduto(item));
        }
    }

    private Pane criarCardProduto(ComboItem item) {

        Produto produto = item.getProduto();

        HBox card = new HBox(10);
        card.setStyle("""
            -fx-background-color: #f8f9fa;
            -fx-border-color: #dee2e6;
            -fx-border-radius: 6;
            -fx-padding: 10;
        """);
        card.setPrefWidth(540);

        Label nomeLabel = new Label(produto.getNome());
        nomeLabel.setPrefWidth(180);
        nomeLabel.setStyle("-fx-font-weight: bold;");

        Button btnMenos = new Button("−");
        btnMenos.setOnAction(e -> diminuirQuantidade(item));

        Label qtdLabel = new Label(String.valueOf(item.getQuantidade()));
        qtdLabel.setMinWidth(30);
        qtdLabel.setStyle("-fx-alignment: center;");

        Button btnMais = new Button("+");
        btnMais.setOnAction(e -> aumentarQuantidade(item));

        Label precoUnit = new Label(
            String.format("R$ %.2f/un.", produto.getPreco())
        );

        Button btnRemover = new Button("🗑");
        btnRemover.setStyle(
            "-fx-background-color: #dc3545; -fx-text-fill: white;"
        );
        btnRemover.setOnAction(e -> removerProdutoEspecifico(item));

        card.getChildren().addAll(
            nomeLabel,
            btnMenos,
            qtdLabel,
            btnMais,
            precoUnit,
            btnRemover
        );

        return card;
    }

    private void aumentarQuantidade(ComboItem item) {
        item.setQuantidade(item.getQuantidade() + 1);
        atualizarListaDeProdutos();
    }

    private void diminuirQuantidade(ComboItem item) {
        int novaQtd = item.getQuantidade() - 1;

        if (novaQtd <= 0) {
            itensDoCombo.remove(item);
        } else {
            item.setQuantidade(novaQtd);
        }

        atualizarListaDeProdutos();
    }

    private void removerProdutoEspecifico(ComboItem item) {
        itensDoCombo.remove(item);
        atualizarListaDeProdutos();
    }

    @FXML
    private void adicionarProduto() {

        try {
            String nomeProduto = nomeProdutoField.getText().trim();
            int quantidade = Integer.parseInt(quantidadeField.getText().trim());

            Produto produto = produtoService.buscarPrimeiroPorNome(nomeProduto);
            if (produto == null) {
                mostrarAlerta("Erro", "Produto não encontrado.", Alert.AlertType.ERROR);
                return;
            }

            for (ComboItem item : itensDoCombo) {
                if (item.getProduto().getId().equals(produto.getId())) {
                    item.setQuantidade(item.getQuantidade() + quantidade);
                    atualizarListaDeProdutos();
                    return;
                }
            }

            ComboItem novoItem = new ComboItem();
            novoItem.setProduto(produto);
            novoItem.setQuantidade(quantidade);
            novoItem.setCombo(comboAtual);

            itensDoCombo.add(novoItem);
            atualizarListaDeProdutos();

            nomeProdutoField.clear();
            quantidadeField.clear();

        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao adicionar produto.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void editarCombo() {

        try {
            comboAtual.setNome(nomeComboField.getText().trim());
            comboAtual.setPrecoTotal(
                new BigDecimal(precoComboField.getText().replace(",", ".")).doubleValue()
            );
            comboAtual.setItensDoCombo(new ArrayList<>(itensDoCombo));

            comboService.salvarCombo(comboAtual);

            mostrarAlerta("Sucesso", "Combo editado com sucesso!", Alert.AlertType.INFORMATION);
            fecharJanela();

        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao salvar combo.", Alert.AlertType.ERROR);
        }
    }

    private void fecharJanela() {
        Stage stage = (Stage) nomeComboField.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void configurarAutoComplete() {

        sugestoesContainer.setVisible(false);
        sugestoesContainer.setManaged(false);

        nomeProdutoField.textProperty().addListener((obs, o, n) -> {

            sugestoesContainer.getChildren().clear();

            if (n == null || n.isBlank()) {
                sugestoesContainer.setVisible(false);
                sugestoesContainer.setManaged(false);
                return;
            }

            List<Produto> produtos = produtoService.buscarListaPorNome(n);

            if (produtos.isEmpty()) return;

            sugestoesContainer.setVisible(true);
            sugestoesContainer.setManaged(true);

            for (Produto p : produtos) {

                Label opcao = new Label(p.getNome());
                opcao.setStyle("""
                    -fx-background-color: white;
                    -fx-padding: 8 12;
                    -fx-cursor: hand;
                """);

                opcao.setOnMouseClicked(e -> {
                    nomeProdutoField.setText(p.getNome());
                    sugestoesContainer.setVisible(false);
                    sugestoesContainer.setManaged(false);
                });

                sugestoesContainer.getChildren().add(opcao);
            }
        });
    }
}
