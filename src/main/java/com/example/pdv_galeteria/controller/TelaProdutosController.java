package com.example.pdv_galeteria.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.pdv_galeteria.model.Produto;
import com.example.pdv_galeteria.service.ProdutoService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

@Component
public class TelaProdutosController implements Initializable {

    @Autowired
    private ProdutoService produtoService;

    @FXML
    private Pane contentPane;

    private double initialX = 13.0;
    private double initialY = 293.0;
    private double cardWidth = 240.0;
    private double cardHeight = 178.0;
    private double horizontalGap = 20.0;
    private double verticalGap = 20.0;
    private int cardsPerRow = 3;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Inicializando TelaProdutosController...");
        carregarProdutos();
    }

    private void carregarProdutos() {
        try {
            System.out.println("Carregando produtos do banco...");
            List<Produto> produtos = produtoService.listarTodos();
            System.out.println("Produtos encontrados: " + produtos.size());
            renderizarProdutos(produtos);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao carregar produtos: " + e.getMessage());
            mostrarMensagemErro("Erro ao carregar produtos: " + e.getMessage());
        }
    }

    private void renderizarProdutos(List<Produto> produtos) {
        contentPane.getChildren()
                .removeIf(node -> node instanceof Pane && node.getStyleClass().contains("card-produtos"));

        if (produtos.isEmpty()) {
            mostrarMensagemSemProdutos();
            return;
        }

        double currentX = initialX;
        double currentY = initialY;
        int cardCount = 0;

        for (Produto produto : produtos) {
            Pane cardProduto = criarCardProduto(produto);
            cardProduto.setLayoutX(currentX);
            cardProduto.setLayoutY(currentY);

            contentPane.getChildren().add(cardProduto);

            cardCount++;
            currentX += cardWidth + horizontalGap;

            if (cardCount % cardsPerRow == 0) {
                currentX = initialX;
                currentY += cardHeight + verticalGap;
            }
        }

        System.out.println("Renderizados " + cardCount + " cards de produtos");
    }

    private Pane criarCardProduto(Produto produto) {
        Pane card = new Pane();
        card.getStyleClass().add("card-produtos");
        card.setPrefSize(cardWidth, cardHeight);
        card.setStyle(
                "-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-radius: 5; -fx-background-color: white;");

        Label labelNome = new Label(truncarNome(produto.getNome(), 15));
        labelNome.setLayoutX(14.0);
        labelNome.setLayoutY(14.0);
        labelNome.setStyle("-fx-font-weight: bold; -fx-font-size: 21px;");
        labelNome.setPrefWidth(200.0);

        Pane categoriaPane = new Pane();
        categoriaPane.setLayoutX(14.0);
        categoriaPane.setLayoutY(45.0);
        categoriaPane.setPrefSize(68.0, 23.0);
        categoriaPane.setStyle("-fx-background-color: #F3F4F6; -fx-border-color: black; -fx-border-radius: 20;");

        Label labelCategoria = new Label("Produto");
        labelCategoria.setLayoutX(9.0);
        labelCategoria.setLayoutY(3.0);
        labelCategoria.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        categoriaPane.getChildren().add(labelCategoria);

        Label labelPreco = new Label(String.format("R$ %.2f", produto.getPreco()));
        labelPreco.setLayoutX(14.0);
        labelPreco.setLayoutY(89.0);
        labelPreco.setStyle("-fx-font-weight: bold; -fx-font-size: 28px; -fx-text-fill: #2a6df4;");

        Label labelEstoque = new Label("Estoque: ");
        labelEstoque.setLayoutX(14.0);
        labelEstoque.setLayoutY(121.0);
        labelEstoque.setStyle("-fx-font-size: 12px;");

        Label labelQuantidade = new Label(produto.getQuantidade() + " Unidades");
        labelQuantidade.setLayoutX(65.0);
        labelQuantidade.setLayoutY(121.0);
        labelQuantidade.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Button btnEditar = new Button("    Editar");
        btnEditar.setLayoutX(14.0);
        btnEditar.setLayoutY(138.0);
        btnEditar.setPrefSize(82.0, 29.0);
        btnEditar.setStyle(
                "-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 3; -fx-font-weight: bold;");
        btnEditar.setCursor(javafx.scene.Cursor.HAND);

        Button btnEntrada = new Button("    Entrada");
        btnEntrada.setLayoutX(102.0);
        btnEntrada.setLayoutY(138.0);
        btnEntrada.setPrefSize(85.0, 29.0);
        btnEntrada.setStyle(
                "-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 3; -fx-font-weight: bold;");
        btnEntrada.setCursor(javafx.scene.Cursor.HAND);

        Button btnApagar = new Button("X");
        btnApagar.setLayoutX(190.0);
        btnApagar.setLayoutY(138.0);
        btnApagar.setPrefSize(30.0, 29.0);
        btnApagar.setStyle(
                "-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 3;");
        btnApagar.setCursor(javafx.scene.Cursor.HAND);

        card.getChildren().addAll(
                labelNome, categoriaPane, labelPreco, labelEstoque, labelQuantidade,
                btnEditar, btnEntrada, btnApagar);

        return card;
    }

    private String truncarNome(String nome, int maxLength) {
        if (nome != null && nome.length() > maxLength) {
            return nome.substring(0, maxLength) + "...";
        }
        return nome != null ? nome : "Sem Nome";
    }

    private void mostrarMensagemSemProdutos() {
        Label mensagem = new Label("Nenhum produto cadastrado");
        mensagem.setLayoutX(400.0);
        mensagem.setLayoutY(350.0);
        mensagem.setStyle("-fx-font-size: 18px; -fx-text-fill: #666;");
        contentPane.getChildren().add(mensagem);
    }

    private void mostrarMensagemErro(String mensagem) {
        Label erro = new Label(mensagem);
        erro.setLayoutX(14.0);
        erro.setLayoutY(250.0);
        erro.setStyle("-fx-font-size: 14px; -fx-text-fill: red;");
        contentPane.getChildren().add(erro);
    }

    @FXML
    private void abrirCadastroProduto() {
        try {
            System.out.println("Abrindo tela de cadastro de produto...");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/CadastrarProduto.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Cadastrar Produto");
            stage.setResizable(false);

            stage.show();

            System.out.println("Tela de cadastro aberta com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir tela de cadastro: " + e.getMessage());
            mostrarMensagemErro("Erro ao abrir cadastro: " + e.getMessage());
        }
    }
}