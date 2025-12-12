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
    }

    private void ajustarCampos(String canal) {
        boolean modoLote = canal.equals("site") || canal.equals("ifood") || canal.equals("outro");

        txtNomeCliente.setDisable(modoLote);
        txtEndereco.setDisable(modoLote);
        cbTipoEntrega.setDisable(modoLote);
        spnPrevisao.setDisable(modoLote);
    }

    private double calcularTotal() {
        return itens.stream()
                .mapToDouble(i -> i.getPrecoUnitario() * i.getQuantidade())
                .sum();
    }

    @FXML
    private void confirmarPedido() {
        String canal = cbCanalVenda.getValue();

        double total = calcularTotal();
        lblTotal.setText(String.valueOf(total));

        boolean vendaLote = canal.equals("site") || canal.equals("ifood") || canal.equals("outro");

        if (vendaLote) {
            vendaEmLoteController.criarVendaEmLote(canal, List.copyOf(itens));
            fecharJanela();
            return;
        }

        // aqui adiciona o metodo para criar pedido normal

        fecharJanela();
    }

    @FXML
    private void cancelar() {
        fecharJanela();
    }

    private void fecharJanela() {
        Stage stage = (Stage) txtNomeCliente.getScene().getWindow();
        stage.close();
    }

    public void adicionarItem(String produto, int quantidade, double precoUnitario) {
        ItemPedido novo = new ItemPedido(produto, quantidade, precoUnitario);
        itens.add(novo);
        lblTotal.setText(String.valueOf(calcularTotal()));
    }
}
