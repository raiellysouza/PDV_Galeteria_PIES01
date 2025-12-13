package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.PdvGaleteriaApplication;
import com.example.pdv_galeteria.model.ItemPedido;
import com.example.pdv_galeteria.model.Pedido;
import com.example.pdv_galeteria.model.Produto;
import com.example.pdv_galeteria.model.StatusPedido;
import com.example.pdv_galeteria.service.PedidoService;
import com.example.pdv_galeteria.service.ProdutoService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.awt.Desktop;

@Component
public class DashboardController implements Initializable {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ProdutoService produtoService;

    @FXML
    private Label lblTotalVendas;

    @FXML
    private Label lblPercentualVendas;

    @FXML
    private Label lblTotalPedidos;

    @FXML
    private Label lblVariacaoPedidos;

    @FXML
    private Label lblProdutosAtivos;

    @FXML
    private Label lblProdutosEstoqueBaixo;

    @FXML
    private Label lblNumeroEstoqueBaixo;

    @FXML
    private Button btnTodos;

    @FXML
    private Button btnRegistrado;

    @FXML
    private Button btnEmAndamento;

    @FXML
    private Button btnPronto;

    @FXML
    private Button btnFinalizado;

    @FXML
    private VBox vboxPedidos;

    @FXML
    private ComboBox<String> statusComboBox1;

    @FXML
    private ComboBox<String> statusComboBox2;

    private StatusPedido filtroAtual = null;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Map<Long, ComboBox<String>> combosPorPedido = new HashMap<>();
    private Map<Long, Pedido> pedidosExibidos = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            if (PdvGaleteriaApplication.getSpringContext() != null) {
                if (pedidoService == null) {
                    pedidoService = PdvGaleteriaApplication.getSpringContext().getBean(PedidoService.class);
                }
                if (produtoService == null) {
                    produtoService = PdvGaleteriaApplication.getSpringContext().getBean(ProdutoService.class);
                }
            }

            configurarFiltros();
            configurarComboBoxStatus();
            carregarDadosDashboard();
            carregarPedidos();
            configurarAtualizacaoAutomatica();

        } catch (Exception e) {
            mostrarErro("Erro ao inicializar dashboard: " + e.getMessage());
        }
    }

    private void configurarFiltros() {
        if (btnTodos != null) {
            btnTodos.setOnAction(e -> aplicarFiltro(null));
            btnTodos.setStyle("-fx-background-color: #7C0000; -fx-text-fill: white;");
        }
        if (btnRegistrado != null) {
            btnRegistrado.setOnAction(e -> aplicarFiltro(StatusPedido.REGISTRADO));
        }
        if (btnEmAndamento != null) {
            btnEmAndamento.setOnAction(e -> aplicarFiltro(StatusPedido.EM_ANDAMENTO));
        }
        if (btnPronto != null) {
            btnPronto.setOnAction(e -> aplicarFiltro(StatusPedido.PRONTO));
        }
        if (btnFinalizado != null) {
            btnFinalizado.setOnAction(e -> aplicarFiltro(StatusPedido.FINALIZADO));
        }
    }

    private void configurarComboBoxStatus() {
        if (statusComboBox1 != null) {
            statusComboBox1.getItems().addAll("REGISTRADO", "EM_ANDAMENTO", "PRONTO", "FINALIZADO");
            statusComboBox1.setValue("REGISTRADO");
            statusComboBox1.setOnAction(e -> {
                if (statusComboBox1.getUserData() != null) {
                    Long pedidoId = (Long) statusComboBox1.getUserData();
                    atualizarStatusPedido(pedidoId, statusComboBox1.getValue());
                }
            });
        }

        if (statusComboBox2 != null) {
            statusComboBox2.getItems().addAll("REGISTRADO", "EM_ANDAMENTO", "PRONTO", "FINALIZADO");
            statusComboBox2.setValue("REGISTRADO");
            statusComboBox2.setOnAction(e -> {
                if (statusComboBox2.getUserData() != null) {
                    Long pedidoId = (Long) statusComboBox2.getUserData();
                    atualizarStatusPedido(pedidoId, statusComboBox2.getValue());
                }
            });
        }
    }

    private void aplicarFiltro(StatusPedido status) {
        filtroAtual = status;
        atualizarEstiloBotoesFiltro();
        carregarPedidos();
    }

    private void atualizarEstiloBotoesFiltro() {
        Button[] botoes = {btnTodos, btnRegistrado, btnEmAndamento, btnPronto, btnFinalizado};
        StatusPedido[] status = {null, StatusPedido.REGISTRADO, StatusPedido.EM_ANDAMENTO,
                StatusPedido.PRONTO, StatusPedido.FINALIZADO};

        for (int i = 0; i < botoes.length; i++) {
            if (botoes[i] != null) {
                if (filtroAtual == status[i]) {
                    botoes[i].setStyle("-fx-background-color: #7C0000; -fx-text-fill: white;");
                } else {
                    botoes[i].setStyle("-fx-background-color: white; -fx-text-fill: #4b5563;");
                }
            }
        }
    }

    private void carregarDadosDashboard() {
        Platform.runLater(() -> {
            try {
                List<Pedido> pedidosHoje = getPedidosDoDia();

                System.out.println("=== CARREGANDO DADOS DASHBOARD ===");
                System.out.println("Total de pedidos hoje: " + pedidosHoje.size());

                double totalVendasHoje = pedidosHoje.stream()
                        .filter(p -> p.getStatus() == StatusPedido.FINALIZADO)
                        .mapToDouble(Pedido::getTotal)
                        .sum();

                System.out.println("Total vendas finalizadas: R$ " + totalVendasHoje);

                long totalPedidosHoje = pedidosHoje.size();

                List<Produto> produtos = produtoService.listarTodos();
                System.out.println("Total de produtos: " + produtos.size());

                long produtosAtivos = produtos.stream()
                        .filter(p -> p.getQuantidade() > 0)
                        .count();

                long estoqueBaixo = produtos.stream()
                        .filter(p -> p.getQuantidade() > 0 && p.getQuantidade() <= 10)
                        .count();

                System.out.println("Produtos ativos: " + produtosAtivos);
                System.out.println("Estoque baixo (<=10): " + estoqueBaixo);

                if (lblTotalVendas != null) {
                    lblTotalVendas.setText("R$ " + formatarValor(totalVendasHoje));
                }

                if (lblPercentualVendas != null) {
                    double percentual = calcularPercentualVendas(totalVendasHoje);
                    lblPercentualVendas.setText("+" + formatarPercentual(percentual) + "%");
                }

                if (lblTotalPedidos != null) {
                    lblTotalPedidos.setText(String.valueOf(totalPedidosHoje));
                }

                if (lblVariacaoPedidos != null) {
                    lblVariacaoPedidos.setText("+" + totalPedidosHoje + " hoje");
                }

                if (lblProdutosAtivos != null) {
                    lblProdutosAtivos.setText(String.valueOf(produtosAtivos));
                }

                if (lblNumeroEstoqueBaixo != null) {
                    lblNumeroEstoqueBaixo.setText(String.valueOf(estoqueBaixo));
                }

                if (lblProdutosEstoqueBaixo != null && estoqueBaixo > 0) {
                    lblProdutosEstoqueBaixo.setStyle("-fx-text-fill: #ef4444;");
                }

                System.out.println("=== DADOS CARREGADOS COM SUCESSO ===");

            } catch (Exception e) {
                System.err.println("Erro ao carregar dados do dashboard: " + e.getMessage());
                e.printStackTrace();
                mostrarErro("Erro ao carregar dados do dashboard: " + e.getMessage());
            }
        });
    }

    private double calcularPercentualVendas(double totalHoje) {
        try {
            List<Pedido> pedidosOntem = pedidoService.listarTodos().stream()
                    .filter(p -> p.getCriadoEm().toLocalDate().equals(LocalDate.now().minusDays(1)))
                    .filter(p -> p.getStatus() == StatusPedido.FINALIZADO)
                    .toList();

            double totalOntem = pedidosOntem.stream()
                    .mapToDouble(Pedido::getTotal)
                    .sum();

            if (totalOntem == 0) {
                return totalHoje > 0 ? 100.0 : 0.0;
            }

            return ((totalHoje - totalOntem) / totalOntem) * 100;

        } catch (Exception e) {
            return 0.0;
        }
    }

    private List<Pedido> getPedidosDoDia() {
        try {
            List<Pedido> todosPedidos = pedidoService.listarTodos();
            LocalDate hoje = LocalDate.now();

            System.out.println("Total de pedidos no sistema: " + todosPedidos.size());

            List<Pedido> pedidosHoje = todosPedidos.stream()
                    .filter(p -> {
                        if (p.getCriadoEm() == null) return false;
                        return p.getCriadoEm().toLocalDate().equals(hoje);
                    })
                    .toList();

            System.out.println("Pedidos de hoje: " + pedidosHoje.size());
            pedidosHoje.forEach(p ->
                    System.out.println("Pedido #" + p.getId() + " - " + p.getCliente() +
                            " - " + p.getStatus() + " - " + p.getTotal()));

            return pedidosHoje;

        } catch (Exception e) {
            System.err.println("Erro ao obter pedidos do dia: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void carregarPedidos() {
        if (vboxPedidos == null) {
            System.err.println("vboxPedidos é nulo!");
            return;
        }

        Platform.runLater(() -> {
            try {
                vboxPedidos.getChildren().clear();
                combosPorPedido.clear();
                pedidosExibidos.clear();

                List<Pedido> pedidos;
                if (filtroAtual == null) {
                    pedidos = pedidoService.listarTodos();
                } else {
                    pedidos = pedidoService.listarPorStatus(filtroAtual);
                }

                System.out.println("Total de pedidos para exibir: " + pedidos.size());

                List<Pedido> pedidosRecentes = pedidos.stream()
                        .sorted((p1, p2) -> {
                            if (p1.getCriadoEm() == null || p2.getCriadoEm() == null) {
                                return 0;
                            }
                            return p2.getCriadoEm().compareTo(p1.getCriadoEm());
                        })
                        .limit(10)
                        .toList();

                System.out.println("Pedidos recentes para exibir: " + pedidosRecentes.size());

                if (pedidosRecentes.isEmpty()) {
                    Label lblVazio = new Label("Nenhum pedido encontrado");
                    lblVazio.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px; -fx-padding: 40px;");
                    vboxPedidos.getChildren().add(lblVazio);
                    return;
                }

                for (int i = 0; i < pedidosRecentes.size(); i++) {
                    Pedido pedido = pedidosRecentes.get(i);
                    System.out.println("Criando card para pedido #" + pedido.getId() + " - " + pedido.getCliente());
                    pedidosExibidos.put(pedido.getId(), pedido);

                    VBox cardPedido = criarCardPedido(pedido, i);
                    vboxPedidos.getChildren().add(cardPedido);

                    if (i < pedidosRecentes.size() - 1) {
                        VBox.setMargin(cardPedido, new Insets(0, 0, 15, 0));
                    }
                }

                System.out.println("Cards de pedidos criados: " + vboxPedidos.getChildren().size());

            } catch (Exception e) {
                System.err.println("Erro ao carregar pedidos: " + e.getMessage());
                e.printStackTrace();
                mostrarErro("Erro ao carregar pedidos: " + e.getMessage());
            }
        });
    }

    private VBox criarCardPedido(Pedido pedido, int index) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e5e7eb; " +
                "-fx-border-radius: 12; -fx-border-width: 1px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        card.setPadding(new Insets(15));

        HBox header = criarHeaderPedido(pedido);
        HBox detalhes = criarDetalhesPedido(pedido);
        Separator separator = new Separator();
        separator.setOpacity(0.5);
        HBox footer = criarFooterPedido(pedido, index);

        card.getChildren().addAll(header, detalhes, separator, footer);
        return card;
    }

    private HBox criarHeaderPedido(Pedido pedido) {
        HBox header = new HBox();
        header.setSpacing(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblTitulo = new Label(pedido.getCliente() + " #" + String.format("%03d", pedido.getId()));
        lblTitulo.setStyle("-fx-text-fill: #111827; -fx-font-weight: bold; -fx-font-size: 16px;");

        HBox tags = criarTagsPedido(pedido);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox botoes = criarBotoesAcoes(pedido);

        header.getChildren().addAll(lblTitulo, tags, spacer, botoes);
        return header;
    }

    private HBox criarTagsPedido(Pedido pedido) {
        HBox tags = new HBox();
        tags.setSpacing(5);

        String corStatus = getCorStatus(pedido.getStatus());
        String corTextoStatus = getCorTextoStatus(pedido.getStatus());

        Label lblStatus = new Label(pedido.getStatus().toString());
        lblStatus.setStyle("-fx-background-color: " + corStatus + "; -fx-text-fill: " + corTextoStatus +
                "; -fx-background-radius: 4; -fx-padding: 2 8; -fx-font-weight: bold; -fx-font-size: 10px;");

        String tipoEntrega = pedido.getTipoEntrega() != null ? pedido.getTipoEntrega() : "Retirada";
        Label lblEntrega = new Label(tipoEntrega);
        lblEntrega.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #4B5563; " +
                "-fx-background-radius: 4; -fx-padding: 2 8; -fx-font-weight: bold; -fx-font-size: 10px;");

        String formaPagamento = pedido.getFormaPagamento() != null ? pedido.getFormaPagamento() : "Não Pago";
        String corPagamento = formaPagamento.equals("Não Pago") ? "#FFEDD5" : "#D1FAE5";
        String corTextoPagamento = formaPagamento.equals("Não Pago") ? "#C2410C" : "#065F46";

        Label lblPagamento = new Label(formaPagamento);
        lblPagamento.setStyle("-fx-background-color: " + corPagamento + "; -fx-text-fill: " + corTextoPagamento +
                "; -fx-background-radius: 4; -fx-padding: 2 8; -fx-font-weight: bold; -fx-font-size: 10px;");

        tags.getChildren().addAll(lblStatus, lblEntrega, lblPagamento);
        return tags;
    }

    private String getCorStatus(StatusPedido status) {
        switch (status) {
            case REGISTRADO: return "#DBEAFE";
            case EM_ANDAMENTO: return "#FEF3C7";
            case PRONTO: return "#D1FAE5";
            case FINALIZADO: return "#D1FAE5";
            default: return "#E5E7EB";
        }
    }

    private String getCorTextoStatus(StatusPedido status) {
        switch (status) {
            case REGISTRADO: return "#1D4ED8";
            case EM_ANDAMENTO: return "#92400E";
            case PRONTO: return "#065F46";
            case FINALIZADO: return "#065F46";
            default: return "#6B7280";
        }
    }

    private HBox criarBotoesAcoes(Pedido pedido) {
        HBox botoes = new HBox(5);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        Button btnVisualizar = criarBotaoAcao("ver", "Visualizar detalhes",
                e -> visualizarPedido(pedido.getId()));

        Button btnEditar = criarBotaoAcao("editar", "Editar pedido",
                e -> editarPedido(pedido.getId()));

        /**Button btnImprimir = criarBotaoAcao("imprimir", "Imprimir pedido",
                e -> imprimirPedido(pedido.getId()));**/

        Button btnExcluir = criarBotaoAcao("excluir", "Excluir pedido",
                e -> confirmarExclusaoPedido(pedido.getId()));

        btnExcluir.setStyle("-fx-background-color: #fee2e2; -fx-border-color: #fca5a5;");

        botoes.getChildren().addAll(btnVisualizar, btnEditar,btnExcluir);
        return botoes;
    }

    private Button criarBotaoAcao(String nomeIcone, String tooltipTexto, EventHandler<ActionEvent> acao) {
        Button btn = new Button();
        btn.getStyleClass().add("btn-action");
        btn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        ImageView icon = carregarIcone(nomeIcone);
        if (icon != null) {
            btn.setGraphic(icon);
        } else {
            btn.setText(getTextoFallback(nomeIcone));
            btn.setStyle("-fx-background-color: #f3f4f6; -fx-border-color: #d1d5db; -fx-border-radius: 4;");
        }

        btn.setOnAction(acao);

        Tooltip tooltip = new Tooltip(tooltipTexto);
        Tooltip.install(btn, tooltip);

        return btn;
    }

    private ImageView carregarIcone(String nomeIcone) {
        try {
            String[] caminhos = {
                    System.getProperty("user.dir") + "/src/main/resources/assets/imgs/" + nomeIcone + ".png",
                    System.getProperty("user.dir") + "/assets/imgs/" + nomeIcone + ".png",
                    System.getProperty("user.dir") + "/imgs/" + nomeIcone + ".png",
                    "assets/imgs/" + nomeIcone + ".png",
                    "/assets/imgs/" + nomeIcone + ".png"
            };

            for (String caminho : caminhos) {
                File file = new File(caminho);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    ImageView icon = new ImageView(image);
                    icon.setFitWidth(16);
                    icon.setFitHeight(16);
                    return icon;
                }
            }

            try {
                URL resource = getClass().getResource("/assets/imgs/" + nomeIcone + ".png");
                if (resource != null) {
                    Image image = new Image(resource.toString());
                    ImageView icon = new ImageView(image);
                    icon.setFitWidth(16);
                    icon.setFitHeight(16);
                    return icon;
                }
            } catch (Exception e) {
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone " + nomeIcone + ": " + e.getMessage());
        }

        return null;
    }

    private String getTextoFallback(String nomeIcone) {
        switch (nomeIcone) {
            case "ver": return "👁";
            case "editar": return "✏";
            case "imprimir": return "🖨";
            case "excluir": return "🗑";
            default: return "•";
        }
    }

    private HBox criarDetalhesPedido(Pedido pedido) {
        HBox detalhes = new HBox(30);

        VBox coluna1 = new VBox(5);
        coluna1.setPrefWidth(400);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm:ss");
        Label lblData = new Label(pedido.getCriadoEm().format(formatter));
        lblData.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");

        VBox itensContainer = new VBox(2);
        Label lblItens = new Label("Itens:");
        lblItens.setStyle("-fx-text-fill: #6b7280; -fx-font-weight: bold; -fx-font-size: 12px;");
        itensContainer.getChildren().add(lblItens);

        try {
            Pedido pedidoComItens = pedidoService.buscarPedidoComItens(pedido.getId());

            if (pedidoComItens.getItens() != null && !pedidoComItens.getItens().isEmpty()) {
                for (ItemPedido item : pedidoComItens.getItens()) {
                    Label lblItem = new Label(item.getQuantidade() + "x " + item.getProduto());
                    lblItem.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 12px;");
                    itensContainer.getChildren().add(lblItem);
                }
            } else {
                Label lblSemItens = new Label("Nenhum item encontrado");
                lblSemItens.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px; -fx-font-style: italic;");
                itensContainer.getChildren().add(lblSemItens);
            }
        } catch (Exception e) {
            Label lblErro = new Label("Erro ao carregar itens");
            lblErro.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
            itensContainer.getChildren().add(lblErro);
        }

        coluna1.getChildren().addAll(lblData, itensContainer);

        VBox coluna2 = new VBox(5);
        coluna2.setPrefWidth(300);

        if (pedido.getFormaPagamento() != null) {
            Label lblPagamento = new Label("Pagamento:");
            lblPagamento.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");

            Label lblFormaPagamento = new Label(pedido.getFormaPagamento());
            lblFormaPagamento.setStyle("-fx-text-fill: #4b5563; -fx-font-weight: bold; -fx-font-size: 12px;");

            coluna2.getChildren().addAll(lblPagamento, lblFormaPagamento);
        }

        detalhes.getChildren().addAll(coluna1, coluna2);
        return detalhes;
    }
    private HBox criarFooterPedido(Pedido pedido, int index) {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);

        Label lblValor = new Label("R$ " + formatarValor(pedido.getTotal()));
        lblValor.setStyle("-fx-text-fill: #111827; -fx-font-weight: bold; -fx-font-size: 18px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox containerDireita = new HBox();
        containerDireita.setAlignment(Pos.CENTER_RIGHT);
        containerDireita.setSpacing(-10);

        ComboBox<String> comboStatus = new ComboBox<>();
        comboStatus.getItems().addAll("REGISTRADO", "EM_ANDAMENTO", "PRONTO", "FINALIZADO");
        comboStatus.setValue(pedido.getStatus().toString());
        comboStatus.setPrefHeight(30);
        comboStatus.setPrefWidth(140);
        comboStatus.setStyle("-fx-background-color: #E5E7EB; -fx-background-radius: 20; " +
                "-fx-font-weight: bold; -fx-text-base-color: #374151;");
        comboStatus.setUserData(pedido.getId());

        comboStatus.setOnAction(e -> {
            Long pedidoId = (Long) comboStatus.getUserData();
            String novoStatus = comboStatus.getValue();
            atualizarStatusPedido(pedidoId, novoStatus);
        });

        combosPorPedido.put(pedido.getId(), comboStatus);

        ImageView avatar = criarAvatarPlaceholder();

        containerDireita.getChildren().addAll(comboStatus, avatar);
        footer.getChildren().addAll(lblValor, spacer, containerDireita);

        return footer;
    }

    private ImageView criarAvatarPlaceholder() {
        ImageView avatar = new ImageView();
        avatar.setFitHeight(32);
        avatar.setFitWidth(32);
        avatar.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 4, 0, 0, 2);");

        try {
            String avatarPath = System.getProperty("user.dir") + "/src/main/resources/assets/imgs/avatar_placeholder.png";
            File file = new File(avatarPath);
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                avatar.setImage(image);
            }
        } catch (Exception e) {
        }

        Circle clip = new Circle(16, 16, 16);
        avatar.setClip(clip);

        return avatar;
    }

    private void atualizarStatusPedido(Long pedidoId, String novoStatusStr) {
        try {
            StatusPedido novoStatus = StatusPedido.valueOf(novoStatusStr);
            pedidoService.atualizarStatus(pedidoId, novoStatus);

            if (pedidosExibidos.containsKey(pedidoId)) {
                Pedido pedido = pedidosExibidos.get(pedidoId);
                pedido.setStatus(novoStatus);
                carregarDadosDashboard();
            }

        } catch (Exception e) {
            mostrarErro("Erro ao atualizar status: " + e.getMessage());
        }
    }

    private void confirmarExclusaoPedido(Long pedidoId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Deseja realmente excluir este pedido?");
        alert.setContentText("Esta ação não pode ser desfeita.");

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                pedidoService.excluirPedido(pedidoId);
                carregarPedidos();
                carregarDadosDashboard();
                mostrarSucesso("Pedido excluído com sucesso!");
            } catch (Exception e) {
                mostrarErro("Erro ao excluir pedido: " + e.getMessage());
            }
        }
    }

    private void visualizarPedido(Long pedidoId) {
        try {
            Pedido pedido = pedidoService.buscarPedidoComItens(pedidoId);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Detalhes do Pedido");
            alert.setHeaderText("Pedido #" + pedidoId + " - " + pedido.getCliente());

            StringBuilder content = new StringBuilder();
            content.append("Data: ").append(pedido.getCriadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
            content.append("Status: ").append(pedido.getStatus()).append("\n");
            content.append("Forma de Pagamento: ").append(pedido.getFormaPagamento()).append("\n");
            content.append("Tipo de Entrega: ").append(pedido.getTipoEntrega()).append("\n\n");
            content.append("Itens:\n");

            if (pedido.getItens() != null && !pedido.getItens().isEmpty()) {
                for (ItemPedido item : pedido.getItens()) {
                    content.append("  • ").append(item.getQuantidade())
                            .append("x ").append(item.getProduto())
                            .append(" - R$ ").append(formatarValor(item.getPrecoUnitario()))
                            .append(" cada (Subtotal: R$ ").append(formatarValor(item.getQuantidade() * item.getPrecoUnitario()))
                            .append(")\n");
                }
            } else {
                content.append("  Nenhum item encontrado\n");
            }

            content.append("\nTotal: R$ ").append(formatarValor(pedido.getTotal()));

            alert.setContentText(content.toString());
            alert.setWidth(600);
            alert.setHeight(400);
            alert.showAndWait();

        } catch (Exception e) {
            System.err.println("Erro ao visualizar pedido: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao visualizar pedido: " + e.getMessage());
        }
    }

    private void editarPedido(Long pedidoId) {
        try {
            Pedido pedido = pedidoService.buscarPedidoComItens(pedidoId);

            TextInputDialog dialog = new TextInputDialog(pedido.getCliente());
            dialog.setTitle("Editar Pedido");
            dialog.setHeaderText("Editar informações do pedido #" + pedidoId);
            dialog.setContentText("Nome do cliente:");

            Optional<String> resultado = dialog.showAndWait();
            if (resultado.isPresent() && !resultado.get().isEmpty()) {
                pedido.setCliente(resultado.get().trim());
                pedidoService.atualizarCliente(pedidoId, resultado.get().trim());
                carregarPedidos();
                mostrarSucesso("Pedido atualizado com sucesso!");
            }

        } catch (Exception e) {
            mostrarErro("Erro ao editar pedido: " + e.getMessage());
        }
    }

    private void configurarAtualizacaoAutomatica() {
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                carregarDadosDashboard();
                carregarPedidos();
            });
        }, 60, 60, TimeUnit.SECONDS);
    }

    private String formatarValor(Double valor) {
        if (valor == null) {
            return "0,00";
        }
        DecimalFormat df = new DecimalFormat("#,##0.00",
                new DecimalFormatSymbols(new Locale("pt", "BR")));
        return df.format(valor);
    }

    private String formatarPercentual(Double percentual) {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(percentual);
    }

    private void mostrarSucesso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    public void abrirTelaVendas(ActionEvent actionEvent) {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaRegistroPedido.fxml");
            if (fxmlUrl == null) {
                mostrarErro("Arquivo da tela de vendas não encontrado!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setTitle("Registro de Pedidos");
            stage.centerOnScreen();
        } catch (Exception e) {
            mostrarErro("Erro ao abrir tela de vendas: " + e.getMessage());
        }
    }

    public void abrirTelaCaixa(ActionEvent actionEvent) {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaCaixa.fxml");
            if (fxmlUrl == null) {
                mostrarErro("Arquivo da tela de caixa não encontrado!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setTitle("Caixa");
            stage.centerOnScreen();
        } catch (Exception e) {
            mostrarErro("Erro ao abrir tela de caixa: " + e.getMessage());
        }
    }

    public void abrirTelaEstoque(ActionEvent actionEvent) {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaProdutos.fxml");
            if (fxmlUrl == null) {
                mostrarErro("Arquivo da tela de estoque não encontrado!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setTitle("Estoque");
            stage.centerOnScreen();
        } catch (Exception e) {
            mostrarErro("Erro ao abrir tela de estoque: " + e.getMessage());
        }
    }

    public void abrirTelaEntregadores(ActionEvent actionEvent) {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaEntregadores.fxml");
            if (fxmlUrl == null) {
                mostrarErro("Arquivo da tela de entregadores não encontrado!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setTitle("Entregadores");
            stage.centerOnScreen();
        } catch (Exception e) {
            mostrarErro("Erro ao abrir tela de entregadores: " + e.getMessage());
        }
    }

    public void abrirTelaRelatorios(ActionEvent actionEvent) {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaRelatorio.fxml");
            if (fxmlUrl == null) {
                mostrarErro("Arquivo da tela de relatórios não encontrado!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setTitle("Relatórios");
            stage.centerOnScreen();
        } catch (Exception e) {
            mostrarErro("Erro ao abrir tela de relatórios: " + e.getMessage());
        }
    }

    public void abrirTelaConfiguracoes(ActionEvent actionEvent) {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaConfiguracao.fxml");
            if (fxmlUrl == null) {
                mostrarErro("Arquivo da tela de configurações não encontrado!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setTitle("Configurações");
            stage.centerOnScreen();
        } catch (Exception e) {
            mostrarErro("Erro ao abrir tela de configurações: " + e.getMessage());
        }
    }

    public void sairParaLogin(ActionEvent actionEvent) {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaSairPrograma.fxml");
            if (fxmlUrl == null) {
                mostrarConfirmacaoSaida();
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            ConfirmacaoSaidaController controller = loader.getController();

            Stage popupStage = new Stage();
            controller.setPopupStage(popupStage);

            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Confirmação de Saída");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(getCurrentStage());
            popupStage.setResizable(false);
            popupStage.centerOnScreen();

            popupStage.showAndWait();

            if (controller.isConfirmado()) {
                voltarParaTelaLogin();
            }
        } catch (Exception e) {
            mostrarConfirmacaoSaida();
        }
    }

    private void mostrarConfirmacaoSaida() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação de Saída");
        alert.setHeaderText("Deseja realmente sair?");
        alert.setContentText("Você será redirecionado para a tela de login.");

        Optional<javafx.scene.control.ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == javafx.scene.control.ButtonType.OK) {
            voltarParaTelaLogin();
        }
    }

    private void voltarParaTelaLogin() {
        try {
            Stage stage = getCurrentStage();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaLogin.fxml"));

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();
        } catch (Exception e) {
            reiniciarAplicacaoCompleta();
        }
    }

    private Stage getCurrentStage() {
        try {
            for (Window window : Window.getWindows()) {
                if (window instanceof Stage && window.isShowing()) {
                    return (Stage) window;
                }
            }

            Stage primaryStage = (Stage) Stage.getWindows().get(0);
            if (primaryStage != null) {
                return primaryStage;
            }

            return new Stage();
        } catch (Exception e) {
            return new Stage();
        }
    }

    private void reiniciarAplicacaoCompleta() {
        try {
            Stage stage = getCurrentStage();
            stage.close();
            PdvGaleteriaApplication.main(new String[]{});
        } catch (Exception e) {
            mostrarErro("Erro crítico. Feche e abra o aplicativo manualmente.");
        }
    }

    /**
    private void mostrarReciboEmTela(String conteudo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Recibo do Pedido");
        alert.setHeaderText("Conteúdo para impressão");
        alert.setWidth(500);
        alert.setHeight(600);

        TextArea textArea = new TextArea(conteudo);
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 12px;");
        textArea.setPrefSize(450, 550);

        ButtonType copiarButton = new ButtonType("Copiar");
        ButtonType fecharButton = new ButtonType("Fechar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(copiarButton, fecharButton);

        alert.getDialogPane().setContent(textArea);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == copiarButton) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(conteudo);
                clipboard.setContent(content);
                mostrarSucesso("Recibo copiado para a área de transferência!");
            }
        });
    }

    private void imprimirPedido(Long pedidoId) {
        try {
            Pedido pedido = pedidoService.buscarPedidoComItens(pedidoId);

            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setTitle("Imprimir Pedido #" + pedidoId);
            alert.setHeaderText("Escolha o formato de impressão:");

            ButtonType btnPDF = new ButtonType("Gerar PDF");
            ButtonType btnTXT = new ButtonType("Gerar TXT");
            ButtonType btnCopiar = new ButtonType("Copiar");
            ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(btnPDF, btnTXT, btnCopiar, btnCancelar);

            TextArea preview = new TextArea(gerarReciboFormatado(pedido));
            preview.setEditable(false);
            preview.setWrapText(true);
            preview.setPrefHeight(150);
            preview.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11px;");

            VBox content = new VBox(10);
            content.getChildren().addAll(
                    new Label("Prévia do pedido:"),
                    preview
            );

            alert.getDialogPane().setContent(content);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == btnPDF) {
                    gerarPDFPedido(pedido);
                } else if (result.get() == btnTXT) {
                    gerarTXTPedido(pedido);
                } else if (result.get() == btnCopiar) {
                    copiarReciboPedido(pedido);
                }
            }

        } catch (Exception e) {
            mostrarErro("Erro ao gerar recibo: " + e.getMessage());
            e.printStackTrace();
        }
    }**/

    private Button criarBotaoOpcao(String texto, String tooltip) {
        Button btn = new Button(texto);
        btn.setPrefWidth(200);
        btn.setPrefHeight(40);
        btn.setStyle("-fx-font-size: 14px; -fx-background-color: #f3f4f6; -fx-border-color: #d1d5db; -fx-border-radius: 5;");

        Tooltip tp = new Tooltip(tooltip);
        Tooltip.install(btn, tp);

        return btn;
    }

    /**
    private void gerarPDFPedido(Pedido pedido) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvar PDF do Pedido");
            fileChooser.setInitialFileName("pedido_" + pedido.getId() + ".pdf");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Arquivo PDF", "*.pdf"),
                    new FileChooser.ExtensionFilter("Todos os arquivos", "*.*")
            );

            Stage stage = (Stage) vboxPedidos.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                String caminho = file.getAbsolutePath();
                if (!caminho.toLowerCase().endsWith(".pdf")) {
                    caminho += ".pdf";
                    file = new File(caminho);
                }

                File pdfFile = pdfService.gerarReciboPedido(pedido, caminho);

                Alert sucesso = new Alert(Alert.AlertType.CONFIRMATION);
                sucesso.setTitle("PDF Gerado com Sucesso!");
                sucesso.setHeaderText("PDF salvo em: " + pdfFile.getAbsolutePath());
                sucesso.setContentText("Deseja abrir o arquivo?");

                ButtonType btnAbrir = new ButtonType("Abrir");
                ButtonType btnFechar = new ButtonType("Fechar", ButtonBar.ButtonData.CANCEL_CLOSE);
                sucesso.getButtonTypes().setAll(btnAbrir, btnFechar);

                Optional<ButtonType> result = sucesso.showAndWait();
                if (result.isPresent() && result.get() == btnAbrir) {
                    abrirArquivo(pdfFile);
                }
            }
        } catch (Exception e) {
            mostrarErro("Erro ao gerar PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void gerarTXTPedido(Pedido pedido) {
        try {
            String recibo = gerarReciboFormatado(pedido);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvar Recibo do Pedido");
            fileChooser.setInitialFileName("pedido_" + pedido.getId() + ".txt");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Arquivo de Texto", "*.txt"),
                    new FileChooser.ExtensionFilter("Todos os arquivos", "*.*")
            );

            Stage stage = (Stage) vboxPedidos.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
                    writer.print(recibo);
                }
                mostrarSucesso("Recibo salvo em: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            mostrarErro("Erro ao salvar TXT: " + e.getMessage());
        }
    }

    private void abrirArquivo(File arquivo) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (arquivo.exists() && desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(arquivo);
                }
            }
        } catch (Exception e) {
            System.err.println("Não foi possível abrir o arquivo: " + e.getMessage());
        }
    }

    private void copiarParaAreaTransferencia(String texto) {
        try {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(texto);
            clipboard.setContent(content);
        } catch (Exception e) {
            System.err.println("Erro ao copiar para área de transferência: " + e.getMessage());
        }
    }

    private void copiarReciboPedido(Pedido pedido) {
        try {
            String recibo = gerarReciboFormatado(pedido);
            copiarParaAreaTransferencia(recibo);
            mostrarSucesso("Recibo copiado para a área de transferência!");
        } catch (Exception e) {
            mostrarErro("Erro ao copiar recibo: " + e.getMessage());
        }
    }

    private String gerarReciboFormatado(Pedido pedido) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("           GALETERIA PDV               \n");
        sb.append("========================================\n\n");
        sb.append("RECIBO DO PEDIDO\n");
        sb.append("Número: #").append(String.format("%03d", pedido.getId())).append("\n");
        sb.append("Data: ").append(pedido.getCriadoEm().format(dtf)).append("\n");
        sb.append("Cliente: ").append(pedido.getCliente()).append("\n");
        sb.append("Status: ").append(pedido.getStatus()).append("\n");
        sb.append("Forma de Pagamento: ").append(pedido.getFormaPagamento()).append("\n");
        sb.append("Tipo de Entrega: ").append(pedido.getTipoEntrega()).append("\n");
        sb.append("----------------------------------------\n");
        sb.append("ITENS DO PEDIDO:\n");
        sb.append("----------------------------------------\n");

        if (pedido.getItens() != null && !pedido.getItens().isEmpty()) {
            sb.append("QTD  DESCRIÇÃO                       PREÇO    SUBTOTAL\n");
            sb.append("---- ------------------------------ -------- ---------\n");

            for (ItemPedido item : pedido.getItens()) {
                String produto = item.getProduto();
                if (produto.length() > 30) produto = produto.substring(0, 27) + "...";

                double subtotal = item.getQuantidade() * item.getPrecoUnitario();
                sb.append(String.format("%3d  %-30s %8.2f %9.2f\n",
                        item.getQuantidade(),
                        produto,
                        item.getPrecoUnitario(),
                        subtotal));
            }
        }

        sb.append("----------------------------------------\n");
        sb.append(String.format("TOTAL: R$ %33.2f\n", pedido.getTotal()));
        sb.append("========================================\n");
        sb.append("Obrigado pela preferência!\n");
        sb.append("========================================\n");

        return sb.toString();
    }**/
}