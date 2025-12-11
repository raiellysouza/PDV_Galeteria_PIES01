package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.Combo;
import com.example.pdv_galeteria.model.ComboItem;
import com.example.pdv_galeteria.model.Produto;
import com.example.pdv_galeteria.service.ComboService;
import com.example.pdv_galeteria.service.ProdutoService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import com.example.pdv_galeteria.model.ComboItem;
import com.example.pdv_galeteria.model.Produto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class TelaCombosController {

    @Autowired
    private ComboService comboService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private FlowPane produtosContainer;

    @FXML
    private FlowPane combosContainer;

    @FXML
    private VBox sugestoesContainer;

    @FXML private TextArea nomeComboField;
    @FXML private TextArea precoComboField;
    @FXML private TextArea nomeProdutoField;
    @FXML private TextArea quantidadeField;
    @FXML private FlowPane comboContainer;

    private final List<ComboItem> itensDoCombo = new ArrayList<>();
    @FXML
    private TextArea nomeComboField;

    @FXML
    private TextArea precoComboField;

    @FXML
    private TextArea nomeProdutoField;

    @FXML
    private TextArea quantidadeField;

    @FXML
    private TextArea produtosTextArea;

    @FXML
    private FlowPane comboContainer;

    private final List<ComboItem> itensDoCombo = new ArrayList<>();

    @FXML
    public void initialize() {
        carregarCombos();
    }

    public void setCombosContainer(FlowPane combosContainer) {
        this.combosContainer = combosContainer;
    }

    @FXML
    public void carregarCombos() {
        try {
            if (combosContainer == null) {
                System.out.println("combosContainer não configurado!");
                return;
            }

            System.out.println("Carregando combos no VBox...");
            combosContainer.getChildren().clear();
            List<Combo> combos = comboService.buscarTodosCombos();

            System.out.println("Combos encontrados: " + (combos != null ? combos.size() : 0));

            if (combos == null || combos.isEmpty()) {
                Label vazio = new Label("Nenhum combo cadastrado");
                vazio.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 20;");
                combosContainer.getChildren().add(vazio);
                return;
            }

            for (Combo combo : combos) {
                Pane card = criarCardCombo(combo);
                combosContainer.getChildren().add(card);
            }

            System.out.println("Cards criados: " + combosContainer.getChildren().size());
        } catch (Exception e) {
            e.printStackTrace();
            if (combosContainer != null) {
                combosContainer.getChildren().clear();
                Label erro = new Label("Erro ao carregar combos: " + e.getMessage());
                erro.setStyle("-fx-text-fill: red; -fx-padding: 10;");
                combosContainer.getChildren().add(erro);
            }
        }
    }

    public Pane criarCardCombo(Combo combo) {
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

        Label labelNome = new Label(combo.getNome() != null ? combo.getNome() : "Sem nome");
        labelNome.setLayoutX(14);
        labelNome.setLayoutY(14);
        labelNome.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label labelPreco = new Label(String.format("R$ %.2f", combo.getPrecoTotal()));
        labelPreco.setLayoutX(14);
        labelPreco.setLayoutY(82);
        labelPreco.setStyle("-fx-font-weight: bold; -fx-font-size: 28px; -fx-text-fill: #2a6df4;");

        int qtdItens = combo.getItensDoCombo() != null ? combo.getItensDoCombo().size() : 0;
        Label labelQtd = new Label("Itens: " + qtdItens);
        labelQtd.setLayoutX(14);
        labelQtd.setLayoutY(120);
        labelQtd.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        Button btnEditar = new Button("Editar");
        btnEditar.setLayoutX(300);
        btnEditar.setLayoutY(120);
        btnEditar.setOnAction(e -> abrirTelaEditarCombo(combo));

        Button btnExcluir = new Button("Excluir");
        btnExcluir.setLayoutX(350);
        btnExcluir.setLayoutY(120);
        btnExcluir.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
        btnExcluir.setOnAction(e -> excluirCombo(combo));

        card.getChildren().addAll(labelNome, labelPreco, labelQtd, btnEditar, btnExcluir);

        return card;
    }

    private void abrirTelaEditarCombo(Combo combo) {
        try {
            Combo comboCompleto = comboService.buscarPorIdComItens(combo.getId());

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/EditarCombo.fxml")
            );
            loader.setControllerFactory(applicationContext::getBean);

            Parent root = loader.load();

            EditarCombosController controller = loader.getController();
            controller.setCombo(comboCompleto);
            Stage stage = new Stage();
            stage.setTitle("Editar Combo");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao abrir tela de edição: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void excluirCombo(Combo combo) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Deseja realmente excluir o combo \"" + combo.getNome() + "\"?");
        var resultado = confirmacao.showAndWait();

        if (resultado.isPresent() && resultado.get().getButtonData().isDefaultButton()) {
            try {
                comboService.deletarCombo(combo.getId());
                mostrarAlerta("Sucesso", "Combo excluído com sucesso!", Alert.AlertType.INFORMATION);
                carregarCombos();
            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Erro", "Erro ao excluir combo: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }


    @FXML
    public void abrirTelaCombo() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/telaCombo.fxml")
            );

            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Cadastro de Combo");
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao abrir tela de combo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void adicionarProduto() {
        try {
            String nomeProduto = nomeProdutoField.getText().trim();
            String qtdStr = quantidadeField.getText().trim();

            if (nomeProduto.isEmpty() || qtdStr.isEmpty()) {
                mostrarAlerta("Aviso", "Preencha o nome do produto e a quantidade.", Alert.AlertType.WARNING);
                return;
            }

            int quantidade = Integer.parseInt(qtdStr);

            Produto produto = produtoService.buscarPrimeiroPorNome(nomeProduto);
            if (produto == null) {
                mostrarAlerta("Erro", "Produto não encontrado: " + nomeProduto, Alert.AlertType.ERROR);
                return;
            }

            ComboItem item = new ComboItem();
            item.setProduto(produto);
            item.setQuantidade(quantidade);
            itensDoCombo.add(item);
            atualizarListaDeProdutos();
            nomeProdutoField.clear();
            quantidadeField.clear();
        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "Quantidade inválida. Digite um número inteiro.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao adicionar produto: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void salvarCombo() {
        try {
            String nomeCombo = nomeComboField.getText().trim();
            String precoStr = precoComboField.getText().trim();

            if (nomeCombo.isEmpty() || precoStr.isEmpty() || itensDoCombo.isEmpty()) {
                mostrarAlerta("Aviso", "Preencha o nome, preço e adicione ao menos um produto.", Alert.AlertType.WARNING);
                return;
            }

            BigDecimal preco = new BigDecimal(precoStr.replace(",", "."));
            Combo combo = new Combo();
            combo.setNome(nomeCombo);
            combo.setPrecoTotal(preco.doubleValue());
            combo.setItensDoCombo(itensDoCombo);
            comboService.salvarCombo(combo);
            mostrarAlerta("Sucesso", "Combo salvo com sucesso!", Alert.AlertType.INFORMATION);

            nomeComboField.clear();
            precoComboField.clear();
            produtosTextArea.clear();
            itensDoCombo.clear();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao salvar combo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        carregarCombos();
    }


private void atualizarListaDeProdutos() {

    produtosListContainer.getChildren().clear(); 
    for (int i = 0; i < itensDoCombo.size(); i++) {

        ComboItem item = itensDoCombo.get(i);

        HBox linha = new HBox();
        linha.setSpacing(10);
        linha.setStyle("""
                -fx-background-color: #f7f7f7;
                -fx-padding: 10;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-border-color: #ddd;
                """);

        Label nome = new Label(item.getQuantidade() + "x  " + item.getProduto().getNome());
        nome.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        Button btnExcluir = new Button("🗑");
        btnExcluir.setStyle("""
                -fx-background-color: transparent;
                -fx-font-size: 16px;
                -fx-cursor: hand;
                """);

        final int index = i;

        btnExcluir.setOnAction(e -> {
            itensDoCombo.remove(index);
            atualizarListaDeProdutos();
        });

        linha.getChildren().addAll(nome, espaco, btnExcluir);
        produtosListContainer.getChildren().add(linha);
    }
}

private void mostrarSugestoes(List<Produto> produtos) {
    sugestoesContainer.getChildren().clear();

    if (produtos == null || produtos.isEmpty()) {
        sugestoesContainer.setVisible(false);
        return;
    }

    for (Produto p : produtos) {

        Label opcao = new Label(p.getNome());
        opcao.setStyle("-fx-padding: 6; -fx-background-color: white; -fx-font-size: 14;");
        opcao.setMaxWidth(Double.MAX_VALUE);

        opcao.setOnMouseEntered(e -> opcao.setStyle("-fx-padding: 6; -fx-background-color: #e6e6e6; -fx-font-size: 14;"));
        opcao.setOnMouseExited(e -> opcao.setStyle("-fx-padding: 6; -fx-background-color: white; -fx-font-size: 14;"));

        opcao.setOnMouseClicked(e -> {
            nomeProdutoField.setText(p.getNome());
            sugestoesContainer.setVisible(false);
        });

        sugestoesContainer.getChildren().add(opcao);
    }

    sugestoesContainer.setVisible(true);
}


    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}