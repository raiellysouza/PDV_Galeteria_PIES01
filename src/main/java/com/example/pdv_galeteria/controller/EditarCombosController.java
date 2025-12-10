package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.Combo;
import com.example.pdv_galeteria.model.ComboItem;
import com.example.pdv_galeteria.model.Produto;
import com.example.pdv_galeteria.service.ComboService;
import com.example.pdv_galeteria.service.ProdutoService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
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
    @FXML private TextArea produtosTextArea;
    @FXML private Button btnEditarCombo;

    private Combo comboAtual;
    private final List<ComboItem> itensDoCombo = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setCombo(Combo combo) {
        this.comboAtual = combo;
        carregarDadosCombo();
    }

    private void carregarDadosCombo() {
        if (comboAtual != null) {
            nomeComboField.setText(comboAtual.getNome());
            precoComboField.setText(String.valueOf(comboAtual.getPrecoTotal()));
            itensDoCombo.clear();
            if (comboAtual.getItensDoCombo() != null) {
                itensDoCombo.addAll(comboAtual.getItensDoCombo());
            }
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
                item.setCombo(comboAtual); // Associa ao combo atual
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
    private void removerProduto() {
        try {
            String nomeProduto = nomeProdutoField.getText().trim();

            if (nomeProduto.isEmpty()) {
                mostrarAlerta("Aviso", "Digite o nome do produto a ser removido.", Alert.AlertType.WARNING);
                return;
            }

            boolean removido = itensDoCombo.removeIf(item ->
                    item.getProduto().getNome().equalsIgnoreCase(nomeProduto));

            if (removido) {
                atualizarListaDeProdutos();
                mostrarAlerta("Sucesso", "Produto removido do combo.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Aviso", "Produto não encontrado no combo.", Alert.AlertType.WARNING);
            }

            nomeProdutoField.clear();
            quantidadeField.clear();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao remover produto: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void limparItens() {
        itensDoCombo.clear();
        atualizarListaDeProdutos();
        mostrarAlerta("Informação", "Todos os itens foram removidos.", Alert.AlertType.INFORMATION);
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
            comboAtual.setItensDoCombo(itensDoCombo);

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

    @FXML
    private void cancelarEdicao() {
        try {
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Confirmar Cancelamento");
            confirmacao.setHeaderText(null);
            confirmacao.setContentText("Deseja cancelar a edição? Todas as alterações serão perdidas.");

            confirmacao.showAndWait().ifPresent(resposta -> {
                if (resposta.getButtonData().isDefaultButton()) {
                    fecharJanela();
                }
            });
        } catch (Exception e) {
            fecharJanela();
        }
    }

    private void atualizarListaDeProdutos() {
        StringBuilder sb = new StringBuilder();
        if (itensDoCombo.isEmpty()) {
            sb.append("Nenhum produto adicionado.");
        } else {
            sb.append("Produtos no combo:\n\n");
            double precoTotal = 0.0;

            for (ComboItem item : itensDoCombo) {
                Produto produto = item.getProduto();
                double precoItem = produto.getPreco() * item.getQuantidade();
                precoTotal += precoItem;

                sb.append("• ").append(produto.getNome())
                        .append(" - Quantidade: ").append(item.getQuantidade())
                        .append(" - Preço unitário: R$ ").append(String.format("%.2f", produto.getPreco()))
                        .append(" - Total: R$ ").append(String.format("%.2f", precoItem))
                        .append("\n");
            }

            sb.append("\nPreço total dos itens: R$ ").append(String.format("%.2f", precoTotal));

            if (precoComboField != null && !precoComboField.getText().isEmpty()) {
                try {
                    BigDecimal precoAtual = new BigDecimal(precoComboField.getText().replace(",", "."));
                    if (Math.abs(precoAtual.doubleValue() - precoTotal) > 0.01) {
                        precoComboField.setText(String.format("%.2f", precoTotal));
                    }
                } catch (Exception e) {
                }
            }
        }

        produtosTextArea.setText(sb.toString());
    }

    private void fecharJanela() {
        try {
            Stage stage = (Stage) nomeComboField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
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