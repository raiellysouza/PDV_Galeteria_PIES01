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
import javafx.scene.text.Text;
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
    @FXML private TextArea precoComboField;
    @FXML private TextArea nomeProdutoField;
    @FXML private TextArea quantidadeField;
    @FXML private VBox produtosListContainer;
    @FXML private Button btnEditarCombo;
    @FXML private Button btnAdicionar;

    private Combo comboAtual;
    private final List<ComboItem> itensDoCombo = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== INICIALIZANDO EDITAR COMBOS CONTROLLER ===");
        System.out.println("produtosListContainer: " + produtosListContainer);
        System.out.println("nomeComboField: " + nomeComboField);
        System.out.println("precoComboField: " + precoComboField);
        System.out.println("btnEditarCombo: " + btnEditarCombo);
        System.out.println("btnAdicionar: " + btnAdicionar);
        System.out.println("===============================================");
    }

    public void setCombo(Combo combo) {
        this.comboAtual = combo;
        System.out.println("Combo definido: " + (combo != null ? combo.getNome() : "null"));

        Platform.runLater(() -> {
            carregarDadosCombo();
        });
    }

    private void carregarDadosCombo() {
        try {
            if (comboAtual != null) {
                System.out.println("Carregando dados do combo: " + comboAtual.getNome());

                if (nomeComboField == null) {
                    System.err.println("ERRO: nomeComboField é nulo!");
                    return;
                }
                if (precoComboField == null) {
                    System.err.println("ERRO: precoComboField é nulo!");
                    return;
                }
                if (produtosListContainer == null) {
                    System.err.println("ERRO: produtosListContainer é nulo! Verifique o FXML.");
                    return;
                }

                nomeComboField.setText(comboAtual.getNome());
                precoComboField.setText(String.valueOf(comboAtual.getPrecoTotal()));

                itensDoCombo.clear();
                if (comboAtual.getItensDoCombo() != null) {
                    itensDoCombo.addAll(comboAtual.getItensDoCombo());
                    System.out.println("Itens carregados: " + itensDoCombo.size());
                }

                atualizarListaDeProdutos();
            } else {
                System.err.println("Combo atual é nulo!");
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar dados do combo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void atualizarListaDeProdutos() {
        try {
            if (produtosListContainer == null) {
                System.err.println("produtosListContainer é nulo!");
                return;
            }

            produtosListContainer.getChildren().clear();

            if (itensDoCombo.isEmpty()) {
                Label labelVazio = new Label("Nenhum produto adicionado ao combo.");
                labelVazio.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
                produtosListContainer.getChildren().add(labelVazio);
                return;
            }

            double precoTotalItens = 0.0;

            for (ComboItem item : itensDoCombo) {
                Produto produto = item.getProduto();
                double precoItem = produto.getPreco() * item.getQuantidade();
                precoTotalItens += precoItem;

                Pane cardProduto = criarCardProduto(item, produto, precoItem);
                produtosListContainer.getChildren().add(cardProduto);
            }

            Pane cardTotal = criarCardTotal(precoTotalItens);
            produtosListContainer.getChildren().add(cardTotal);

            if (precoComboField != null) {
                try {
                    String textoPreco = precoComboField.getText();
                    if (textoPreco != null && !textoPreco.trim().isEmpty()) {
                        BigDecimal precoAtual = new BigDecimal(textoPreco.replace(",", "."));
                        if (Math.abs(precoAtual.doubleValue() - precoTotalItens) > 0.01) {
                            precoComboField.setText(String.format("%.2f", precoTotalItens));
                        }
                    }
                } catch (Exception e) {
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao atualizar lista de produtos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Pane criarCardProduto(ComboItem item, Produto produto, double precoItem) {
        HBox card = new HBox(10);
        card.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-radius: 6;" +
                        "-fx-padding: 10;" +
                        "-fx-spacing: 10;"
        );
        card.setPrefWidth(540);

        Label nomeLabel = new Label(produto.getNome());
        nomeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        nomeLabel.setPrefWidth(200);

        Label quantidadeLabel = new Label("Qtd: " + item.getQuantidade() + " un.");
        quantidadeLabel.setStyle("-fx-font-size: 14px;");

        Label precoUnitLabel = new Label(String.format("R$ %.2f/un.", produto.getPreco()));
        precoUnitLabel.setStyle("-fx-font-size: 14px;");

        Label totalLabel = new Label(String.format("Total: R$ %.2f", precoItem));
        totalLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745; -fx-font-size: 14px;");

        Button btnRemover = new Button("Remover");
        btnRemover.setStyle(
                "-fx-background-color: #dc3545;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );
        btnRemover.setOnAction(e -> removerProdutoEspecifico(item));

        card.getChildren().addAll(nomeLabel, quantidadeLabel, precoUnitLabel, totalLabel, btnRemover);

        return card;
    }

    private Pane criarCardTotal(double precoTotalItens) {
        HBox card = new HBox(10);
        card.setStyle(
                "-fx-background-color: #e9ecef;" +
                        "-fx-border-color: #ced4da;" +
                        "-fx-border-radius: 6;" +
                        "-fx-padding: 10;" +
                        "-fx-spacing: 10;"
        );
        card.setPrefWidth(540);

        Label labelTotal = new Label("TOTAL DOS ITENS:");
        labelTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label valorTotal = new Label(String.format("R$ %.2f", precoTotalItens));
        valorTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #28a745;");

        card.getChildren().addAll(labelTotal, valorTotal);

        return card;
    }

    private void removerProdutoEspecifico(ComboItem itemParaRemover) {
        try {
            itensDoCombo.remove(itemParaRemover);
            atualizarListaDeProdutos();
            mostrarAlerta("Sucesso", "Produto removido do combo.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            System.err.println("Erro ao remover produto específico: " + e.getMessage());
            mostrarAlerta("Erro", "Erro ao remover produto: " + e.getMessage(), Alert.AlertType.ERROR);
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
            if (quantidade <= 0) {
                mostrarAlerta("Erro", "Quantidade deve ser maior que zero.", Alert.AlertType.ERROR);
                return;
            }

            Produto produto = produtoService.buscarPrimeiroPorNome(nomeProduto);
            if (produto == null) {
                mostrarAlerta("Erro", "Produto não encontrado: " + nomeProduto, Alert.AlertType.ERROR);
                return;
            }

            boolean produtoExistente = false;
            for (ComboItem item : itensDoCombo) {
                if (item.getProduto().getId().equals(produto.getId())) {
                    item.setQuantidade(item.getQuantidade() + quantidade);
                    produtoExistente = true;
                    break;
                }
            }

            if (!produtoExistente) {
                ComboItem item = new ComboItem();
                item.setProduto(produto);
                item.setQuantidade(quantidade);
                item.setCombo(comboAtual);
                itensDoCombo.add(item);
            }

            atualizarListaDeProdutos();
            nomeProdutoField.clear();
            quantidadeField.clear();

        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "Quantidade inválida. Digite um número inteiro.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao adicionar produto: " + e.getMessage(), Alert.AlertType.ERROR);
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

            if (itensDoCombo.isEmpty()) {
                mostrarAlerta("Aviso", "Adicione ao menos um produto ao combo.", Alert.AlertType.WARNING);
                return;
            }

            BigDecimal preco = new BigDecimal(precoStr.replace(",", "."));

            comboAtual.setNome(nome);
            comboAtual.setPrecoTotal(preco.doubleValue());
            comboAtual.setItensDoCombo(new ArrayList<>(itensDoCombo));

            comboService.salvarCombo(comboAtual);

            mostrarAlerta("Sucesso", "Combo editado com sucesso!", Alert.AlertType.INFORMATION);

            fecharJanela();

        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "Preço inválido. Use números com ponto ou vírgula.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao editar combo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void fecharJanela() {
        try {
            Stage stage = (Stage) nomeComboField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}