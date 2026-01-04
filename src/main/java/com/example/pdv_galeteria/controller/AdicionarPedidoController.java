package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.ItemPedido;
import com.example.pdv_galeteria.service.PedidoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class AdicionarPedidoController {

    @FXML
    private ComboBox<String> cbCanalVenda;
    @FXML
    private TextField txtNomeCliente;
    @FXML
    private TextField txtEndereco;
    @FXML
    private ComboBox<String> cbTipoEntrega;
    @FXML
    private ComboBox<String> cbPagamento;
    @FXML
    private Spinner<Integer> spnPrevisao;
    @FXML
    private TableView<ItemPedido> tabelaItens;
    @FXML
    private Label lblTotal;
    @FXML
    private Button btnCancelar;
    @FXML
    private TextField txtPrevisaoEntrega;
    @FXML
    private Label labelTrocoGeral;

    private final ObservableList<ItemPedido> itens = FXCollections.observableArrayList();

    private VendaEmLoteController vendaEmLoteController;

    public void setDependencies(PedidoService pedidoService, VendaEmLoteController vendaEmLoteController) {
        this.vendaEmLoteController = vendaEmLoteController;
    }

    @FXML
    public void initialize() {
        cbCanalVenda.getItems().addAll("local", "site", "ifood", "outro");
        cbTipoEntrega.getItems().addAll("retirada", "entrega");
        cbPagamento.getItems().addAll("dinheiro", "pix", "cartao");

        tabelaItens.setItems(itens);

        cbCanalVenda.valueProperty().addListener((obs, oldV, newV) -> ajustarCampos(newV));

        configurarCampoPrevisao();
    }

    private void configurarCampoPrevisao() {
        txtPrevisaoEntrega.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtPrevisaoEntrega.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void ajustarCampos(String canal) {
        boolean modoLote = canal.equals("site") || canal.equals("ifood") || canal.equals("outro");

        txtNomeCliente.setDisable(modoLote);
        txtEndereco.setDisable(modoLote);
        cbTipoEntrega.setDisable(modoLote);
        txtPrevisaoEntrega.setDisable(modoLote);
    }

    private double calcularTotal() {
        return itens.stream()
                .mapToDouble(i -> i.getPrecoUnitario() * i.getQuantidade())
                .sum();
    }

    @FXML
    private void confirmarPedido() {
        String previsaoText = txtPrevisaoEntrega.getText().trim();
        int previsaoEntrega = 0;

        try {
            if (!previsaoText.isEmpty()) {
                previsaoEntrega = Integer.parseInt(previsaoText);
                if (previsaoEntrega < 0) {
                    mostrarAlerta("Erro", "O tempo de previsão não pode ser negativo");
                    return;
                }
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "Digite um número válido para o tempo de previsão");
            return;
        }

        if (cbCanalVenda.getValue() == null) {
            mostrarAlerta("Erro", "Selecione o canal da venda");
            return;
        }

        if (txtNomeCliente.getText().trim().isEmpty()) {
            mostrarAlerta("Erro", "Informe o nome do cliente");
            return;
        }

        if (itens.isEmpty()) {
            mostrarAlerta("Erro", "Adicione pelo menos um item ao pedido");
            return;
        }

        String canal = cbCanalVenda.getValue();
        double total = calcularTotal();
        lblTotal.setText(String.format("R$ %.2f", total));

        boolean vendaLote = canal.equals("site") || canal.equals("ifood") || canal.equals("outro");

        if (vendaLote) {
            vendaEmLoteController.criarVendaEmLote(canal, List.copyOf(itens));
            fecharJanela();
            return;
        }

        System.out.println("Pedido registrado com sucesso!");
        System.out.println("Previsão de entrega: " + previsaoEntrega + " minutos");

        fecharJanela();
    }

    private void fecharJanela() {
        Stage stage = (Stage) txtNomeCliente.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    public void adicionarItem(String produto, int quantidade, double precoUnitario) {
        ItemPedido novo = new ItemPedido(produto, quantidade, precoUnitario);
        itens.add(novo);
        lblTotal.setText(String.format("R$ %.2f", calcularTotal()));
    }

    public void setPrevisaoEntrega(int minutos) {
        txtPrevisaoEntrega.setText(String.valueOf(minutos));
    }

    public int getPrevisaoEntrega() {
        try {
            return Integer.parseInt(txtPrevisaoEntrega.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
