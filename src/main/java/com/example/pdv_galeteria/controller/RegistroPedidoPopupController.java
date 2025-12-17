package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.*;
import com.example.pdv_galeteria.service.CaixaService;
import com.example.pdv_galeteria.service.PedidoService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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

    @FXML private Pane rootPane;
    @Autowired private CaixaService caixaService;
    @Autowired private PedidoService pedidoService;
    @Autowired private TelaRegistroPedidoController telaRegistroController;


    @FXML private Label labelNomeCliente;
    @FXML private Label labelTipoEntrega;
    @FXML private Label labelTaxaEntrega;
    @FXML private Label labelEndereco;
    @FXML private Label labelNumero;
    @FXML private Label labelTelefone;
    @FXML private Label labelPontoReferencia;
    @FXML private Label labelObservacoes;

    @FXML private TextArea campoNomeCliente;
    @FXML private TextArea campoEndereco;
    @FXML private TextField campoTelefone;
    @FXML private TextArea campoPontoReferencia;
    @FXML private TextArea campoObservacoes;
    @FXML private TextField campoTaxaEntrega;
    @FXML private TextField campoNumero;
    @FXML private TextField campoTempoPrevisao;

    @FXML private CheckBox checkLoja;
    @FXML private CheckBox checkSite;
    @FXML private CheckBox checkIfood;
    @FXML private CheckBox checkOutro;
    @FXML private CheckBox checkRetirada;
    @FXML private CheckBox checkEntrega;

    @FXML private ToggleButton btnPix;
    @FXML private ToggleButton btnDinheiro;
    @FXML private ToggleButton btnDebito;
    @FXML private ToggleButton btnCredito;

    @FXML private VBox containerEndereco;
    @FXML private VBox containerValoresPagamento;
    @FXML private VBox containerValorUnico;

    @FXML private HBox containerValorPix;
    @FXML private HBox containerValorDinheiro;
    @FXML private HBox containerValorDebito;
    @FXML private HBox containerValorCredito;

    @FXML private TextField campoValorPix;
    @FXML private TextField campoValorDinheiro;
    @FXML private TextField campoValorDebito;
    @FXML private TextField campoValorCredito;
    @FXML private TextField campoValorPagoUnico;

    @FXML private Label labelTrocoDinheiro;
    @FXML private Label labelTrocoUnico;
    @FXML private Label labelTotalPago;

    @FXML private TreeView<String> treeItensPedido;
    @FXML private Label labelTotal;
    @FXML private Button btnRegistrarPedido;
    @FXML private Button btnCancelar;

    private Map<Produto, Integer> carrinho;
    private double totalPedido;
    private DecimalFormat df = new DecimalFormat("#,##0.00");
    private Stage popupStage;
    private static Long contadorVendas = 1L;

    private List<ToggleButton> botoesFormaPagamento = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== RegistroPedidoPopupController.initialize() ===");

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "BR"));
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        df.setDecimalFormatSymbols(symbols);

        if (btnPix != null) botoesFormaPagamento.add(btnPix);
        if (btnDinheiro != null) botoesFormaPagamento.add(btnDinheiro);
        if (btnDebito != null) botoesFormaPagamento.add(btnDebito);
        if (btnCredito != null) botoesFormaPagamento.add(btnCredito);

        configurarBotoesFormaPagamento();

        configurarListenersValores();

        configurarCheckboxes();

        configurarBotoesAcao();

        inicializarEstadoPadrao();

        System.out.println("Controller inicializado");
    }

    private void configurarBotoesFormaPagamento() {
        for (ToggleButton botao : botoesFormaPagamento) {
            botao.setOnAction(e -> {
                atualizarEstiloBotoesPagamento();
                atualizarVisibilidadeCamposPagamento();
            });
        }
    }

    private void configurarBotoesAcao() {
        if (btnRegistrarPedido != null) {
            btnRegistrarPedido.setOnAction(e -> handleRegistrarVenda());
        }

        if (btnCancelar != null) {
            btnCancelar.setOnAction(e -> handleCancelar());
        }
    }

    @FXML
    private void handleCancelar() {
        if (popupStage != null) {
            popupStage.close();
        }
    }

    private void atualizarEstiloBotoesPagamento() {
        for (ToggleButton botao : botoesFormaPagamento) {
            if (botao.isSelected()) {
                botao.setStyle("-fx-background-color: #d2691e; -fx-background-radius: 8px;");
            } else {
                botao.setStyle("-fx-background-color: #f68411; -fx-background-radius: 8px;");
            }
        }
    }

    private void inicializarEstadoPadrao() {
        if (checkLoja != null) checkLoja.setSelected(false);
        if (checkSite != null) checkSite.setSelected(false);
        if (checkIfood != null) checkIfood.setSelected(false);
        if (checkOutro != null) checkOutro.setSelected(false);
        if (checkRetirada != null) checkRetirada.setSelected(false);
        if (checkEntrega != null) checkEntrega.setSelected(false);

        for (ToggleButton botao : botoesFormaPagamento) {
            botao.setSelected(false);
            botao.setStyle("-fx-background-color: #f68411; -fx-background-radius: 8px;");
        }


        if (containerEndereco != null) containerEndereco.setVisible(false);

        if (containerValoresPagamento != null) containerValoresPagamento.setVisible(false);
        if (containerValorUnico != null) containerValorUnico.setVisible(false);

        atualizarVisibilidadeCamposPagamento();
    }

    private void configurarListenersValores() {
        if (campoValorPagoUnico != null) {
            campoValorPagoUnico.textProperty().addListener((observable, oldValue, newValue) -> {
                calcularTrocoUnico();
            });
        }

        if (campoValorDinheiro != null) {
            campoValorDinheiro.textProperty().addListener((observable, oldValue, newValue) -> {
                calcularTrocoMultiplo();
                atualizarTotalPago();
            });
        }

        if (campoValorPix != null) {
            campoValorPix.textProperty().addListener((observable, oldValue, newValue) -> {
                atualizarTotalPago();
            });
        }

        if (campoValorDebito != null) {
            campoValorDebito.textProperty().addListener((observable, oldValue, newValue) -> {
                atualizarTotalPago();
            });
        }

        if (campoValorCredito != null) {
            campoValorCredito.textProperty().addListener((observable, oldValue, newValue) -> {
                atualizarTotalPago();
            });
        }

        if (campoTaxaEntrega != null) {
            campoTaxaEntrega.textProperty().addListener((observable, oldValue, newValue) -> {
                atualizarTotalComTaxa();
            });
        }
    }

    private void atualizarTotalComTaxa() {
        try {
            double taxa = parseValor(campoTaxaEntrega.getText());
            double novoTotal = (totalPedido + taxa);
            labelTotal.setText("R$ " + df.format(novoTotal));
        } catch (NumberFormatException e) {
            labelTotal.setText("R$ " + df.format(totalPedido));
        }
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
                        atualizarVisibilidadePorCanal();
                    } else {
                        atualizarVisibilidadePorCanal();
                    }
                });
            }
        }

        if (checkRetirada != null && checkEntrega != null) {
            checkRetirada.setOnAction(e -> {
                if (checkRetirada.isSelected()) {
                    checkEntrega.setSelected(false);
                }
                atualizarVisibilidadeCamposEndereco();
            });

            checkEntrega.setOnAction(e -> {
                if (checkEntrega.isSelected()) {
                    checkRetirada.setSelected(false);
                }
                atualizarVisibilidadeCamposEndereco();
            });
        }
    }

    private void atualizarVisibilidadePorCanal() {
        String canalSelecionado = getCanalVendaSelecionado();

        switch (canalSelecionado) {
            case "Loja":
                mostrarCamposLoja();
                break;
            case "Site":
            case "Ifood":
            case "Outro":
                mostrarCamposOnline();
                break;
            default:
                esconderTodosCampos();
                break;
        }
    }

    private void mostrarCamposLoja() {
        if (labelNomeCliente != null) labelNomeCliente.setVisible(true);
        if (campoNomeCliente != null) campoNomeCliente.setVisible(true);

        if (labelTipoEntrega != null) labelTipoEntrega.setVisible(true);
        if (checkRetirada != null) checkRetirada.setVisible(true);
        if (checkEntrega != null) checkEntrega.setVisible(true);

        if (labelTaxaEntrega != null) labelTaxaEntrega.setVisible(true);
        if (campoTaxaEntrega != null) campoTaxaEntrega.setVisible(true);

        atualizarVisibilidadeCamposEndereco();

        if (labelObservacoes != null) labelObservacoes.setVisible(true);
        if (campoObservacoes != null) campoObservacoes.setVisible(true);

        if (campoTempoPrevisao != null) campoTempoPrevisao.setVisible(true);

        for (ToggleButton botao : botoesFormaPagamento) {
            botao.setVisible(true);
        }
    }

    private void mostrarCamposOnline() {
        if (labelTipoEntrega != null) labelTipoEntrega.setVisible(false);
        if (checkRetirada != null) checkRetirada.setVisible(false);
        if (checkEntrega != null) checkEntrega.setVisible(false);

        if (labelTaxaEntrega != null) labelTaxaEntrega.setVisible(false);
        if (campoTaxaEntrega != null) campoTaxaEntrega.setVisible(false);

        if (containerEndereco != null) containerEndereco.setVisible(false);

        if (labelNomeCliente != null) labelNomeCliente.setVisible(true);
        if (campoNomeCliente != null) campoNomeCliente.setVisible(true);

        if (labelObservacoes != null) labelObservacoes.setVisible(true);
        if (campoObservacoes != null) campoObservacoes.setVisible(true);

        if (campoTempoPrevisao != null) campoTempoPrevisao.setVisible(false);

        for (ToggleButton botao : botoesFormaPagamento) {
            botao.setVisible(true);
        }
    }

    private void esconderTodosCampos() {
        if (labelTipoEntrega != null) labelTipoEntrega.setVisible(false);
        if (checkRetirada != null) checkRetirada.setVisible(false);
        if (checkEntrega != null) checkEntrega.setVisible(false);
        if (labelTaxaEntrega != null) labelTaxaEntrega.setVisible(false);
        if (campoTaxaEntrega != null) campoTaxaEntrega.setVisible(false);
        if (containerEndereco != null) containerEndereco.setVisible(false);

        for (ToggleButton botao : botoesFormaPagamento) {
            botao.setVisible(false);
        }
    }

    private void atualizarVisibilidadeCamposEndereco() {
        boolean mostrarEndereco = checkEntrega != null && checkEntrega.isSelected();

        if (containerEndereco != null) {
            containerEndereco.setVisible(mostrarEndereco);
        }

        boolean obrigatorio = mostrarEndereco;
        String texto = obrigatorio ? "*" : "";

        if (labelEndereco != null) labelEndereco.setText("Endereço" + texto);
        if (labelNumero != null) labelNumero.setText("Número" + texto);
        if (labelTelefone != null) labelTelefone.setText("Telefone" + texto);
        if (labelPontoReferencia != null) labelPontoReferencia.setText("Ponto de Referência" + texto);
    }

    private void atualizarVisibilidadeCamposPagamento() {
        List<String> formasSelecionadas = getFormasPagamentoSelecionadas();
        int quantidadeFormas = formasSelecionadas.size();

        if (quantidadeFormas == 0) {
            if (containerValoresPagamento != null) containerValoresPagamento.setVisible(false);
            if (containerValorUnico != null) containerValorUnico.setVisible(false);
        } else if (quantidadeFormas == 1) {
            if (containerValoresPagamento != null) containerValoresPagamento.setVisible(false);
            if (containerValorUnico != null) containerValorUnico.setVisible(true);

            String forma = formasSelecionadas.get(0);
            boolean mostrarTroco = forma.equals("Dinheiro");
            if (labelTrocoUnico != null) labelTrocoUnico.setVisible(mostrarTroco);

            limparCamposMultiplosPagamento();
        } else {
            if (containerValoresPagamento != null) containerValoresPagamento.setVisible(true);
            if (containerValorUnico != null) containerValorUnico.setVisible(false);

            if (containerValorPix != null) containerValorPix.setVisible(formasSelecionadas.contains("Pix"));
            if (containerValorDinheiro != null) containerValorDinheiro.setVisible(formasSelecionadas.contains("Dinheiro"));
            if (containerValorDebito != null) containerValorDebito.setVisible(formasSelecionadas.contains("Débito"));
            if (containerValorCredito != null) containerValorCredito.setVisible(formasSelecionadas.contains("Crédito"));

            if (campoValorPagoUnico != null) campoValorPagoUnico.clear();
        }

        atualizarTotalPago();
    }

    private void limparCamposMultiplosPagamento() {
        if (campoValorPix != null) campoValorPix.clear();
        if (campoValorDinheiro != null) campoValorDinheiro.clear();
        if (campoValorDebito != null) campoValorDebito.clear();
        if (campoValorCredito != null) campoValorCredito.clear();
        if (labelTrocoDinheiro != null) labelTrocoDinheiro.setText("R$ 0,00");
        if (labelTotalPago != null) labelTotalPago.setText("R$ 0,00");
    }

    private void calcularTrocoUnico() {
        try {
            String valorStr = campoValorPagoUnico.getText();
            double valorPago = parseValor(valorStr);
            double troco = valorPago - getTotalComTaxa();

            if (troco > 0) {
                labelTrocoUnico.setText("R$ " + df.format(troco));
            } else {
                labelTrocoUnico.setText("R$ 0,00");
            }
        } catch (NumberFormatException e) {
            labelTrocoUnico.setText("R$ 0,00");
        }
    }

    private void calcularTrocoMultiplo() {
        try {
            String valorStr = campoValorDinheiro.getText();
            double valorDinheiro = parseValor(valorStr);

            double totalOutras = 0;
            List<String> formas = getFormasPagamentoSelecionadas();

            if (formas.contains("Pix")) {
                totalOutras += parseValor(campoValorPix.getText());
            }
            if (formas.contains("Débito")) {
                totalOutras += parseValor(campoValorDebito.getText());
            }
            if (formas.contains("Crédito")) {
                totalOutras += parseValor(campoValorCredito.getText());
            }

            double totalPago = valorDinheiro + totalOutras;
            double troco = valorDinheiro - (getTotalComTaxa() - totalOutras);

            if (troco > 0) {
                labelTrocoDinheiro.setText("R$ " + df.format(troco));
            } else {
                labelTrocoDinheiro.setText("R$ 0,00");
            }
        } catch (NumberFormatException e) {
            labelTrocoDinheiro.setText("R$ 0,00");
        }
    }

    private void atualizarTotalPago() {
        try {
            double total = 0;
            List<String> formas = getFormasPagamentoSelecionadas();

            if (formas.size() == 1) {
                String valorStr = campoValorPagoUnico.getText();
                total = parseValor(valorStr);
            } else {
                for (String forma : formas) {
                    switch (forma) {
                        case "Pix":
                            total += parseValor(campoValorPix.getText());
                            break;
                        case "Dinheiro":
                            total += parseValor(campoValorDinheiro.getText());
                            break;
                        case "Débito":
                            total += parseValor(campoValorDebito.getText());
                            break;
                        case "Crédito":
                            total += parseValor(campoValorCredito.getText());
                            break;
                    }
                }
            }

            if (labelTotalPago != null) {
                labelTotalPago.setText("R$ " + df.format(total));
            }
        } catch (NumberFormatException e) {
            if (labelTotalPago != null) {
                labelTotalPago.setText("R$ 0,00");
            }
        }
    }

    private double getTotalComTaxa() {
        try {
            double taxa = parseValor(campoTaxaEntrega.getText());
            return totalPedido + taxa;
        } catch (NumberFormatException e) {
            return totalPedido;
        }
    }

    private double parseValor(String valorStr) {
        if (valorStr == null || valorStr.trim().isEmpty()) {
            return 0.0;
        }

        String valorLimpo = valorStr
                .replace("R$", "")
                .replace(" ", "")
                .replace(".", "")
                .replace(",", ".")
                .trim();

        if (valorLimpo.isEmpty()) {
            return 0.0;
        }

        return Double.parseDouble(valorLimpo);
    }

    private boolean validarCampos() {
        System.out.println("\n=== VALIDANDO CAMPOS ===");

        String canalVenda = getCanalVendaSelecionado();
        if (canalVenda.equals("Não informado")) {
            mostrarErro("Selecione um canal de venda");
            return false;
        }

        if (campoNomeCliente == null || campoNomeCliente.getText().trim().isEmpty()) {
            mostrarErro("Nome do cliente é obrigatório");
            return false;
        }

        List<String> formasPagamento = getFormasPagamentoSelecionadas();
        if (formasPagamento.isEmpty()) {
            mostrarErro("Selecione pelo menos uma forma de pagamento");
            return false;
        }

        if (!validarValoresPagamento(formasPagamento)) {
            return false;
        }

        if (canalVenda.equals("Loja")) {
            if (!validarCamposLoja()) {
                return false;
            }
        } else {
            if (!validarCamposOnline()) {
                return false;
            }
        }

        if (carrinho == null || carrinho.isEmpty()) {
            mostrarErro("Não há itens no pedido");
            return false;
        }

        if (totalPedido <= 0) {
            mostrarErro("Total do pedido inválido");
            return false;
        }

        System.out.println("=== VALIDAÇÃO CONCLUÍDA COM SUCESSO ===");
        return true;
    }

    private boolean validarCamposLoja() {
        if (!checkRetirada.isSelected() && !checkEntrega.isSelected()) {
            mostrarErro("Selecione o tipo de entrega");
            return false;
        }

        if (checkEntrega.isSelected()) {
            if (campoEndereco.getText().trim().isEmpty()) {
                mostrarErro("Endereço é obrigatório para entrega");
                return false;
            }

            if (campoNumero.getText().trim().isEmpty()) {
                mostrarErro("Número é obrigatório para entrega");
                return false;
            }

            String telefoneLimpo = campoTelefone.getText().replaceAll("[^0-9]", "");
            if (telefoneLimpo.length() < 10 || telefoneLimpo.length() > 11) {
                mostrarErro("Telefone inválido. Deve ter 10 ou 11 dígitos");
                return false;
            }
        }

        if (campoTempoPrevisao.getText().trim().isEmpty()) {
            mostrarErro("Tempo de previsão é obrigatório");
            return false;
        }

        return true;
    }

    private boolean validarCamposOnline() {
        return true;
    }

    private boolean validarValoresPagamento(List<String> formasPagamento) {
        double totalComTaxa = getTotalComTaxa();
        double totalPago = 0;

        if (formasPagamento.size() == 1) {
            String forma = formasPagamento.get(0);
            try {
                String valorStr = campoValorPagoUnico.getText();
                double valorPago = parseValor(valorStr);

                if (valorPago <= 0) {
                    mostrarErro("Valor pago deve ser maior que zero");
                    return false;
                }

                if (forma.equals("Dinheiro") && valorPago < totalComTaxa) {
                    mostrarErro("Valor pago em dinheiro (R$ " + df.format(valorPago) +
                            ") é menor que o total (R$ " + df.format(totalComTaxa) + ")");
                    return false;
                }

                totalPago = valorPago;
            } catch (NumberFormatException e) {
                mostrarErro("Valor pago inválido");
                return false;
            }
        } else {
            for (String forma : formasPagamento) {
                try {
                    double valorForma = 0;

                    switch (forma) {
                        case "Pix":
                            valorForma = parseValor(campoValorPix.getText());
                            break;
                        case "Dinheiro":
                            valorForma = parseValor(campoValorDinheiro.getText());
                            break;
                        case "Débito":
                            valorForma = parseValor(campoValorDebito.getText());
                            break;
                        case "Crédito":
                            valorForma = parseValor(campoValorCredito.getText());
                            break;
                    }

                    if (valorForma < 0) {
                        mostrarErro("Valor para " + forma + " não pode ser negativo");
                        return false;
                    }

                    totalPago += valorForma;
                } catch (NumberFormatException e) {
                    mostrarErro("Valor inválido para " + forma);
                    return false;
                }
            }

            if (totalPago < totalComTaxa) {
                mostrarErro("Total pago (R$ " + df.format(totalPago) +
                        ") é menor que o total do pedido (R$ " + df.format(totalComTaxa) + ")");
                return false;
            }
        }

        return true;
    }

    private List<String> getFormasPagamentoSelecionadas() {
        List<String> formas = new ArrayList<>();

        if (btnPix != null && btnPix.isSelected()) formas.add("Pix");
        if (btnDinheiro != null && btnDinheiro.isSelected()) formas.add("Dinheiro");
        if (btnDebito != null && btnDebito.isSelected()) formas.add("Débito");
        if (btnCredito != null && btnCredito.isSelected()) formas.add("Crédito");

        return formas;
    }

    private Map<String, Double> getValoresPorFormaPagamento() {
        Map<String, Double> valores = new HashMap<>();
        List<String> formas = getFormasPagamentoSelecionadas();

        if (formas.size() == 1) {
            String forma = formas.get(0);
            double valor = parseValor(campoValorPagoUnico.getText());
            valores.put(forma, valor);
        } else {
            for (String forma : formas) {
                double valor = 0;

                switch (forma) {
                    case "Pix":
                        valor = parseValor(campoValorPix.getText());
                        break;
                    case "Dinheiro":
                        valor = parseValor(campoValorDinheiro.getText());
                        break;
                    case "Débito":
                        valor = parseValor(campoValorDebito.getText());
                        break;
                    case "Crédito":
                        valor = parseValor(campoValorCredito.getText());
                        break;
                }

                valores.put(forma, valor);
            }
        }

        return valores;
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
            String formaPagamento = getFormaPagamentoConcatenada();
            String canalVenda = getCanalVendaSelecionado();
            String tipoEntrega = getTipoEntregaSelecionado();
            Map<String, Double> valoresPagamento = getValoresPorFormaPagamento();

            double taxaEntrega = parseValor(campoTaxaEntrega.getText());
            double totalComTaxa = getTotalComTaxa();

            String enderecoEntrega = campoEndereco != null ? campoEndereco.getText().trim() : "";
            String numeroEndereco = campoNumero != null ? campoNumero.getText().trim() : "";
            String telefone = campoTelefone != null ? campoTelefone.getText().trim() : "";
            String observacoes = campoObservacoes != null ? campoObservacoes.getText().trim() : "";
            String pontoReferencia = campoPontoReferencia != null ? campoPontoReferencia.getText().trim() : "";
            String tempoPrevisao = campoTempoPrevisao != null ? campoTempoPrevisao.getText().trim() : "";

            double totalPago = 0;
            for (Double valor : valoresPagamento.values()) {
                totalPago += valor;
            }

            double trocoTotal = 0;
            if (totalPago > totalComTaxa) {
                trocoTotal = totalPago - totalComTaxa;
            }

            Long numeroVenda = contadorVendas++;
            System.out.println("Número da venda: " + numeroVenda);

            String descricaoVenda = "Venda #" + String.format("%03d", numeroVenda);
            if (!canalVenda.equals("Não informado")) {
                descricaoVenda += " - " + canalVenda;
            }

            String referenciaVenda = "VENDA_" + numeroVenda;
            BigDecimal valorVenda = BigDecimal.valueOf(totalComTaxa);

            System.out.println("Registrando entrada no caixa: R$ " + valorVenda);
            System.out.println("Descrição: " + descricaoVenda);

            System.out.println("=== SALVANDO PEDIDO NO BANCO ===");

            Pedido pedido = new Pedido(nomeCliente);
            pedido.setFormaPagamento(formaPagamento);
            pedido.setTipoEntrega(tipoEntrega);
            pedido.setStatus(StatusPedido.REGISTRADO);
            pedido.setTelefone(telefone);
            pedido.setEndereco(enderecoEntrega);
            pedido.setPontoReferencia(pontoReferencia);
            pedido.setObservacoes(observacoes);
            pedido.setValorPago(totalPago);
            pedido.setTroco(trocoTotal);
            pedido.setTempoEstimado(tempoPrevisao);

            if (checkEntrega != null && checkEntrega.isSelected()) {
                pedido.setTaxaEntrega(taxaEntrega);
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

            if (trocoTotal > 0) {
                System.out.println("Registrando saída (troco) no caixa: R$ " + trocoTotal);

                String descricaoTroco = "Troco - Venda #" + String.format("%03d", numeroVenda);
                String referenciaTroco = "TROCO_" + numeroVenda;

                MovimentoCaixa movimentoTroco = caixaService.registrarSaida(
                        BigDecimal.valueOf(trocoTotal),
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
            mensagemSucesso.append("Total: R$ ").append(String.format("%.2f", totalComTaxa)).append("\n");
            mensagemSucesso.append("Forma de Pagamento: ").append(formaPagamento).append("\n");
            mensagemSucesso.append("Canal: ").append(canalVenda).append("\n");
            mensagemSucesso.append("Tipo de Entrega: ").append(tipoEntrega).append("\n");
            mensagemSucesso.append("Número do Pedido: #").append(pedidoSalvo.getId()).append("\n");

            if (taxaEntrega > 0) {
                mensagemSucesso.append("Taxa de Entrega: R$ ").append(String.format("%.2f", taxaEntrega)).append("\n");
            }

            mensagemSucesso.append("Número da Venda: #").append(String.format("%03d", numeroVenda));

            if (trocoTotal > 0) {
                mensagemSucesso.append("\nTroco: R$ ").append(String.format("%.2f", trocoTotal));
            }

            mostrarSucesso(mensagemSucesso.toString());

            limparCarrinhoEInterface();

            if (popupStage != null) {
                popupStage.close();
            }

            if (btnRegistrarPedido.getScene() != null) {
                Node node = btnRegistrarPedido.getScene().getRoot();
                if (node.getUserData() != null && node.getUserData() instanceof CaixaController) {
                    CaixaController caixaController = (CaixaController) node.getUserData();
                    caixaController.atualizarInterface();
                }
            }

        } catch (Exception e) {
            System.err.println("ERRO ao registrar venda: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao registrar venda: " + e.getMessage());
        }
    }


    private String getFormaPagamentoConcatenada() {
        List<String> formas = getFormasPagamentoSelecionadas();
        if (formas.isEmpty()) {
            return "Não informado";
        }
        return String.join(" + ", formas);
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
            return df.format(valor);
        } catch (Exception e) {
            return "0,00";
        }
    }

    public void setPopupStage(Stage stage) {
        this.popupStage = stage;
    }

    public void setCaixaService(CaixaService caixaService) {
        this.caixaService = caixaService;
    }

    public void setPedidoService(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    private void limparCarrinhoEInterface() {
        try {
            if (carrinho != null) {
                carrinho.clear();
                System.out.println("Carrinho limpo após venda");
            }

            limparCamposFormulario();

            limparCarrinhoNaTelaPrincipal();

        } catch (Exception e) {
            System.err.println("Erro ao limpar carrinho: " + e.getMessage());
        }
    }

    private void limparCamposFormulario() {
        if (campoNomeCliente != null) campoNomeCliente.clear();
        if (campoEndereco != null) campoEndereco.clear();
        if (campoTelefone != null) campoTelefone.clear();
        if (campoPontoReferencia != null) campoPontoReferencia.clear();
        if (campoObservacoes != null) campoObservacoes.clear();
        if (campoTaxaEntrega != null) campoTaxaEntrega.clear();
        if (campoNumero != null) campoNumero.clear();
        if (campoTempoPrevisao != null) campoTempoPrevisao.clear();
        if (campoValorPix != null) campoValorPix.clear();
        if (campoValorDinheiro != null) campoValorDinheiro.clear();
        if (campoValorDebito != null) campoValorDebito.clear();
        if (campoValorCredito != null) campoValorCredito.clear();
        if (campoValorPagoUnico != null) campoValorPagoUnico.clear();

        if (checkLoja != null) checkLoja.setSelected(false);
        if (checkSite != null) checkSite.setSelected(false);
        if (checkIfood != null) checkIfood.setSelected(false);
        if (checkOutro != null) checkOutro.setSelected(false);
        if (checkRetirada != null) checkRetirada.setSelected(false);
        if (checkEntrega != null) checkEntrega.setSelected(false);

        if (btnPix != null) btnPix.setSelected(false);
        if (btnDinheiro != null) btnDinheiro.setSelected(false);
        if (btnDebito != null) btnDebito.setSelected(false);
        if (btnCredito != null) btnCredito.setSelected(false);

        atualizarEstiloBotoesPagamento();

        if (labelTrocoDinheiro != null) labelTrocoDinheiro.setText("R$ 0,00");
        if (labelTrocoUnico != null) labelTrocoUnico.setText("R$ 0,00");
        if (labelTotalPago != null) labelTotalPago.setText("R$ 0,00");
        if (labelTotal != null) labelTotal.setText("R$ 0,00");

        inicializarEstadoPadrao();
    }

    private void limparCarrinhoNaTelaPrincipal() {
        try {
            if (telaRegistroController != null) {
                telaRegistroController.limparCarrinho();
            }

            Stage stageAtual = (Stage) (btnRegistrarPedido != null ?
                    btnRegistrarPedido.getScene().getWindow() : popupStage);

            if (stageAtual != null && stageAtual.getOwner() != null) {
                Stage ownerStage = (Stage) stageAtual.getOwner();
                if (ownerStage.getScene() != null && ownerStage.getScene().getRoot() != null) {
                    Object controller = ownerStage.getScene().getRoot().getUserData();
                    if (controller instanceof TelaRegistroPedidoController) {
                        ((TelaRegistroPedidoController) controller).limparCarrinho();
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Não foi possível limpar carrinho na tela principal: " + e.getMessage());
        }
    }

    public void setTelaRegistroController(TelaRegistroPedidoController controller) {
        this.telaRegistroController = controller;
    }
}