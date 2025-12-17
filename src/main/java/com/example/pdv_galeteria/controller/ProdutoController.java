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

import java.util.Optional;

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
        System.out.println("ProdutoController inicializado!");
        System.out.println("ProdutoService: " + (produtoService != null ? "INJETADO" : "NULO"));

        configurarCheckboxes();
        configurarValidacaoCamposComVirgula();
    }

    private void configurarValidacaoCamposComVirgula() {
        if (txtPreco != null) {
            txtPreco.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*([,\\.]\\d{0,2})?")) {
                    txtPreco.setText(oldValue);
                }
            });
        }

        if (txtPrecoEditar != null) {
            txtPrecoEditar.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*([,\\.]\\d{0,2})?")) {
                    txtPrecoEditar.setText(oldValue);
                }
            });
        }

        if (txtEstoque != null) {
            txtEstoque.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    txtEstoque.setText(oldValue);
                }
            });
        }
    }

    private void configurarValidacaoCampos() {
        if (txtPreco != null) {
            txtPreco.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*([,\\.]\\d{0,2})?")) {
                    txtPreco.setText(oldValue);
                }
            });
        }

        if (txtPrecoEditar != null) {
            txtPrecoEditar.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*([,\\.]\\d{0,2})?")) {
                    txtPrecoEditar.setText(oldValue);
                }
            });
        }

        if (txtEstoque != null) {
            txtEstoque.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    txtEstoque.setText(oldValue);
                }
            });
        }
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
            System.out.println("Tentando adicionar produto...");
            System.out.println("ProdutoService: " + produtoService);

            if (produtoService == null) {
                mostrarAlerta("Erro", "Sistema não inicializado. Reinicie a aplicação.");
                return;
            }

            if (!validarCamposCadastro()) {
                return;
            }

            String nome = txtNome.getText().trim();

            String precoText = txtPreco.getText().replace(",", ".");
            Double preco = Double.parseDouble(precoText);

            Integer quantidade = Integer.parseInt(txtEstoque.getText().trim());

            Produto produto = new Produto();
            produto.setNome(nome);
            produto.setPreco(preco);
            produto.setQuantidade(quantidade);

            Produto produtoSalvo = produtoService.salvar(produto);

            limparCampos();
            fecharJanela();

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Já existe um produto ativo")) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Produto Já Existe");
                alert.setHeaderText("Não é possível criar o produto");
                alert.setContentText(e.getMessage() +
                        "\n\nSoluções:\n" +
                        "1. Use um nome diferente\n" +
                        "2. Edite o produto existente\n" +
                        "3. Verifique se há produtos desativados com esse nome");
                alert.showAndWait();
            } else {
                mostrarAlerta("Erro", e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao cadastrar produto: " + e.getMessage());
        }
    }

    private boolean validarCamposCadastro() {
        StringBuilder erros = new StringBuilder();

        if (txtNome.getText().trim().isEmpty()) {
            erros.append("• Digite o nome do produto\n");
        }

        if (txtPreco.getText().trim().isEmpty()) {
            erros.append("• Digite o preço do produto\n");
        } else {
            try {
                double preco = Double.parseDouble(txtPreco.getText().replace(",", "."));
                if (preco <= 0) {
                    erros.append("• O preço deve ser maior que zero\n");
                }
            } catch (NumberFormatException e) {
                erros.append("• Preço inválido (use números)\n");
            }
        }

        if (txtEstoque.getText().trim().isEmpty()) {
            erros.append("• Digite a quantidade em estoque\n");
        } else {
            try {
                int quantidade = Integer.parseInt(txtEstoque.getText().trim());
                if (quantidade < 0) {
                    erros.append("• A quantidade não pode ser negativa\n");
                }
            } catch (NumberFormatException e) {
                erros.append("• Quantidade inválida (use números inteiros)\n");
            }
        }

        if (erros.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validação");
            alert.setHeaderText("Corrija os seguintes erros:");
            alert.setContentText(erros.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    @FXML
    private void editarProduto() {
        try {
            System.out.println("Iniciando edição de produto...");

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

            String novoNome = txtNomeEditar.getText().trim();
            String precoText = txtPrecoEditar.getText().replace(",", ".");
            Double novoPreco = Double.parseDouble(precoText);

            if (!novoNome.equals(produtoParaEdicao.getNome())) {
                Optional<Produto> produtoComMesmoNome = produtoService.buscarPorNomeExatoIncluindoDesativados(novoNome);

                if (produtoComMesmoNome.isPresent()) {
                    Produto outroProduto = produtoComMesmoNome.get();

                    if (outroProduto.getAtivo() != null && outroProduto.getAtivo()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Nome Já Existe");
                        alert.setHeaderText("Não é possível usar este nome");
                        alert.setContentText("Já existe um produto ATIVO com o nome '" + novoNome + "'\n" +
                                "ID: " + outroProduto.getId() + "\n\n" +
                                "Use um nome diferente.");
                        alert.showAndWait();
                        return;
                    } else {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Produto Desativado Encontrado");
                        confirm.setHeaderText("Existe um produto DESATIVADO com este nome");
                        confirm.setContentText("Deseja reativar o produto desativado?\n" +
                                "Ou use um nome diferente para criar um novo produto.");

                        confirm.getButtonTypes().setAll(
                                new javafx.scene.control.ButtonType("Reativar", javafx.scene.control.ButtonBar.ButtonData.YES),
                                new javafx.scene.control.ButtonType("Usar Outro Nome", javafx.scene.control.ButtonBar.ButtonData.NO)
                        );

                        Optional<javafx.scene.control.ButtonType> result = confirm.showAndWait();
                        if (result.isPresent() && result.get().getText().equals("Reativar")) {
                            Produto novoProduto = new Produto();
                            novoProduto.setNome(novoNome);
                            novoProduto.setPreco(novoPreco);
                            novoProduto.setQuantidade(produtoParaEdicao.getQuantidade());

                            Produto reativado = produtoService.salvar(novoProduto);

                            Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                            sucesso.setTitle("Produto Reativado");
                            sucesso.setHeaderText(null);
                            sucesso.setContentText("Produto '" + novoNome + "' reativado com sucesso!\n" +
                                    "O produto anterior mantém seu nome original.");
                            sucesso.showAndWait();

                            fecharJanelaEdicao();
                            return;
                        } else {
                            return;
                        }
                    }
                }
            }

            produtoParaEdicao.setNome(novoNome);
            produtoParaEdicao.setPreco(novoPreco);

            Produto produtoAtualizado = produtoService.salvar(produtoParaEdicao);
            System.out.println("Produto atualizado com ID: " + produtoAtualizado.getId());

            mostrarAlerta("Sucesso", "Produto '" + novoNome + "' atualizado com sucesso!");

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
            System.out.println("Carregando dados para edição: " + produtoParaEdicao.getNome());
        }
    }

    public void setOnEdicaoConcluidaCallback(Runnable callback) {
        this.onEdicaoConcluidaCallback = callback;
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

    private void fecharJanelaEdicao() {
        if (onEdicaoConcluidaCallback != null) {
            onEdicaoConcluidaCallback.run();
        }

        if (txtNomeEditar != null && txtNomeEditar.getScene() != null) {
            Stage stage = (Stage) txtNomeEditar.getScene().getWindow();
            stage.close();
        }
    }
}