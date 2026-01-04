package com.example.pdv_galeteria.controller;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import com.example.pdv_galeteria.model.Combo;
import com.example.pdv_galeteria.model.UsuarioSessao;
import com.example.pdv_galeteria.service.CaixaService;
import com.example.pdv_galeteria.service.ComboService;
import javafx.event.ActionEvent;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import com.example.pdv_galeteria.model.Produto;
import com.example.pdv_galeteria.service.ProdutoService;
import com.example.pdv_galeteria.PdvGaleteriaApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.stage.Modality;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ContentDisplay;
import org.springframework.stereotype.Controller;
import java.util.*;
import com.example.pdv_galeteria.controller.CaixaController;


@Controller
public class TelaProdutosController implements Initializable {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private TelaCombosController telaCombosController;

    @FXML
    private Pane mainContentPane;

    private List<Produto> produtosList = new ArrayList<>();
    private Produto produtoSelecionado;

    @FXML
    private FlowPane produtosContainer;

    @FXML
    private FlowPane combosContainer;

    @FXML
    private TextField campoBusca;

    private List<Produto> todosProdutos = new ArrayList<>();
    private Timer timerBusca;

    @FXML
    private Pane contentPane;

    @FXML
    private AnchorPane comboContainerPane;

    @Autowired
    private TelaCombosController combosController;

    @Autowired
    private ComboService comboService;

    @Autowired
    private UsuarioSessao usuarioSessao;

    @FXML
    private Label labelNomeUsuario;

    private double initialX = 13.0;
    private double initialY = 293.0;
    private double cardWidth = 240.0;
    private double cardHeight = 178.0;
    private double horizontalGap = 20.0;
    private double verticalGap = 20.0;
    private int cardsPerRow = 2;

    private List<Pane> cardsOriginaisProdutos = new ArrayList<>();
    private List<Pane> cardsOriginaisCombos = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Inicializando TelaProdutosController...");
        System.out.println("MainContentPane: " + (mainContentPane != null ? "ENCONTRADO" : "NULO"));

        System.out.println("campoBusca: " + (campoBusca != null ? "INJETADO" : "NULO"));
        System.out.println("campoBusca: " + (campoBusca != null ? "INJETADO" : "NULO"));

        PdvGaleteriaApplication.debugSpringContext();
        System.out.println("ProdutoService: " + (produtoService != null ? "INJETADO" : "NULO"));

        if (labelNomeUsuario != null && usuarioSessao != null) {
            labelNomeUsuario.setText(usuarioSessao.getNomeUsuario());
        }

        timerBusca = new Timer();

        atualizarNomeUsuarioNoMenu();

        carregarProdutos();
        combosController.setCombosContainer(combosContainer);
        combosController.carregarCombos();
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

    public void carregarProdutos() {
        try {
            System.out.println("Carregando produtos em scroll horizontal...");

            produtosContainer.getChildren().clear();
            List<Produto> produtos = produtoService.listarTodos();

            System.out.println("Produtos encontrados: " + (produtos != null ? produtos.size() : 0));

            if (produtos == null || produtos.isEmpty()) {
                Label vazio = new Label("Nenhum produto cadastrado");
                vazio.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 20;");
                produtosContainer.getChildren().add(vazio);
                return;
            }

            for (Produto produto : produtos) {
                System.out.println("Criando card para produto: " + produto.getNome());
                Pane card = criarCardProduto(produto, false);
                produtosContainer.getChildren().add(card);
            }

            System.out.println("Cards de produtos criados: " + produtosContainer.getChildren().size());

        } catch (Exception e) {
            e.printStackTrace();
            produtosContainer.getChildren().clear();
            Label erro = new Label("Erro ao carregar produtos: " + e.getMessage());
            erro.setStyle("-fx-text-fill: red; -fx-padding: 10;");
            produtosContainer.getChildren().add(erro);
        }
    }

    private Pane criarCardProduto(Produto produto, boolean isCombo) {
        Pane card = new Pane();
        card.getStyleClass().add("card-produtos");
        card.setPrefSize(400, 178);
        card.setMinSize(400, 178);
        card.setMaxSize(400, 178);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 5; " +
                "-fx-border-radius: 5; " +
                "-fx-border-color: rgba(0,0,0,0.2); " +
                "-fx-border-width: 1;");

        Label labelNome = new Label(produto.getNome());
        labelNome.setLayoutX(14);
        labelNome.setLayoutY(14);
        labelNome.setStyle("-fx-font-weight: bold; -fx-font-size: 21px; -fx-text-fill: black;");

        String categoria = isCombo ? "Combo" : "Produto";
        Pane categoriaPane = new Pane();
        categoriaPane.setLayoutX(299);
        categoriaPane.setLayoutY(18);
        categoriaPane.setPrefSize(68, 23);
        categoriaPane.setStyle("-fx-background-color: #F3F4F6; " +
                "-fx-background-radius: 20; " +
                "-fx-border-radius: 20;");

        Label labelCategoria = new Label(categoria);
        labelCategoria.setLayoutX(9);
        labelCategoria.setLayoutY(3);
        labelCategoria.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #374151;");
        categoriaPane.getChildren().add(labelCategoria);

        int quantidade = produto.getQuantidade();
        String textoQuantidade = quantidade + " unidades";

        Label labelQuantidade = new Label(textoQuantidade);
        labelQuantidade.setLayoutX(14);
        labelQuantidade.setLayoutY(120);

        if (quantidade <= 10) {
            labelQuantidade.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #f68411;");
        } else {
            labelQuantidade.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #374151;");
        }

        Label labelPreco = new Label(String.format("R$ %.2f", produto.getPreco()));
        labelPreco.setLayoutX(14);
        labelPreco.setLayoutY(82);
        labelPreco.setStyle("-fx-font-weight: bold; -fx-font-size: 28px; -fx-text-fill: #2a6df4;");

        Button btnEditar = criarBotaoComIcone("Editar", 65, 144, 90, 29, "/assets/imgs/editar.png");
        Button btnSegundo = isCombo
                ? criarBotaoComIcone("Excluir", 160, 144, 95, 29, "/assets/imgs/delete.png")
                : criarBotaoComIcone("Entrada", 160, 144, 95, 29, "/assets/imgs/plus-square.png");
        Button btnApagar = criarBotaoIcone(269, 144, 30, 29, "/assets/imgs/delete.png");

        btnEditar.setOnAction(e -> {
            produtoSelecionado = produto;
            editarProduto();
        });

        btnSegundo.setOnAction(e -> {
            if (isCombo) {
                System.out.println("Clicou para excluir combo: " + produto.getNome());
            } else {
                System.out.println("Clicou para entrada de estoque: " + produto.getNome());
            }
        });

        btnApagar.setOnAction(e -> {
            System.out.println("Clicou para apagar produto: " + produto.getNome() + " (ID: " + produto.getId() + ")");
            executarExclusaoProduto(produto);
        });

        card.getChildren().addAll(
                labelNome, categoriaPane, labelPreco,
                labelQuantidade,
                btnEditar, btnSegundo, btnApagar
        );

        return card;
    }

    private Button criarBotaoComIcone(String texto, double x, double y, double width, double height, String iconePath) {
        Button botao = new Button(texto);
        botao.setLayoutX(x);
        botao.setLayoutY(y);
        botao.setPrefSize(width, height);
        botao.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #D1D5DB; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 6; " +
                "-fx-background-radius: 6; " +
                "-fx-text-fill: #374151; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12px;");
        try {
            ImageView icone = new ImageView(new Image(getClass().getResourceAsStream(iconePath)));
            icone.setFitWidth(16);
            icone.setFitHeight(16);
            botao.setGraphic(icone);
            botao.setContentDisplay(ContentDisplay.LEFT);
            botao.setGraphicTextGap(8);
            botao.setCursor(Cursor.HAND);
        } catch (Exception e) {
            System.out.println("Ícone não encontrado: " + iconePath);
        }

        return botao;
    }

    private Button criarBotaoIcone(double x, double y, double width, double height, String iconePath) {
        Button botao = new Button();
        botao.setLayoutX(x);
        botao.setLayoutY(y);
        botao.setPrefSize(width, height);
        botao.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #D1D5DB; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 6; " +
                "-fx-background-radius: 6;");
        botao.setCursor(Cursor.HAND);
        try {
            ImageView icone = new ImageView(new Image(getClass().getResourceAsStream(iconePath)));
            icone.setFitWidth(16);
            icone.setFitHeight(16);
            botao.setGraphic(icone);
        } catch (Exception e) {
            System.out.println("Ícone não encontrado: " + iconePath);
        }

        return botao;
    }

    private void mostrarMensagemErro(String mensagem) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText(mensagem);
            alert.showAndWait();
        });
    }

    @FXML
    private void abrirTelaCombo() {
        telaCombosController.abrirTelaCombo();
    }

    @FXML
    private void abrirCadastroProduto() {
        try {
            System.out.println("Abrindo tela de cadastro de produto...");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/CadastrarProduto.fxml"));

            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Cadastrar Produto");
            stage.setResizable(false);

            stage.setOnHidden(e -> {
                System.out.println("Janela de cadastro fechada, recarregando produtos...");
                carregarProdutos();
            });

            stage.show();
            System.out.println("Tela de cadastro aberta com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir tela de cadastro: " + e.getMessage());
            mostrarMensagemErro("Erro ao abrir cadastro: " + e.getMessage());
        }
    }

    @FXML
    private void abrirTelaVendas() {
        try {
            System.out.println("Abrindo tela de vendas...");

            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaRegistroPedido.fxml");
            if (fxmlUrl == null) {
                System.err.println("Arquivo FXML não encontrado: TelaRegistroPedido.fxml");
                mostrarMensagemErro("Arquivo da tela de vendas não encontrado!");
                return;
            }
            System.out.println("Arquivo FXML encontrado: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() == null) {
                System.err.println("Contexto do Spring não disponível");
                System.err.println("Contexto do Spring não disponível");
                try {
                    Parent root = loader.load();

                    Stage stage = (Stage) contentPane.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Registro de Pedidos");
                    stage.centerOnScreen();

                    System.out.println("Tela de vendas aberta sem Spring");
                    return;
                } catch (Exception fallbackException) {
                    fallbackException.printStackTrace();
                    mostrarMensagemErro("Erro ao abrir tela: " + fallbackException.getMessage());
                    return;
                }
            }

            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();

            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Registro de Pedidos");
            stage.centerOnScreen();

            System.out.println("Tela de vendas aberta com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir tela de vendas: " + e.getMessage());

            String errorDetails = "Erro ao abrir tela de vendas:\n";
            if (e.getCause() != null) {
                errorDetails += "Causa: " + e.getCause().getMessage();
            } else {
                errorDetails += e.getMessage();
            }
            mostrarMensagemErro(errorDetails);
        }
    }

    @FXML
    private void sairParaLogin() {
        try {
            System.out.println("Abrindo pop-up de confirmação de saída...");

            if (usuarioSessao != null) {
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
            System.out.println("Tentando reiniciar aplicação completamente...");

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

    private void executarExclusaoProduto(Produto produto) {
        try {
            System.out.println("Iniciando desativação do produto: " + produto.getNome() + " (ID: " + produto.getId() + ")");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/PopupExclusaoConfirmacao.fxml"));

            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();

            ConfirmacaoExclusaoController popupController = loader.getController();

            popupController.setProduto(produto);

            popupController.setOnConfirmacaoListener((confirmado) -> {
                if (confirmado) {
                    try {
                        produtoService.desativarProduto(produto.getId());
                        System.out.println("Produto desativado com sucesso");
                        carregarProdutos();

                    } catch (Exception e) {
                        System.err.println("Erro ao desativar produto: " + e.getMessage());
                        e.printStackTrace();
                        mostrarMensagemErro("Erro ao desativar produto: " + e.getMessage());
                    }
                } else {
                    System.out.println("Desativação cancelada pelo usuário");
                }
            });

            Stage popupStage = new Stage();
            popupStage.setTitle("Confirmar Exclusão");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(root));
            popupStage.setResizable(false);

            popupController.setPopupStage(popupStage);

            if (contentPane.getScene() != null && contentPane.getScene().getWindow() != null) {
                popupStage.initOwner(contentPane.getScene().getWindow());
            }

            popupStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erro ao carregar popup de confirmação: " + e.getMessage());
            e.printStackTrace();

            mostrarConfirmacaoFallback(produto);
        } catch (Exception e) {
            System.err.println("ERRO ao desativar produto: " + e.getMessage());
            e.printStackTrace();

            Platform.runLater(() -> {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erro");
                errorAlert.setHeaderText("Não foi possível desativar o produto");
                errorAlert.setContentText("Erro: " + e.getMessage() +
                        "\n\nTente novamente ou contate o suporte.");
                errorAlert.showAndWait();
            });
        }
    }

    private void mostrarConfirmacaoFallback(Produto produto) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Desativar Produto");
        confirmAlert.setHeaderText("Desativar '" + produto.getNome() + "'?");
        confirmAlert.setContentText(
                "Este produto está sendo usado em combos!\n\n" +
                        "Ao desativar:\n" +
                        "• Estoque será zerado (0 unidades)\n" +
                        "• Produto sairá da lista de produtos\n" +
                        "• Continuará disponível nos combos existentes\n\n" +
                        "Deseja continuar?"
        );

        confirmAlert.getButtonTypes().setAll(
                new ButtonType("Sim, Desativar", ButtonBar.ButtonData.YES),
                new ButtonType("Cancelar", ButtonBar.ButtonData.NO)
        );

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.YES) {
            produtoService.desativarProduto(produto.getId());
            System.out.println("Produto desativado com sucesso");
            carregarProdutos();

            Platform.runLater(() -> {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Produto Desativado");
                successAlert.setHeaderText(null);
                successAlert.setContentText(
                        "Produto '" + produto.getNome() + "' desativado!\n\n" +
                                "Status: Desativado\n" +
                                "Estoque: 0 unidades\n" +
                                "Disponível em combos: Sim"
                );
                successAlert.showAndWait();
            });
        }
    }

    @FXML
    private void editarProduto() {
        try {
            System.out.println("Abrindo tela de edição para: " + produtoSelecionado.getNome());

            if (produtoSelecionado == null) {
                mostrarMensagemErro("Nenhum produto selecionado para edição.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaEditarProdutos.fxml"));

            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();

            ProdutoController produtoController = loader.getController();

            produtoController.setProdutoParaEdicao(produtoSelecionado);
            produtoController.setOnEdicaoConcluidaCallback(() -> {
                System.out.println("Edição concluída, atualizando lista...");
                carregarProdutos();
            });

            Stage popupStage = new Stage();
            popupStage.setTitle("Editar Produto");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(root));
            popupStage.setResizable(false);

            if (contentPane.getScene() != null) {
                popupStage.initOwner(contentPane.getScene().getWindow());
            }

            popupStage.showAndWait();

            System.out.println("Popup de edição fechado");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir tela de edição: " + e.getMessage());
            mostrarMensagemErro("Erro ao abrir tela de edição: " + e.getMessage());
        }
    }

    @FXML
    private void onBuscaKeyReleased() {
        String termoBusca = campoBusca.getText().trim();
        System.out.println("onBuscaKeyReleased CHAMADO! Texto: '" + termoBusca + "'");

        if (timerBusca != null) {
            timerBusca.cancel();
            timerBusca.purge();
        }

        if (termoBusca.isEmpty()) {
            System.out.println("Campo VAZIO - restaurando tela original");
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
                Platform.runLater(() -> {
                    System.out.println("Executando busca após delay para: '" + termoBusca + "'");
                    executarBusca(termoBusca);
                });
            }
        }, 300);
    }

    private void executarBusca(String termoBusca) {
        try {
            System.out.println("Buscando produtos E combos por: '" + termoBusca + "'");

            List<Produto> produtosEncontrados = produtoService.buscarListaPorNome(termoBusca);

            List<Combo> combosEncontrados = comboService.buscarCombosPorNome(termoBusca);

            System.out.println(produtosEncontrados.size() + " produtos + " +
                    combosEncontrados.size() + " combos encontrados");

            renderizarProdutosECombos(produtosEncontrados, combosEncontrados);

        } catch (Exception e) {
            System.err.println("Erro na busca: " + e.getMessage());
            e.printStackTrace();

            Platform.runLater(() -> {
                mostrarMensagemErro("Erro ao buscar itens: " + e.getMessage());
            });

            try {
                List<Produto> todosProdutos = produtoService.listarTodos();
                List<Combo> todosCombos = comboService.buscarTodosCombos();
                renderizarProdutosECombos(todosProdutos, todosCombos);
            } catch (Exception ex) {
                System.err.println("Erro ao restaurar itens após falha: " + ex.getMessage());
            }
        }
    }

    private void renderizarProdutosECombos(List<Produto> produtos, List<Combo> combos) {
        Platform.runLater(() -> {
            try {
                System.out.println("Renderizando resultados da busca");

                if (produtosContainer != null) {
                    produtosContainer.setVisible(false);
                    produtosContainer.setManaged(false);
                }
                if (comboContainerPane != null) {
                    comboContainerPane.setVisible(false);
                    comboContainerPane.setManaged(false);
                }
                if (combosContainer != null) {
                    combosContainer.setVisible(false);
                    combosContainer.setManaged(false);
                }

                Pane container = mainContentPane != null ? mainContentPane : contentPane;
                if (container != null) {
                    container.getChildren().removeIf(node ->
                            node instanceof Pane &&
                                    node.getId() != null &&
                                    node.getId().startsWith("card-busca-")
                    );
                }

                if (produtos.isEmpty() && combos.isEmpty()) {
                    mostrarMensagemSemProdutos();
                    return;
                }

                double startX = 315.0;
                double startY = 240.0;
                double cardWidth = 400.0;
                double cardHeight = 178.0;
                double horizontalGap = 59.0;
                double verticalGap = 26.0;

                if (!produtos.isEmpty()) {
                    System.out.println("Renderizando " + produtos.size() + " produtos da busca");

                    int count = 0;
                    for (Produto produto : produtos) {
                        double currentX = (count % 2 == 0) ? startX : startX + cardWidth + horizontalGap;
                        double currentY = startY + ((count / 2) * (cardHeight + verticalGap));

                        Pane cardProduto = criarCardProduto(produto, false);
                        cardProduto.setLayoutX(currentX);
                        cardProduto.setLayoutY(currentY);
                        cardProduto.setId("card-busca-produto-" + produto.getId());

                        if (container != null) {
                            container.getChildren().add(cardProduto);
                        }
                        count++;
                    }
                }

                if (!combos.isEmpty()) {
                    System.out.println("Renderizando " + combos.size() + " combos da busca");

                    double combosStartY = startY + (Math.ceil(produtos.size() / 2.0) * (cardHeight + verticalGap)) + 260;

                    double comboCardWidth = 400.0;

                    double comboXOffset = (comboCardWidth - cardWidth) / 2;

                    int comboCount = 0;
                    for (Combo combo : combos) {
                        double baseX = (comboCount % 2 == 0) ? startX : startX + cardWidth + horizontalGap;
                        double currentX = baseX - comboXOffset;
                        double currentY = combosStartY + ((comboCount / 2) * (cardHeight + verticalGap));

                        Pane cardCombo = telaCombosController.criarCardCombo(combo);
                        cardCombo.setLayoutX(currentX);
                        cardCombo.setLayoutY(currentY);
                        cardCombo.setId("card-busca-combo-" + combo.getId());

                        if (container != null) {
                            container.getChildren().add(cardCombo);
                        }
                        comboCount++;
                    }
                }

                System.out.println("Busca renderizada - " +
                        produtos.size() + " produtos + " + combos.size() + " combos");

            } catch (Exception e) {
                System.err.println("Erro ao renderizar busca: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void restaurarTelaOriginal() {
        Platform.runLater(() -> {
            try {
                System.out.println("Restaurando tela original...");

                if (produtosContainer != null) {
                    produtosContainer.setVisible(true);
                    produtosContainer.setManaged(true);
                }
                if (comboContainerPane != null) {
                    comboContainerPane.setVisible(true);
                    comboContainerPane.setManaged(true);
                }
                if (combosContainer != null) {
                    combosContainer.setVisible(true);
                    combosContainer.setManaged(true);
                }

                Pane container = mainContentPane != null ? mainContentPane : contentPane;
                if (container != null) {
                    container.getChildren().removeIf(node ->
                            node instanceof Pane &&
                                    node.getId() != null &&
                                    node.getId().startsWith("card-busca-")
                    );
                }

                carregarProdutos();
                if (combosController != null) {
                    combosController.carregarCombos();
                }

                System.out.println("Tela original restaurada com sucesso!");

            } catch (Exception e) {
                System.err.println("Erro ao restaurar tela original: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void mostrarMensagemSemProdutos() {
        Platform.runLater(() -> {
            try {
                if (produtosContainer != null) {
                    produtosContainer.setVisible(false);
                    produtosContainer.setManaged(false);
                }
                if (comboContainerPane != null) {
                    comboContainerPane.setVisible(false);
                    comboContainerPane.setManaged(false);
                }
                if (combosContainer != null) {
                    combosContainer.setVisible(false);
                    combosContainer.setManaged(false);
                }

                Pane container = mainContentPane != null ? mainContentPane : contentPane;
                if (container != null) {
                    container.getChildren().removeIf(node ->
                            node instanceof Pane &&
                                    node.getId() != null &&
                                    node.getId().startsWith("card-busca-")
                    );

                    Label mensagem = new Label("Nenhum produto ou combo encontrado para: '" + campoBusca.getText() + "'");
                    mensagem.setStyle("-fx-text-fill: #666; -fx-font-size: 14px; -fx-padding: 20px;");
                    mensagem.setLayoutX(400);
                    mensagem.setLayoutY(300);
                    container.getChildren().add(mensagem);
                }

            } catch (Exception e) {
                System.err.println("Erro ao mostrar mensagem sem produtos: " + e.getMessage());
            }
        });
    }

    @FXML
    private void abrirTelaCaixa(ActionEvent event) {
        try {
            System.out.println("Abrindo tela do caixa...");

            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaCaixa.fxml");

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
        }
    }

    @FXML
    private void abrirTelaEntregadores() {
        try {
            System.out.println("=== ABRINDO TELA ENTREGADORES ===");

            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaEntregadores.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERRO: Arquivo não encontrado!");

                mostrarAlerta("Erro", "Tela de entregadores não disponível no momento.");
                return;
            }

            System.out.println("FXML encontrado: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
                System.out.println("Spring configurado");
            }

            System.out.println("Carregando FXML...");
            Parent root = loader.load();
            System.out.println("FXML carregado com sucesso!");

            Stage stage = null;


            if (campoBusca != null && campoBusca.getScene() != null) {
                stage = (Stage) campoBusca.getScene().getWindow();
            }

            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle("Entregadores");
                stage.centerOnScreen();
                System.out.println("Tela de entregadores aberta com SUCESSO!");
            }

        } catch (Exception e) {
            System.err.println("=== ERRO AO ABRIR TELA ENTREGADORES ===");
            System.err.println("Mensagem: " + e.getMessage());
            e.printStackTrace();

            mostrarAlerta("Erro", "Não foi possível abrir a tela de entregadores: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
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

            System.err.println("Nenhum stage encontrado, criando novo...");
            return new Stage();

        } catch (Exception e) {
            System.err.println("Erro ao obter stage atual: " + e.getMessage());
            return new Stage();
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

            Stage stage = (Stage) produtosContainer.getScene().getWindow();
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
        }
    }

    @FXML
    private void abrirTelaDashboard() {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaDashBoard.fxml");
            if (fxmlUrl == null) {
                mostrarErro("Arquivo da tela de dashboard não encontrado!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");
            stage.centerOnScreen();
        } catch (Exception e) {
            mostrarErro("Erro ao abrir tela de dashboard: " + e.getMessage());
        }
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}