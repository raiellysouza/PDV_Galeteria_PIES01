package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.PdvGaleteriaApplication;
import com.example.pdv_galeteria.model.Produto;
import com.example.pdv_galeteria.model.UsuarioSessao;
import com.example.pdv_galeteria.service.CaixaService;
import com.example.pdv_galeteria.service.PedidoService;
import com.example.pdv_galeteria.service.ProdutoService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

@Component
public class TelaRegistroPedidoController implements Initializable {

    @FXML
    private AnchorPane contentPane;

    @FXML
    private FlowPane produtosContainer;

    @FXML
    private ScrollPane scrollProdutos;

    @FXML
    private TextField campoBusca;

    @FXML
    private VBox carrinhoContainer;

    @FXML
    private Label labelTotal;

    @FXML
    private Button btnRegistrarPedido;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private CaixaService caixaService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private UsuarioSessao usuarioSessao;

    @FXML
    private Label labelNomeUsuario;

    private Map<Produto, Integer> carrinho = new LinkedHashMap<>();
    private List<Produto> todosProdutos = new ArrayList<>();
    private List<Produto> produtosFiltrados = new ArrayList<>();
    private double totalPedido = 0.0;
    private DecimalFormat df = new DecimalFormat("#,##0.00");
    private Timer timerBusca;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        timerBusca = new Timer();

        campoBusca.setPromptText("Buscar produto...");
        campoBusca.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #d1d5db; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-padding: 10 12; " +
                "-fx-font-size: 14px;");

        if (scrollProdutos != null) {
            scrollProdutos.setFitToWidth(true);
            scrollProdutos.setFitToHeight(true);
            scrollProdutos.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollProdutos.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollProdutos.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        }

        if (produtosContainer != null) {
            produtosContainer.setHgap(15);
            produtosContainer.setVgap(15);
            produtosContainer.setPadding(new Insets(15));
            produtosContainer.setStyle("-fx-background-color: transparent;");
        }
        if (carrinhoContainer != null) {
            carrinhoContainer.setSpacing(8);
        }

        if (btnRegistrarPedido != null) {
            btnRegistrarPedido.setOnAction(e -> registrarPedido());
            btnRegistrarPedido.setDisable(true);
        }

        produtosContainer.prefWrapLengthProperty().bind(
                scrollProdutos.widthProperty().subtract(25)
        );

        atualizarNomeUsuarioNoMenu();

        carregarProdutos();
        atualizarCarrinho();
    }

    private void atualizarNomeUsuarioNoMenu() {
        if (labelNomeUsuario != null && usuarioSessao != null) {
            String nome = usuarioSessao.getNomeUsuario();
            System.out.println("Atualizando nome do usuário: " + nome);
            labelNomeUsuario.setText(nome);
        } else {
            System.out.println("Erro: labelNomeUsuario ou usuarioSessao é nulo");
            System.out.println("labelNomeUsuario: " + (labelNomeUsuario != null ? "OK" : "NULO"));
            System.out.println("usuarioSessao: " + (usuarioSessao != null ? "OK" : "NULO"));
        }
    }

    private void carregarProdutos() {
        try {
            todosProdutos = produtoService.listarTodos();
            produtosFiltrados = new ArrayList<>(todosProdutos);
            renderizarProdutos(produtosFiltrados);

        } catch (Exception e) {
            System.err.println("Erro ao carregar produtos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderizarProdutos(List<Produto> produtos) {
        if (produtosContainer == null) {
            System.err.println("produtosContainer é nulo!");
            return;
        }

        Platform.runLater(() -> {
            try {
                produtosContainer.getChildren().clear();

                if (produtos.isEmpty()) {
                    Label vazio = new Label("Nenhum produto encontrado");
                    vazio.setStyle("-fx-text-fill: #666; -fx-font-size: 14px; -fx-padding: 20;");
                    produtosContainer.getChildren().add(vazio);
                    return;
                }

                for (Produto produto : produtos) {
                    Pane card = criarCardProduto(produto);
                    produtosContainer.getChildren().add(card);
                }

            } catch (Exception e) {
                System.err.println("Erro ao renderizar produtos: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private Pane criarCardProduto(Produto produto) {
        Pane card = new Pane();
        card.setMinSize(300, 180);
        card.setMaxSize(300, 180);
        card.setPrefSize(300, 180);

        card.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #e5e7eb; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.getStyleClass().add("card-produtos");

        Label labelNome = new Label(produto.getNome());
        labelNome.setLayoutX(20);
        labelNome.setLayoutY(30);
        labelNome.setFont(Font.font("System", FontWeight.BOLD, 25));
        labelNome.setStyle("-fx-text-fill: #111827;");
        labelNome.setWrapText(true);
        labelNome.setMaxWidth(260);

        Label labelPreco = new Label(String.format("R$ %.2f", produto.getPreco()));
        labelPreco.setLayoutX(40);
        labelPreco.setLayoutY(100);
        labelPreco.setFont(Font.font("System", FontWeight.BOLD, 28));
        labelPreco.setStyle("-fx-text-fill: #2a6df4;");

        String categoria = determinarCategoria(produto);
        Label labelCategoria = new Label(categoria);

        double categoriaWidth = categoria.length() * 8 + 24;
        double categoriaX = 300 - categoriaWidth - 20;

        labelCategoria.setLayoutX(categoriaX);
        labelCategoria.setLayoutY(70);
        labelCategoria.setFont(Font.font("System", FontWeight.BOLD, 12));
        labelCategoria.setStyle("-fx-text-fill: #374151; " +
                "-fx-background-color: #F3F4F6; " +
                "-fx-padding: 4 12; " +
                "-fx-background-radius: 20;");

        card.getChildren().addAll(labelNome, labelPreco, labelCategoria);

        card.setCursor(Cursor.HAND);
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #f8fafc; " +
                    "-fx-border-color: #f68411; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 12; " +
                    "-fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.3), 10, 0, 0, 4);");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; " +
                    "-fx-border-color: #e5e7eb; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 12; " +
                    "-fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        });

        card.setOnMouseClicked(e -> adicionarProduto(produto));

        return card;
    }

    private String determinarCategoria(Produto produto) {
        String nome = produto.getNome().toLowerCase();
        if (nome.contains("galeto")) return "Principal";
        if (nome.contains("refrigerante") || nome.contains("bebida")) return "Bebida";
        if (nome.contains("batata") || nome.contains("frita")) return "Acompanhamento";
        if (nome.contains("combo")) return "Combo";
        if (nome.contains("maminha")) return "Principal";
        return "Produto";
    }

    @FXML
    private void onBuscaKeyReleased() {
        String termoBusca = campoBusca.getText().trim();

        if (timerBusca != null) {
            timerBusca.cancel();
            timerBusca.purge();
        }

        if (termoBusca.isEmpty()) {
            timerBusca = new Timer();
            timerBusca.schedule(new TimerTask() {
                @Override
                public void run() {
                    restaurarTelaOriginal();
                }
            }, 100);
            return;
        }

        timerBusca = new Timer();
        timerBusca.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> executarBusca(termoBusca));
            }
        }, 300);
    }

    private void executarBusca(String termoBusca) {
        try {
            List<Produto> produtosEncontrados = produtoService.buscarListaPorNome(termoBusca);

            if (produtosEncontrados.isEmpty()) {
                produtosFiltrados = new ArrayList<>();
            } else {
                produtosFiltrados = produtosEncontrados;
            }

            renderizarProdutos(produtosFiltrados);

        } catch (Exception e) {
            System.err.println("Erro na busca: " + e.getMessage());
            produtosFiltrados = todosProdutos.stream()
                    .filter(p -> p.getNome().toLowerCase().contains(termoBusca.toLowerCase()))
                    .toList();
            renderizarProdutos(produtosFiltrados);
        }
    }

    private void restaurarTelaOriginal() {
        Platform.runLater(() -> {
            produtosFiltrados = new ArrayList<>(todosProdutos);
            renderizarProdutos(produtosFiltrados);
        });
    }

    public void adicionarProduto(Produto produto) {
        if (produto == null) return;

        int quantidadeAtual = carrinho.getOrDefault(produto, 0);
        carrinho.put(produto, quantidadeAtual + 1);
        atualizarCarrinho();
    }

    public void incrementarQuantidade(Produto produto) {
        if (produto == null || !carrinho.containsKey(produto)) return;

        int quantidadeAtual = carrinho.get(produto);
        carrinho.put(produto, quantidadeAtual + 1);
        atualizarCarrinho();
    }

    public void decrementarQuantidade(Produto produto) {
        if (produto == null || !carrinho.containsKey(produto)) return;

        int quantidadeAtual = carrinho.get(produto);
        if (quantidadeAtual > 1) {
            carrinho.put(produto, quantidadeAtual - 1);
        } else {
            carrinho.remove(produto);
        }
        atualizarCarrinho();
    }

    public void removerProduto(Produto produto) {
        if (produto == null || !carrinho.containsKey(produto)) return;

        carrinho.remove(produto);
        atualizarCarrinho();
    }

    private void atualizarCarrinho() {
        if (carrinhoContainer == null) return;

        Platform.runLater(() -> {
            try {
                carrinhoContainer.getChildren().clear();
                totalPedido = 0.0;

                if (carrinho.isEmpty()) {
                    Label labelVazio = new Label("Carrinho vazio");
                    labelVazio.setStyle("-fx-text-fill: #666; -fx-font-size: 14px; -fx-padding: 20;");
                    carrinhoContainer.getChildren().add(labelVazio);
                } else {
                    for (Map.Entry<Produto, Integer> entry : carrinho.entrySet()) {
                        Produto produto = entry.getKey();
                        int quantidade = entry.getValue();
                        double subtotal = produto.getPreco() * quantidade;
                        totalPedido += subtotal;

                        Pane itemPane = criarItemCarrinhoEstatico(produto, quantidade, subtotal);
                        carrinhoContainer.getChildren().add(itemPane);

                        Separator separator = new Separator();
                        separator.setPrefWidth(240);
                        separator.setStyle("-fx-padding: 8 0; -fx-opacity: 0.3;");
                        carrinhoContainer.getChildren().add(separator);
                    }

                    if (!carrinhoContainer.getChildren().isEmpty()) {
                        carrinhoContainer.getChildren().remove(carrinhoContainer.getChildren().size() - 1);
                    }
                }

                if (labelTotal != null) {
                    labelTotal.setText("R$ " + df.format(totalPedido));
                    labelTotal.setStyle("-fx-text-fill: #2a6df4;");
                }

                if (btnRegistrarPedido != null) {
                    btnRegistrarPedido.setDisable(carrinho.isEmpty());
                }

            } catch (Exception e) {
                System.err.println("Erro ao atualizar carrinho: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private Pane criarItemCarrinhoEstatico(Produto produto, int quantidade, double subtotal) {
        Pane itemPane = new Pane();
        itemPane.setPrefSize(240, 60);
        itemPane.setStyle("-fx-background-color: transparent;");

        Label labelNome = new Label(produto.getNome());
        labelNome.setLayoutX(0);
        labelNome.setLayoutY(0);
        labelNome.setFont(Font.font("System", FontWeight.BOLD, 14));
        labelNome.setStyle("-fx-text-fill: #111827;");
        labelNome.setWrapText(true);
        labelNome.setMaxWidth(140);

        Label labelDetalhes = new Label(
                String.format("R$ %.2f x %d", produto.getPreco(), quantidade)
        );
        labelDetalhes.setLayoutX(0);
        labelDetalhes.setLayoutY(22);
        labelDetalhes.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        Button btnMenos = new Button("-");
        btnMenos.setLayoutX(140);
        btnMenos.setLayoutY(15);
        btnMenos.setPrefSize(25, 25);
        btnMenos.setStyle("-fx-background-color: #f3f4f6; " +
                "-fx-background-radius: 4; " +
                "-fx-border-color: #d1d5db; " +
                "-fx-border-radius: 4; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12px; " +
                "-fx-cursor: hand;");
        btnMenos.setOnAction(e -> decrementarQuantidade(produto));

        Label labelQuantidade = new Label(String.valueOf(quantidade));
        labelQuantidade.setLayoutX(170);
        labelQuantidade.setLayoutY(15);
        labelQuantidade.setFont(Font.font("System", FontWeight.BOLD, 16));
        labelQuantidade.setStyle("-fx-text-fill: #111827;");

        Button btnMais = new Button("+");
        btnMais.setLayoutX(195);
        btnMais.setLayoutY(15);
        btnMais.setPrefSize(25, 25);
        btnMais.setStyle("-fx-background-color: #f3f4f6; " +
                "-fx-background-radius: 4; " +
                "-fx-border-color: #d1d5db; " +
                "-fx-border-radius: 4; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12px; " +
                "-fx-cursor: hand;");
        btnMais.setOnAction(e -> incrementarQuantidade(produto));

        Button btnDeletar = new Button("×");
        btnDeletar.setLayoutX(225);
        btnDeletar.setLayoutY(15);
        btnDeletar.setPrefSize(25, 25);
        btnDeletar.setStyle("-fx-background-color: #fecaca; " +
                "-fx-text-fill: #dc2626; " +
                "-fx-background-radius: 4; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12px; " +
                "-fx-cursor: hand;");
        btnDeletar.setOnAction(e -> removerProduto(produto));

        itemPane.getChildren().addAll(labelNome, labelDetalhes, btnMenos, labelQuantidade, btnMais, btnDeletar);

        return itemPane;
    }

    @FXML
    private void registrarPedido() {
        if (carrinho.isEmpty()) {
            mostrarAlerta("Carrinho Vazio",
                    "Adicione produtos ao carrinho antes de registrar o pedido.",
                    Alert.AlertType.WARNING);
            return;
        }

        try {
            abrirPopupRegistroPedido();

        } catch (Exception e) {
            System.err.println("Erro ao abrir pop-up de registro: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Erro",
                    "Não foi possível abrir o formulário de registro: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void abrirPopupRegistroPedido() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaAdicionarPedidos.fxml"));

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();
            RegistroPedidoPopupController controller = loader.getController();

            controller.setCaixaService(caixaService);
            controller.setPedidoService(pedidoService);

            controller.setTelaRegistroController(this);

            controller.setDadosPedido(carrinho, totalPedido);

            Stage popupStage = new Stage();
            controller.setPopupStage(popupStage);

            popupStage.initOwner(contentPane.getScene().getWindow());

            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Registrar Pedido");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.showAndWait();

            limparCarrinho();
            atualizarInterface();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void atualizarInterface() {
        try {
            if (PdvGaleteriaApplication.getSpringContext() != null) {
                CaixaController caixaController = PdvGaleteriaApplication.getSpringContext()
                        .getBean(CaixaController.class);

                if (caixaController != null) {
                    caixaController.atualizarCaixa();
                    System.out.println("Caixa atualizado após venda");
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao atualizar caixa: " + e.getMessage());
        }
    }

    @FXML
    private void abrirTelaEstoque() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaProdutos.fxml"));

            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();

            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Estoque");
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirTelaEntregadores() {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaEntregadores.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERRO: Arquivo não encontrado!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Entregadores");
            stage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("Erro ao abrir tela de entregadores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirTelaDashboard() {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaDashBoard.fxml");

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");
            stage.centerOnScreen();

        } catch (Exception e) {
            mostrarAlerta("Funcionalidade em Desenvolvimento",
                    "Tela de dashboard será implementada em breve!",
                    Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void abrirTelaRelatorios() {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaRelatorio.fxml");
            if (fxmlUrl == null) {
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            Stage stage = (Stage) campoBusca.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Relatórios");
            stage.centerOnScreen();

        } catch (Exception e) {
        }
    }

    @FXML
    private void abrirTelaConfiguracoes() {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaConfiguracao.fxml");
            if (fxmlUrl == null) {
                mostrarAlerta("Funcionalidade em Desenvolvimento",
                        "Tela de configurações será implementada em breve!",
                        Alert.AlertType.INFORMATION);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Configurações");
            stage.centerOnScreen();

        } catch (Exception e) {
            mostrarAlerta("Funcionalidade em Desenvolvimento",
                    "Tela de configurações será implementada em breve!",
                    Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void sairParaLogin() {
        try {
            System.out.println("Abrindo pop-up de confirmação de saída...");

            if (usuarioSessao != null) {
                usuarioSessao.logout();
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaSairPrograma.fxml"));

            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();
            ConfirmacaoSaidaController controller = loader.getController();

            Stage popupStage = new Stage();
            controller.setPopupStage(popupStage);

            Stage currentStage = (Stage) campoBusca.getScene().getWindow();

            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Confirmação de Saída");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(currentStage);
            popupStage.setResizable(false);
            popupStage.centerOnScreen();

            popupStage.showAndWait();

            if (controller.isConfirmado()) {
                voltarParaTelaLogin(currentStage);
            }
        } catch (Exception e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmação de Saída");
            alert.setHeaderText("Deseja realmente sair?");
            alert.setContentText("Você será redirecionado para a tela de login.");

            Optional<ButtonType> resultado = alert.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                if (usuarioSessao != null) {
                    usuarioSessao.logout();
                }
                voltarParaTelaLogin((Stage) alert.getDialogPane().getScene().getWindow());
            }
        }
    }

    private void voltarParaTelaLogin(Stage currentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaLogin.fxml"));

            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();

            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Login");
            currentStage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reiniciarAplicacaoCompleta() {
        try {
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.close();

            PdvGaleteriaApplication.relaunchApplication();

        } catch (Exception e) {
            System.err.println("Erro ao reiniciar aplicação: " + e.getMessage());

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao reiniciar aplicação");
            alert.setContentText("Por favor, feche e abra o programa manualmente.");
            alert.showAndWait();

            Platform.exit();
        }
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML
    private void abrirTelaCaixa(ActionEvent event) {
        try {
            System.out.println("Abrindo tela do caixa...");

            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaCaixa.fxml");
            if (fxmlUrl == null) {
                mostrarAlerta("Erro", "Arquivo da tela do caixa não encontrado!", Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

                       Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Controle de Caixa");
            stage.centerOnScreen();

            System.out.println("Tela do caixa aberta com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao abrir tela do caixa: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao abrir tela do caixa: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private Stage getCurrentStage() {
        return (Stage) contentPane.getScene().getWindow();
    }

    private double calcularTotalCarrinho() {
        double total = 0.0;
        if (carrinho != null && !carrinho.isEmpty()) {
            for (Map.Entry<Produto, Integer> entry : carrinho.entrySet()) {
                Produto produto = entry.getKey();
                int quantidade = entry.getValue();
                total += produto.getPreco() * quantidade;
            }
        }
        return total;
    }

    public void limparCarrinho() {
        carrinho.clear();
        atualizarCarrinho();
    }
}