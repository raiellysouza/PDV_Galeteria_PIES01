package com.example.pdv_galeteria.Frontend.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InicialController {

    @FXML
    private VBox vboxItensCarrinho;
    @FXML
    private Label labelTotalCarrinho;
    @FXML
    private Label labelContadorItens;

    private List<ProdutoCarrinho> carrinho = new ArrayList<>();
    private double total = 0.0;
    private int proximoId = 1;

    // Classe interna para representar produtos no carrinho
    private static class ProdutoCarrinho {
        String nome;
        double preco;
        int id;
        int quantidade;

        ProdutoCarrinho(String nome, double preco, int id) {
            this.nome = nome;
            this.preco = preco;
            this.id = id;
            this.quantidade = 1;
        }
    }

    @FXML
    public void initialize() {
        atualizarCarrinho();
    }

    // Métodos para adicionar produtos
    @FXML
    private void adicionarCoxaFrango(MouseEvent event) {
        adicionarAoCarrinho("Coxa de Frango", 12.00);
    }

    @FXML
    private void adicionarCocaCola(MouseEvent event) {
        adicionarAoCarrinho("Coca-Cola 2L", 8.00);
    }

    @FXML
    private void adicionarEspetinho(MouseEvent event) {
        adicionarAoCarrinho("Espetinho", 6.00);
    }

    @FXML
    private void adicionarLinguica(MouseEvent event) {
        adicionarAoCarrinho("Linguiça", 10.00);
    }

    @FXML
    private void adicionarFrangoInteiro(MouseEvent event) {
        adicionarAoCarrinho("Frango Inteiro", 25.00);
    }

    @FXML
    private void adicionarPorcaoArroz(MouseEvent event) {
        adicionarAoCarrinho("Porção de Arroz", 5.00);
    }

    private void adicionarAoCarrinho(String produto, double preco) {
        // Verifica se o produto já existe no carrinho
        ProdutoCarrinho produtoExistente = encontrarProduto(produto);

        if (produtoExistente != null) {
            // Se já existe, aumenta a quantidade
            produtoExistente.quantidade++;
            total += preco;
        } else {
            // Se não existe, adiciona novo produto
            carrinho.add(new ProdutoCarrinho(produto, preco, proximoId));
            total += preco;
            proximoId++;
        }

        atualizarCarrinho();
    }

    private ProdutoCarrinho encontrarProduto(String nome) {
        for (ProdutoCarrinho produto : carrinho) {
            if (produto.nome.equals(nome)) {
                return produto;
            }
        }
        return null;
    }

    private void atualizarCarrinho() {
        // Limpa a VBox atual
        vboxItensCarrinho.getChildren().clear();

        if (carrinho.isEmpty()) {
            Label labelVazio = new Label("Carrinho vazio\nClique nos produtos para adicionar");
            labelVazio.setStyle("-fx-text-fill: #666; -fx-font-style: italic; -fx-alignment: center;");
            labelVazio.setPadding(new Insets(40, 20, 40, 20));
            vboxItensCarrinho.getChildren().add(labelVazio);
        } else {
            for (ProdutoCarrinho produto : carrinho) {
                vboxItensCarrinho.getChildren().add(criarItemCarrinho(produto));
            }
        }

        // Atualiza totais
        labelTotalCarrinho.setText(String.format("R$ %.2f", total));
        int totalItens = carrinho.stream().mapToInt(p -> p.quantidade).sum();
        labelContadorItens.setText(totalItens + (totalItens == 1 ? " item" : " itens"));
    }

    private HBox criarItemCarrinho(ProdutoCarrinho produto) {
        HBox itemBox = new HBox();
        itemBox.setSpacing(10);
        itemBox.setPadding(new Insets(8));
        itemBox.setStyle(
                "-fx-background-color: #f8f8f8; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");
        itemBox.setPrefWidth(274); // Largura fixa para ocupar todo o espaço
        itemBox.setMaxWidth(274);

        // Label do produto - ocupa mais espaço
        String textoProduto = produto.quantidade > 1
                ? produto.nome + " (x" + produto.quantidade + ")\nR$ " + String.format("%.2f", produto.preco) + " cada"
                : produto.nome + "\nR$ " + String.format("%.2f", produto.preco);

        Label labelProduto = new Label(textoProduto);
        labelProduto.setStyle("-fx-text-fill: #333; -fx-font-size: 12px;");
        labelProduto.setWrapText(true);
        labelProduto.setPrefWidth(220); // Mais espaço para o texto
        HBox.setHgrow(labelProduto, Priority.ALWAYS);

        // Botão excluir - menor e alinhado à direita
        Button btnExcluir = new Button("✕");
        btnExcluir.setStyle(
                "-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-min-width: 30; -fx-min-height: 30; -fx-max-width: 30;");
        btnExcluir.setOnAction(e -> removerDoCarrinho(produto.id));
        btnExcluir.setCursor(javafx.scene.Cursor.HAND);

        itemBox.getChildren().addAll(labelProduto, btnExcluir);

        return itemBox;
    }

    private void removerDoCarrinho(int id) {
        ProdutoCarrinho produtoRemover = null;
        for (ProdutoCarrinho produto : carrinho) {
            if (produto.id == id) {
                produtoRemover = produto;
                break;
            }
        }

        if (produtoRemover != null) {
            if (produtoRemover.quantidade > 1) {
                // Se tem mais de 1, diminui a quantidade
                produtoRemover.quantidade--;
                total -= produtoRemover.preco;
            } else {
                // Se tem apenas 1, remove completamente
                carrinho.remove(produtoRemover);
                total -= produtoRemover.preco;
            }
            atualizarCarrinho();
            System.out.println("Item atualizado: " + produtoRemover.nome);
        }
    }

    @FXML
    private void finalizarPedido(ActionEvent event) {
        if (carrinho.isEmpty()) {
            System.out.println("Carrinho vazio! Adicione itens antes de finalizar.");
            return;
        }

        System.out.println("=== PEDIDO FINALIZADO ===");
        System.out.println("Itens do pedido:");
        for (ProdutoCarrinho produto : carrinho) {
            System.out.println("  " + produto.nome +
                    (produto.quantidade > 1 ? " (x" + produto.quantidade + ")" : "") +
                    " - R$ " + String.format("%.2f", produto.preco * produto.quantidade));
        }
        System.out.println("Total: R$ " + String.format("%.2f", total));
        System.out.println("========================");

        // Aqui você pode adicionar:
        // - Salvar no banco de dados
        // - Imprimir comprovante
        // - Enviar para cozinha

        limparCarrinho();
    }

    private void limparCarrinho() {
        carrinho.clear();
        total = 0.0;
        proximoId = 1;
        atualizarCarrinho();
        System.out.println("Carrinho limpo!");
    }

    @FXML
    private void voltarLogin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(
                new File("src/main/java/com/example/pdv_galeteria/Frontend/views/TelaLogin.fxml").toURI().toURL());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Métodos úteis para futuras expansões
    public List<String> getItensCarrinho() {
        List<String> itens = new ArrayList<>();
        for (ProdutoCarrinho produto : carrinho) {
            itens.add(produto.nome + " x" + produto.quantidade);
        }
        return itens;
    }

    public double getTotal() {
        return total;
    }
}