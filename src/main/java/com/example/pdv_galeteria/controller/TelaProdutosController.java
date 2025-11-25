package com.example.pdv_galeteria.controller;
import java.net.URL;
import java.util.*;

import com.example.pdv_galeteria.model.Combo;
import com.example.pdv_galeteria.service.ComboService;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
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
import java.util.*;

@Component
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
    private TelaCombosController combosController; // Injeta o controller de combos

    @Autowired
    private ComboService comboService;

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

        // VERIFICAR SE CAMPO DE BUSCA FOI INJETADO
        System.out.println("🔍 campoBusca: " + (campoBusca != null ? "✅ INJETADO" : "❌ NULO"));

        // DEBUG: Verificar caminho e testar popup
        verificarCaminhoFXML();
        testePopupBasico();

        // Debug do Spring Context
        PdvGaleteriaApplication.debugSpringContext();
        System.out.println("✅ ProdutoService: " + (produtoService != null ? "INJETADO" : "NULO"));

        // Inicializar timer de busca
        timerBusca = new Timer();

        // Carregar produtos
        carregarProdutos();
        carregarTelaCombos();
        combosController.setCombosContainer(combosContainer);
        combosController.carregarCombos();
    }

    public void carregarProdutos() {
        try {
            System.out.println("🔄 Carregando produtos em scroll horizontal...");

            produtosContainer.getChildren().clear();
            List<Produto> produtos = produtoService.listarTodos();

            System.out.println("📊 Produtos encontrados: " + (produtos != null ? produtos.size() : 0));

            if (produtos == null || produtos.isEmpty()) {
                Label vazio = new Label("Nenhum produto cadastrado");
                vazio.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 20;");
                produtosContainer.getChildren().add(vazio);
                return;
            }

            for (Produto produto : produtos) {
                System.out.println("🎴 Criando card para produto: " + produto.getNome());
                Pane card = criarCardProduto(produto, false); // ← false para produtos normais
                produtosContainer.getChildren().add(card);
            }

            System.out.println("✅ Cards de produtos criados: " + produtosContainer.getChildren().size());

        } catch (Exception e) {
            e.printStackTrace();
            produtosContainer.getChildren().clear();
            Label erro = new Label("Erro ao carregar produtos: " + e.getMessage());
            erro.setStyle("-fx-text-fill: red; -fx-padding: 10;");
            produtosContainer.getChildren().add(erro);
        }
    }


    private void carregarTelaCombos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pdv_galeteria/Frontend/views/Teladisplaycombo.fxml"));
            loader.setControllerFactory(context::getBean);
            Pane combosPane = loader.load();
            comboContainerPane.getChildren().setAll(combosPane);
            AnchorPane.setTopAnchor(combosPane, 0.0);
            AnchorPane.setLeftAnchor(combosPane, 0.0);
            AnchorPane.setRightAnchor(combosPane, 0.0);
            AnchorPane.setBottomAnchor(combosPane, 0.0);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao carregar a sub-tela de combos: " + e.getMessage());
        }
    }

    private void renderizarProdutos(List<Produto> produtos) {
        // Limpar todos os elementos do mainContentPane
        if (mainContentPane != null) {
            mainContentPane.getChildren().clear();
        } else {
            System.err.println("❌ mainContentPane é nulo! Usando contentPane como fallback.");
            contentPane.getChildren().removeIf(node -> node instanceof Pane && node.getStyleClass().contains("card-produtos"));
        }

        if (produtos.isEmpty()) {
            mostrarMensagemSemProdutos();
            return;
        }

        // Separar produtos e combos
        List<Produto> produtosNormais = new ArrayList<>();
        List<Produto> combos = new ArrayList<>();

        for (Produto produto : produtos) {
            if (produto.getNome() != null && produto.getNome().toLowerCase().contains("combo")) {
                combos.add(produto);
            } else {
                produtosNormais.add(produto);
            }
        }

        double startX = 315.0;
        double startY = 240.0;
        double cardWidth = 400.0;
        double cardHeight = 178.0;
        double horizontalGap = 59.0;
        double verticalGap = 26.0;

        // Renderizar PRODUTOS NORMAIS
        if (!produtosNormais.isEmpty()) {


            int count = 0;
            for (Produto produto : produtosNormais) {
                double currentX = (count % 2 == 0) ? startX : startX + cardWidth + horizontalGap;
                double currentY = startY + ((count / 2) * (cardHeight + verticalGap));

                Pane cardProduto = criarCardProduto(produto, false);
                cardProduto.setLayoutX(currentX);
                cardProduto.setLayoutY(currentY);

                if (mainContentPane != null) {
                    mainContentPane.getChildren().add(cardProduto);
                } else {
                    contentPane.getChildren().add(cardProduto);
                }
                count++;
            }
        }

        // Renderizar COMBOS
        if (!combos.isEmpty()) {
            // Calcular posição Y para combos
            double combosStartY = startY + (Math.ceil(produtosNormais.size() / 2.0) * (cardHeight + verticalGap)) + 50;


            int comboCount = 0;
            for (Produto combo : combos) {
                double currentX = (comboCount % 2 == 0) ? startX : startX + cardWidth + horizontalGap;
                double currentY = combosStartY + ((comboCount / 2) * (cardHeight + verticalGap));

                Pane cardCombo = criarCardProduto(combo, true);
                cardCombo.setLayoutX(currentX);
                cardCombo.setLayoutY(currentY);

                if (mainContentPane != null) {
                    mainContentPane.getChildren().add(cardCombo);
                } else {
                    contentPane.getChildren().add(cardCombo);
                }
                comboCount++;
            }
        }
    }

    private Pane criarCardProduto(Produto produto, boolean isCombo) {
        Pane card = new Pane();
        card.getStyleClass().add("card-produtos");
        card.setPrefSize(400, 178); // TAMANHO FIXO
        card.setMinSize(400, 178);
        card.setMaxSize(400, 178);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 5; " +
                "-fx-border-radius: 5; " +
                "-fx-border-color: rgba(0,0,0,0.2); " +
                "-fx-border-width: 1;");

        // Nome do produto
        Label labelNome = new Label(produto.getNome());
        labelNome.setLayoutX(14);
        labelNome.setLayoutY(14);
        labelNome.setStyle("-fx-font-weight: bold; -fx-font-size: 21px; -fx-text-fill: black;");

        // Categoria
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

        // Preço
        Label labelPreco = new Label(String.format("R$ %.2f", produto.getPreco()));
        labelPreco.setLayoutX(14);
        labelPreco.setLayoutY(82);
        labelPreco.setStyle("-fx-font-weight: bold; -fx-font-size: 28px; -fx-text-fill: #2a6df4;");

        // BOTÕES
        Button btnEditar = criarBotaoComIcone("Editar", 65, 144, 90, 29, "/assets/imgs/editar.png");
        Button btnSegundo = isCombo
                ? criarBotaoComIcone("Excluir", 160, 144, 95, 29, "/assets/imgs/delete.png")
                : criarBotaoComIcone("Entrada", 160, 144, 95, 29, "/assets/imgs/plus-square.png");
        Button btnApagar = criarBotaoIcone(269, 144, 30, 29, "/assets/imgs/delete.png");

        // Ações dos botões
        btnEditar.setOnAction(e -> {
            produtoSelecionado = produto;
            editarProduto();
        });

        btnSegundo.setOnAction(e -> {
            if (isCombo) {
                System.out.println("🗑️ Clicou para excluir combo: " + produto.getNome());
            } else {
                System.out.println("📥 Clicou para entrada de estoque: " + produto.getNome());
            }
        });

        // Adicionar ação ao botão de apagar
        btnApagar.setOnAction(e -> {
            System.out.println("🗑️ Clicou para apagar produto: " + produto.getNome() + " (ID: " + produto.getId() + ")");
            executarExclusaoProduto(produto); // ← Usando o método que já existe
        });

        card.getChildren().addAll(
                labelNome, categoriaPane, labelPreco,
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

            // Usar o ControllerFactory do Spring (agora estático)
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
            System.out.println("✅ Tela de cadastro aberta com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erro ao abrir tela de cadastro: " + e.getMessage());
            mostrarMensagemErro("Erro ao abrir cadastro: " + e.getMessage());
        }
    }

    @FXML
    private void abrirTelaVendas() {
        try {
            System.out.println("Abrindo tela de vendas...");

            // PRIMEIRO: Verificar se o arquivo FXML existe
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaRegistroPedido.fxml");
            if (fxmlUrl == null) {
                System.err.println("❌ Arquivo FXML não encontrado: TelaRegistroPedido.fxml");
                mostrarMensagemErro("Arquivo da tela de vendas não encontrado!");
                return;
            }
            System.out.println("✅ Arquivo FXML encontrado: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            // Verificar se o contexto do Spring está disponível
            if (PdvGaleteriaApplication.getSpringContext() == null) {
                System.err.println("❌ Contexto do Spring não disponível");
                // Tentar carregar sem o Spring como fallback
                try {
                    Parent root = loader.load();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Registro de Pedidos");
                    stage.show();
                    System.out.println("✅ Tela de vendas aberta sem Spring");
                    return;
                } catch (Exception fallbackException) {
                    fallbackException.printStackTrace();
                    mostrarMensagemErro("Erro ao abrir tela: " + fallbackException.getMessage());
                    return;
                }
            }

            // Usar o ControllerFactory do Spring
            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();

            // Criar nova stage em vez de reutilizar a atual
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Registro de Pedidos");
            stage.setMaximized(true);

            stage.show();
            System.out.println("✅ Tela de vendas aberta com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erro ao abrir tela de vendas: " + e.getMessage());

            // Mostrar detalhes mais específicos do erro
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

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaSairPrograma.fxml"));

            Parent root = loader.load();
            ConfirmacaoSaidaController controller = loader.getController();

            Stage popupStage = new Stage();
            controller.setPopupStage(popupStage);

            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Confirmação de Saída");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(contentPane.getScene().getWindow());
            popupStage.setResizable(false);
            popupStage.centerOnScreen();

            popupStage.showAndWait();

            if (controller.isConfirmado()) {
                System.out.println("Usuário confirmou saída, voltando para login...");
                voltarParaTelaLogin();
            } else {
                System.out.println("Usuário cancelou a saída.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir pop-up de confirmação: " + e.getMessage());

            // Fallback com alerta
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmação de Saída");
            alert.setHeaderText("Deseja realmente sair?");
            alert.setContentText("Você será redirecionado para a tela de login.");

            Optional<ButtonType> resultado = alert.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                voltarParaTelaLogin();
            }
        }
    }

    private void voltarParaTelaLogin() {
        try {
            System.out.println("Iniciando processo de volta para login...");

            Stage stage = (Stage) contentPane.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaLogin.fxml"));

            // Usar o ControllerFactory do Spring para garantir injeção de dependências
            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();

            System.out.println("✅ Tela de login carregada com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erro ao voltar para login: " + e.getMessage());

            // Tentar reiniciar a aplicação completamente
            reiniciarAplicacaoCompleta();
        }
    }

    private void reiniciarAplicacaoCompleta() {
        try {
            System.out.println("Tentando reiniciar aplicação completamente...");

            // Fechar a janela atual
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.close();

            // Usar o método de reinício da aplicação principal
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

    private void testePopupBasico() {
        System.out.println("🎯 TESTE BÁSICO DO POPUP...");
        try {
            // Método mais simples possível
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/pdv_galeteria/view/popupExclusaoConfirmacao.fxml"));

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("TESTE - Popup Básico");
            stage.show();

            System.out.println("✅ POPUP BÁSICO ABERTO COM SUCESSO!");
        } catch (Exception e) {
            System.err.println("❌ FALHA NO POPUP BÁSICO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void verificarCaminhoFXML() {
        System.out.println("📍 VERIFICANDO CAMINHO DO FXML...");
        String[] caminhos = {
                "/com/example/pdv_galeteria/view/popupExclusaoConfirmacao.fxml",
                "/view/popupExclusaoConfirmacao.fxml",
                "popupExclusaoConfirmacao.fxml"
        };

        for (String caminho : caminhos) {
            try {
                URL url = getClass().getResource(caminho);
                System.out.println("🔍 " + caminho + " → " + (url != null ? "✅ ENCONTRADO" : "❌ NÃO ENCONTRADO"));
                if (url != null) {
                    System.out.println("   📂 " + url);
                }
            } catch (Exception e) {
                System.out.println("🔍 " + caminho + " → ❌ ERRO: " + e.getMessage());
            }
        }
    }

    private void mostrarMensagemSucesso(String mensagem) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void executarExclusaoProduto(Produto produto) {
        try {
            System.out.println("🗑️ EXECUTANDO EXCLUSÃO: " + produto.getNome() + " (ID: " + produto.getId() + ")");
            if (produtoService == null) {
                System.err.println("❌ produtoService é nulo!");
                mostrarMensagemErro("Erro: Serviço não disponível");
                return;
            }

            // Executar a exclusão
            produtoService.deletar(produto.getId());
            System.out.println("✅ Produto excluído do banco com sucesso");

            // Recarregar a lista de produtos
            carregarProdutos();

            // Mostrar mensagem de sucesso
            mostrarMensagemSucesso("Produto '" + produto.getNome() + "' excluído com sucesso!");
        } catch (Exception e) {
            System.err.println("❌ ERRO na exclusão: " + e.getMessage());
            e.printStackTrace();
            mostrarMensagemErro("Erro ao excluir produto: " + e.getMessage());
        }
    }

    @FXML
    private void editarProduto() {
        try {
            System.out.println("✏️ Abrindo tela de edição para: " + produtoSelecionado.getNome());

            if (produtoSelecionado == null) {
                mostrarMensagemErro("Nenhum produto selecionado para edição.");
                return;
            }

            // Carregar o FXML específico de edição
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaEditarProdutos.fxml");
            if (fxmlUrl == null) {
                System.err.println("❌ Arquivo FXML não encontrado: TelaEditarProdutos.fxml");
                mostrarMensagemErro("Arquivo de edição não encontrado!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();

            // Obter o controller
            ProdutoController produtoController = loader.getController();

            // Configurar para modo edição
            produtoController.setProdutoParaEdicao(produtoSelecionado);
            produtoController.setOnEdicaoConcluidaCallback(() -> {
                // Callback quando a edição for concluída
                System.out.println("✅ Edição concluída, atualizando lista...");
                carregarProdutos(); // Recarrega a lista
            });

            // Criar e mostrar o popup
            Stage popupStage = new Stage();
            popupStage.setTitle("Editar Produto");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(root));
            popupStage.setResizable(false);

            if (contentPane.getScene() != null) {
                popupStage.initOwner(contentPane.getScene().getWindow());
            }

            popupStage.showAndWait();

            System.out.println("✅ Popup de edição fechado");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erro ao abrir tela de edição: " + e.getMessage());
            mostrarMensagemErro("Erro ao abrir tela de edição: " + e.getMessage());
        }
    }

    // Método chamado quando digitar no campo de busca
    @FXML
    private void onBuscaKeyReleased() {
        String termoBusca = campoBusca.getText().trim();
        System.out.println("🎯 onBuscaKeyReleased CHAMADO! Texto: '" + termoBusca + "'");

        // Cancelar timer anterior se existir
        if (timerBusca != null) {
            timerBusca.cancel();
            timerBusca.purge();
        }

        // Se campo estiver vazio, restaurar tela original
        if (termoBusca.isEmpty()) {
            System.out.println("🔍 Campo VAZIO - restaurando tela original");
            timerBusca = new Timer();
            timerBusca.schedule(new TimerTask() {
                @Override
                public void run() {
                    restaurarTelaOriginal();
                }
            }, 100);
            return;
        }

        // Busca com delay para não sobrecarregar
        timerBusca = new Timer();
        timerBusca.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    System.out.println("🚀 Executando busca após delay para: '" + termoBusca + "'");
                    executarBusca(termoBusca);
                });
            }
        }, 300);
    }

    // Método que executa a busca
    private void executarBusca(String termoBusca) {
        try {
            System.out.println("🔍 Buscando produtos E combos por: '" + termoBusca + "'");

            // Buscar produtos normais
            List<Produto> produtosEncontrados = produtoService.buscarListaPorNome(termoBusca);

            // Buscar combos
            List<Combo> combosEncontrados = comboService.buscarCombosPorNome(termoBusca);

            System.out.println("✅ " + produtosEncontrados.size() + " produtos + " +
                    combosEncontrados.size() + " combos encontrados");

            // Renderizar produtos e combos juntos
            renderizarProdutosECombos(produtosEncontrados, combosEncontrados);

        } catch (Exception e) {
            System.err.println("❌ Erro na busca: " + e.getMessage());
            e.printStackTrace();

            Platform.runLater(() -> {
                mostrarMensagemErro("Erro ao buscar itens: " + e.getMessage());
            });

            // Em caso de erro, restaurar todos os itens
            try {
                List<Produto> todosProdutos = produtoService.listarTodos();
                List<Combo> todosCombos = comboService.buscarTodosCombos();
                renderizarProdutosECombos(todosProdutos, todosCombos);
            } catch (Exception ex) {
                System.err.println("❌ Erro ao restaurar itens após falha: " + ex.getMessage());
            }
        }
    }

    private void renderizarProdutosECombos(List<Produto> produtos, List<Combo> combos) {
        Platform.runLater(() -> {
            try {
                System.out.println("🔍 Renderizando resultados da busca");

                // Esconder os containers originais
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

                // Limpar cards de busca anteriores da área principal
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

                // USAR A MESMA LÓGICA DO renderizarProdutos
                double startX = 315.0;
                double startY = 240.0;
                double cardWidth = 400.0;
                double cardHeight = 178.0;
                double horizontalGap = 59.0;
                double verticalGap = 26.0;

                // Renderizar PRODUTOS DA BUSCA
                if (!produtos.isEmpty()) {
                    System.out.println("🎨 Renderizando " + produtos.size() + " produtos da busca");

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

                // Renderizar COMBOS DA BUSCA (usando a mesma lógica de posicionamento)
                if (!combos.isEmpty()) {
                    System.out.println("🎨 Renderizando " + combos.size() + " combos da busca");

                    double combosStartY = startY + (Math.ceil(produtos.size() / 2.0) * (cardHeight + verticalGap)) + 260;

                    // Largura do card de combo (do seu método criarCardCombo)
                    double comboCardWidth = 400.0;

                    // Ajuste para centralizar os combos (já que são mais largos)
                    double comboXOffset = (comboCardWidth - cardWidth) / 2;

                    int comboCount = 0;
                    for (Combo combo : combos) {
                        double baseX = (comboCount % 2 == 0) ? startX : startX + cardWidth + horizontalGap;
                        double currentX = baseX - comboXOffset; // Centralizar
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

                System.out.println("✅ Busca renderizada - " +
                        produtos.size() + " produtos + " + combos.size() + " combos");

            } catch (Exception e) {
                System.err.println("❌ Erro ao renderizar busca: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void restaurarTelaOriginal() {
        Platform.runLater(() -> {
            try {
                System.out.println("🔄 Restaurando tela original...");

                // Mostrar os containers originais
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

                // Limpar todos os cards de busca da área principal
                Pane container = mainContentPane != null ? mainContentPane : contentPane;
                if (container != null) {
                    container.getChildren().removeIf(node ->
                            node instanceof Pane &&
                                    node.getId() != null &&
                                    node.getId().startsWith("card-busca-")
                    );
                }

                // Recarregar os dados se necessário
                carregarProdutos();
                if (combosController != null) {
                    combosController.carregarCombos();
                }

                System.out.println("✅ Tela original restaurada com sucesso!");

            } catch (Exception e) {
                System.err.println("❌ Erro ao restaurar tela original: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void mostrarMensagemSemProdutos() {
        Platform.runLater(() -> {
            try {
                // Esconder containers originais
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

                // Limpar cards de busca anteriores
                Pane container = mainContentPane != null ? mainContentPane : contentPane;
                if (container != null) {
                    container.getChildren().removeIf(node ->
                            node instanceof Pane &&
                                    node.getId() != null &&
                                    node.getId().startsWith("card-busca-")
                    );

                    // Mostrar mensagem
                    Label mensagem = new Label("Nenhum produto ou combo encontrado para: '" + campoBusca.getText() + "'");
                    mensagem.setStyle("-fx-text-fill: #666; -fx-font-size: 14px; -fx-padding: 20px;");
                    mensagem.setLayoutX(400);
                    mensagem.setLayoutY(300);
                    container.getChildren().add(mensagem);
                }

            } catch (Exception e) {
                System.err.println("❌ Erro ao mostrar mensagem sem produtos: " + e.getMessage());
            }
        });
    }

}