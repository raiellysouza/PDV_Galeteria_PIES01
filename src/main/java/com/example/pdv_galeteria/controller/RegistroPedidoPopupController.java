package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.*;
import com.example.pdv_galeteria.service.CaixaService;
import com.example.pdv_galeteria.service.PedidoService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

@Component
public class RegistroPedidoPopupController implements Initializable {

    @FXML
    private Pane rootPane;

    @Autowired
    private CaixaService caixaService;

    @Autowired
    private PedidoService pedidoService;

    @FXML
    private TextArea campoNomeCliente;

    @FXML
    private TextArea campoEndereco;

    @FXML
    private TextArea campoTelefone;

    @FXML
    private TextArea campoPontoReferencia;

    @FXML
    private TextArea campoObservacoes;

    @FXML
    private TextArea campoValorPago;

    @FXML
    private TextArea campoTroco;

    @FXML
    private CheckBox checkLoja;

    @FXML
    private CheckBox checkSite;

    @FXML
    private CheckBox checkIfood;

    @FXML
    private CheckBox checkOutro;

    @FXML
    private CheckBox checkRetirada;

    @FXML
    private CheckBox checkEntrega;

    @FXML
    private ToggleButton btnPix;

    @FXML
    private ToggleButton btnDinheiro;

    @FXML
    private ToggleButton btnDebito;

    @FXML
    private ToggleButton btnCredito;

    @FXML
    private TreeView<String> treeItensPedido;

    @FXML
    private Label labelTotal;

    @FXML
    private Button btnRegistrarPedido;

    private Map<Produto, Integer> carrinho;
    private double totalPedido;
    private DecimalFormat df = new DecimalFormat("#,##0.00");

    private Stage popupStage;
    private ToggleGroup grupoFormaPagamento = new ToggleGroup();

    private static Long contadorVendas = 1L;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== RegistroPedidoPopupController.initialize() ===");

        if (btnPix != null) {
            btnPix.setToggleGroup(grupoFormaPagamento);
        }
        if (btnDinheiro != null) {
            btnDinheiro.setToggleGroup(grupoFormaPagamento);
        }
        if (btnDebito != null) {
            btnDebito.setToggleGroup(grupoFormaPagamento);
        }
        if (btnCredito != null) {
            btnCredito.setToggleGroup(grupoFormaPagamento);
        }

        if (campoValorPago != null) {
            campoValorPago.textProperty().addListener((observable, oldValue, newValue) -> {
                calcularTroco();
            });
        }

        if (btnRegistrarPedido != null) {
            btnRegistrarPedido.setOnAction(e -> handleRegistrarVenda());
        }

        configurarCheckboxes();

        System.out.println("Controller inicializado");
    }

    private void configurarCheckboxes() {
        List<CheckBox> checkboxesCanal = Arrays.asList(checkLoja, checkSite, checkIfood, checkOutro);
        for (CheckBox checkBox : checkboxesCanal) {
            if (checkBox != null) {
                checkBox.setOnAction(e -> {
                    if (checkBox.isSelected()) {
                        for (CheckBox cb : checkboxesCanal) {
                            if (cb != null && cb != checkBox) {
                                cb.setSelected(false);
                            }
                        }
                    }
                });
            }
        }

        if (checkRetirada != null && checkEntrega != null) {
            checkRetirada.setOnAction(e -> {
                if (checkRetirada.isSelected()) {
                    checkEntrega.setSelected(false);
                }
            });

            checkEntrega.setOnAction(e -> {
                if (checkEntrega.isSelected()) {
                    checkRetirada.setSelected(false);
                }
            });
        }
    }

    private void calcularTroco() {
        if (campoValorPago == null || campoTroco == null) return;

        try {
            String valorPagoStr = campoValorPago.getText()
                    .replace("R$", "")
                    .replace(" ", "")
                    .replace(".", "")
                    .replace(",", ".")
                    .trim();

            if (!valorPagoStr.isEmpty()) {
                double valorPago = Double.parseDouble(valorPagoStr);
                double troco = valorPago - totalPedido;

                if (troco > 0) {
                    campoTroco.setText("Troco: R$ " + df.format(troco));
                } else {
                    campoTroco.setText("Troco: R$ 0,00");
                }
            } else {
                campoTroco.setText("Troco: R$ 0,00");
            }
        } catch (NumberFormatException e) {
            campoTroco.setText("Troco: R$ 0,00");
        }
    }

    private boolean validarCampos() {
        System.out.println("\n=== VALIDANDO CAMPOS ===");

        if (campoNomeCliente == null || campoNomeCliente.getText() == null ||
                campoNomeCliente.getText().trim().isEmpty()) {
            mostrarErro("Nome do cliente é obrigatório");
            return false;
        }

        if (grupoFormaPagamento.getSelectedToggle() == null) {
            mostrarErro("Selecione uma forma de pagamento");
            return false;
        }

        String formaPagamento = getFormaPagamentoSelecionada();
        System.out.println("Forma de pagamento: " + formaPagamento);

        if (formaPagamento.equals("Dinheiro")) {
            if (campoValorPago == null || campoValorPago.getText() == null ||
                    campoValorPago.getText().trim().isEmpty()) {
                mostrarErro("Valor pago é obrigatório para pagamento em dinheiro");
                return false;
            }

            try {
                String valorLimpo = campoValorPago.getText()
                        .replace("R$", "")
                        .replace(" ", "")
                        .replace(".", "")
                        .replace(",", ".")
                        .trim();

                if (valorLimpo.isEmpty()) {
                    mostrarErro("Valor pago é obrigatório");
                    return false;
                }

                double valorPago = Double.parseDouble(valorLimpo);
                if (valorPago < totalPedido) {
                    mostrarErro("Valor pago (R$ " + df.format(valorPago) +
                            ") é menor que o total do pedido (R$ " + df.format(totalPedido) + ")");
                    return false;
                }
            } catch (NumberFormatException e) {
                mostrarErro("Valor pago inválido. Use números (ex: 50,00 ou 50.00)");
                return false;
            }
        }

        if (checkRetirada == null || checkEntrega == null) {
            mostrarErro("Erro interno: campos de entrega não encontrados");
            return false;
        }

        if (!checkEntrega.isSelected() && !checkRetirada.isSelected()) {
            mostrarErro("Selecione o tipo de entrega");
            return false;
        }

        if (checkEntrega.isSelected()) {
            if (campoEndereco == null || campoEndereco.getText() == null ||
                    campoEndereco.getText().trim().isEmpty()) {
                mostrarErro("Endereço é obrigatório para entrega");
                return false;
            }

            if (campoTelefone == null || campoTelefone.getText() == null ||
                    campoTelefone.getText().trim().isEmpty()) {
                mostrarErro("Telefone é obrigatório para entrega");
                return false;
            }

            String telefoneLimpo = campoTelefone.getText().replaceAll("[^0-9]", "");
            if (telefoneLimpo.length() < 10 || telefoneLimpo.length() > 11) {
                mostrarErro("Telefone inválido. Deve ter 10 ou 11 dígitos");
                return false;
            }
        }

        if (carrinho == null || carrinho.isEmpty()) {
            mostrarErro("Não há itens no pedido. Adicione produtos antes de registrar.");
            return false;
        }

        if (totalPedido <= 0) {
            mostrarErro("Total do pedido inválido");
            return false;
        }

        System.out.println("=== VALIDAÇÃO CONCLUÍDA COM SUCESSO ===");
        return true;
    }

    private String getFormaPagamentoSelecionada() {
        if (grupoFormaPagamento.getSelectedToggle() != null) {
            ToggleButton selected = (ToggleButton) grupoFormaPagamento.getSelectedToggle();
            return selected.getText();
        }
        return "Não informado";
    }

    private String getCanalVendaSelecionado() {
        if (checkLoja != null && checkLoja.isSelected()) return "Loja";
        if (checkSite != null && checkSite.isSelected()) return "Site";
        if (checkIfood != null && checkIfood.isSelected()) return "Ifood";
        if (checkOutro != null && checkOutro.isSelected()) return "Outro";
        return "Não informado";
    }

    private String getTipoEntregaSelecionado() {
        if (checkRetirada != null && checkRetirada.isSelected()) return "Retirada na Loja";
        if (checkEntrega != null && checkEntrega.isSelected()) return "Entrega";
        return "Não informado";
    }

    private void mostrarErro(String mensagem) {
        Alert erro = new Alert(Alert.AlertType.ERROR);
        erro.setTitle("Erro de Validação");
        erro.setHeaderText(null);
        erro.setContentText(mensagem);
        erro.showAndWait();
    }

    private void mostrarSucesso(String mensagem) {
        Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
        sucesso.setTitle("Venda Registrada");
        sucesso.setHeaderText(null);
        sucesso.setContentText(mensagem);
        sucesso.showAndWait();
    }

    public void setPopupStage(Stage stage) {
        this.popupStage = stage;
    }

    public void setCaixaService(CaixaService caixaService) {
        this.caixaService = caixaService;
        System.out.println("CaixaService injetado no pop-up controller: " + (caixaService != null));
    }

    @FXML
    private void handleRegistrarVenda() {
        try {
            System.out.println("=== REGISTRANDO VENDA NO POP-UP ===");

            if (!validarCampos()) {
                System.out.println("Validação falhou");
                return;
            }

            if (caixaService == null) {
                System.err.println("ERRO: caixaService é nulo!");
                mostrarErro("Serviço de caixa não disponível");
                return;
            }

            if (pedidoService == null) {
                System.err.println("ERRO: pedidoService é nulo!");
                mostrarErro("Serviço de pedidos não disponível");
                return;
            }

            Optional<Caixa> caixaOpt = caixaService.getCaixaAbertoDoDia();
            if (!caixaOpt.isPresent()) {
                System.err.println("ERRO: Nenhum caixa aberto encontrado");
                mostrarErro("Não há caixa aberto! Abra o caixa antes de registrar vendas.");
                return;
            }

            Caixa caixa = caixaOpt.get();
            System.out.println("Caixa encontrado - ID: " + caixa.getId());

            String nomeCliente = campoNomeCliente.getText().trim();
            String formaPagamento = getFormaPagamentoSelecionada();
            String canalVenda = getCanalVendaSelecionado();
            String tipoEntrega = getTipoEntregaSelecionado();
            String valorPagoStr = campoValorPago.getText();
            String enderecoEntrega = campoEndereco != null ? campoEndereco.getText().trim() : "";
            String telefone = campoTelefone != null ? campoTelefone.getText().trim() : "";
            String observacoes = campoObservacoes != null ? campoObservacoes.getText().trim() : "";
            String pontoReferencia = campoPontoReferencia != null ? campoPontoReferencia.getText().trim() : "";

            BigDecimal valorTroco = BigDecimal.ZERO;
            if (formaPagamento.equals("Dinheiro") && valorPagoStr != null && !valorPagoStr.trim().isEmpty()) {
                try {
                    String valorLimpo = valorPagoStr
                            .replace("R$", "")
                            .replace(" ", "")
                            .replace(".", "")
                            .replace(",", ".")
                            .trim();

                    if (!valorLimpo.isEmpty()) {
                        double valorPago = Double.parseDouble(valorLimpo);
                        if (valorPago > totalPedido) {
                            double troco = valorPago - totalPedido;
                            valorTroco = BigDecimal.valueOf(troco);
                            System.out.println("Troco calculado: R$ " + troco);
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Erro ao calcular troco: " + e.getMessage());
                }
            }

            Long numeroVenda = contadorVendas++;
            System.out.println("Número da venda: " + numeroVenda);

            String descricaoVenda = "Venda #" + String.format("%03d", numeroVenda);
            if (!canalVenda.equals("Não informado")) {
                descricaoVenda += " - " + canalVenda;
            }

            String referenciaVenda = "VENDA_" + numeroVenda;
            BigDecimal valorVenda = BigDecimal.valueOf(totalPedido);

            System.out.println("Registrando entrada no caixa: R$ " + valorVenda);
            System.out.println("Descrição: " + descricaoVenda);

            System.out.println("=== SALVANDO PEDIDO NO BANCO ===");

            Pedido pedido = new Pedido(nomeCliente);
            pedido.setFormaPagamento(formaPagamento);
            pedido.setTipoEntrega(tipoEntrega);
            pedido.setStatus(StatusPedido.REGISTRADO);

            if (checkEntrega != null && checkEntrega.isSelected()) {
                String enderecoCompleto = enderecoEntrega;
                if (!pontoReferencia.isEmpty()) {
                    enderecoCompleto += " - " + pontoReferencia;
                }
                pedido.setTipoEntrega("Entrega - " + enderecoCompleto);

                if (!observacoes.isEmpty()) {
                }
            }

            System.out.println("Adicionando itens ao pedido...");
            for (Map.Entry<Produto, Integer> entry : carrinho.entrySet()) {
                Produto produto = entry.getKey();
                int quantidade = entry.getValue();

                System.out.println("  - " + quantidade + "x " + produto.getNome() +
                        " (R$ " + produto.getPreco() + " cada)");

                ItemPedido item = new ItemPedido(
                        produto.getNome(),
                        quantidade,
                        produto.getPreco()
                );
                item.setPedido(pedido);
                pedido.addItem(item);
            }

            pedido.recalcularTotal();

            System.out.println("Total do pedido: R$ " + pedido.getTotal());
            System.out.println("Status: " + pedido.getStatus());
            System.out.println("Cliente: " + pedido.getCliente());

            Pedido pedidoSalvo = pedidoService.criarPedido(pedido);

            System.out.println("SUCESSO: Pedido salvo no banco com ID: " + pedidoSalvo.getId());
            System.out.println("=== PEDIDO SALVO NO BANCO ===");

            MovimentoCaixa movimentoVenda = caixaService.registrarEntrada(
                    valorVenda,
                    descricaoVenda,
                    referenciaVenda
            );

            if (movimentoVenda == null) {
                System.err.println("ERRO: Falha ao registrar venda no caixa");
                mostrarErro("Falha ao registrar venda. Tente novamente.");
                return;
            }

            System.out.println("SUCESSO: Venda registrada no caixa com ID: " + movimentoVenda.getId());

            MovimentoCaixa movimentoTroco = null;
            if (valorTroco.compareTo(BigDecimal.ZERO) > 0) {
                System.out.println("Registrando saída (troco) no caixa: R$ " + valorTroco);

                String descricaoTroco = "Troco - Venda #" + String.format("%03d", numeroVenda);
                String referenciaTroco = "TROCO_" + numeroVenda;

                movimentoTroco = caixaService.registrarSaida(
                        valorTroco,
                        descricaoTroco,
                        referenciaTroco
                );

                if (movimentoTroco != null) {
                    System.out.println("SUCESSO: Troco registrado com ID: " + movimentoTroco.getId());
                } else {
                    System.err.println("AVISO: Troco não foi registrado!");
                }
            }

            StringBuilder mensagemSucesso = new StringBuilder();
            mensagemSucesso.append("Venda registrada com sucesso!\n\n");
            mensagemSucesso.append("Cliente: ").append(nomeCliente.isEmpty() ? "Não informado" : nomeCliente).append("\n");
            mensagemSucesso.append("Total: R$ ").append(String.format("%.2f", totalPedido)).append("\n");
            mensagemSucesso.append("Forma de Pagamento: ").append(formaPagamento).append("\n");
            mensagemSucesso.append("Canal: ").append(canalVenda).append("\n");
            mensagemSucesso.append("Tipo de Entrega: ").append(tipoEntrega).append("\n");
            mensagemSucesso.append("Número do Pedido: #").append(pedidoSalvo.getId()).append("\n");

            if (valorTroco.compareTo(BigDecimal.ZERO) > 0) {
                mensagemSucesso.append("Troco: R$ ").append(String.format("%.2f", valorTroco)).append("\n");
            }

            mensagemSucesso.append("\nNúmero da Venda: #").append(String.format("%03d", numeroVenda));

            mostrarSucesso(mensagemSucesso.toString());

            if (popupStage != null) {
                popupStage.close();
            }

        } catch (Exception e) {
            System.err.println("ERRO ao registrar venda: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao registrar venda: " + e.getMessage());
        }
    }

    public void setDadosPedido(Map<Produto, Integer> carrinho, double totalPedido) {
        System.out.println("=== SET DADOS DO PEDIDO NO POP-UP ===");

        this.carrinho = carrinho;
        this.totalPedido = totalPedido;

        System.out.println("Total do pedido: R$ " + totalPedido);
        System.out.println("Itens no carrinho: " + (carrinho != null ? carrinho.size() : 0));

        if (labelTotal != null) {
            labelTotal.setText("R$ " + formatarMoeda(totalPedido));
        }

        if (treeItensPedido != null && carrinho != null && !carrinho.isEmpty()) {
            treeItensPedido.setRoot(null);
            TreeItem<String> rootItem = new TreeItem<>("Itens do Pedido");

            for (Map.Entry<Produto, Integer> entry : carrinho.entrySet()) {
                Produto p = entry.getKey();
                int quantidade = entry.getValue();
                double subtotal = p.getPreco() * quantidade;

                String nomeProduto = p.getNome();
                if (nomeProduto.length() > 30) {
                    nomeProduto = nomeProduto.substring(0, 27) + "...";
                }

                String itemText = String.format("%s x%d = R$ %.2f",
                        nomeProduto,
                        quantidade,
                        subtotal);

                TreeItem<String> item = new TreeItem<>(itemText);
                rootItem.getChildren().add(item);
            }

            treeItensPedido.setRoot(rootItem);
            treeItensPedido.setShowRoot(true);
        }

        System.out.println("=== DADOS DO PEDIDO SETADOS ===");
    }

    private String formatarMoeda(double valor) {
        try {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "BR"));
            symbols.setDecimalSeparator(',');
            symbols.setGroupingSeparator('.');

            DecimalFormat dfMoeda = new DecimalFormat("#,##0.00", symbols);
            return dfMoeda.format(valor);
        } catch (Exception e) {
            return "0,00";
        }
    }
}