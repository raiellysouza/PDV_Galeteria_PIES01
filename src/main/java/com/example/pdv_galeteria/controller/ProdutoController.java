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
    private TextField txtNomeEditar;

    @FXML
    private TextField txtPrecoEditar;

    private Produto produtoParaEdicao;
    private Runnable onEdicaoConcluidaCallback;


    @FXML
    private void initialize() {
        System.out.println("✅ ProdutoController inicializado!");
        System.out.println("✅ ProdutoService: " + (produtoService != null ? "INJETADO" : "NULO"));

        configurarCheckboxes();
    }

    private void configurarCheckboxes() {
        if (checkPrincipal != null) {
            checkPrincipal.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) desmarcarOutrosCheckboxes(checkPrincipal);
            });
        }

        if (checkBebida != null) {
            checkBebida.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) desmarcarOutrosCheckboxes(checkBebida);
            });
        }

        if (checkAcompanhamento != null) {
            checkAcompanhamento.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) desmarcarOutrosCheckboxes(checkAcompanhamento);
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

    private void desmarcarOutrosCheckboxes(CheckBox checkboxSelecionado) {
        if (checkPrincipal != null && checkPrincipal != checkboxSelecionado) {
            checkPrincipal.setSelected(false);
        }
        if (checkBebida != null && checkBebida != checkboxSelecionado) {
            checkBebida.setSelected(false);
        }
        if (checkAcompanhamento != null && checkAcompanhamento != checkboxSelecionado) {
            checkAcompanhamento.setSelected(false);
        }

        if (txtOutraCategoria != null) {
            txtOutraCategoria.setText("");
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

            if (txtNome.getText().isEmpty() || txtPreco.getText().isEmpty() || txtEstoque.getText().isEmpty()) {
                mostrarAlerta("Erro", "Preencha todos os campos obrigatórios!");
                return;
            }

            Produto produto = new Produto();
            produto.setNome(txtNome.getText().trim());

            String precoText = txtPreco.getText().replace(",", ".");
            produto.setPreco(Double.parseDouble(precoText));

            produto.setQuantidade(Integer.parseInt(txtEstoque.getText().trim()));

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

    @FXML
    private void editarProduto() {
        try {
            System.out.println("✏️ Iniciando edição de produto...");

            if (produtoService == null) {
                mostrarAlerta("Erro", "Sistema não inicializado. Reinicie a aplicação.");
                return;
            }

            if (txtNomeEditar.getText().isEmpty() || txtPrecoEditar.getText().isEmpty()) {
                mostrarAlerta("Erro", "Preencha todos os campos obrigatórios!");
                return;
            }

            if (produtoParaEdicao == null || produtoParaEdicao.getId() == null) {
                mostrarAlerta("Erro", "Nenhum produto selecionado para edição.");
                return;
            }

            produtoParaEdicao.setNome(txtNomeEditar.getText().trim());

            String precoText = txtPrecoEditar.getText().replace(",", ".");
            produtoParaEdicao.setPreco(Double.parseDouble(precoText));

            Produto produtoAtualizado = produtoService.salvar(produtoParaEdicao);
            System.out.println("✅ Produto atualizado com ID: " + produtoAtualizado.getId());

            mostrarAlerta("Sucesso", "Produto atualizado com sucesso!");

            fecharJanelaEdicao();

        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "Preço deve ser um número válido!\nEx: 25.50 ou 25,50");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao atualizar produto: " + e.getMessage());
        }
    }

    public void setProdutoParaEdicao(Produto produto) {
        this.produtoParaEdicao = produto;
        carregarDadosProdutoEdicao();
    }

    private void carregarDadosProdutoEdicao() {
        if (produtoParaEdicao != null && txtNomeEditar != null && txtPrecoEditar != null) {
            txtNomeEditar.setText(produtoParaEdicao.getNome());
            txtPrecoEditar.setText(String.format("%.2f", produtoParaEdicao.getPreco()));
            System.out.println("📝 Carregando dados para edição: " + produtoParaEdicao.getNome());
        }
    }

    public void setOnEdicaoConcluidaCallback(Runnable callback) {
        this.onEdicaoConcluidaCallback = callback;
    }

    private void fecharJanelaEdicao() {
        if (onEdicaoConcluidaCallback != null) {
            onEdicaoConcluidaCallback.run();
        }

        if (txtNomeEditar != null && txtNomeEditar.getScene() != null) {
            Stage stage = (Stage) txtNomeEditar.getScene().getWindow();
            stage.close();
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