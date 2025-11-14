package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.Combo;
import com.example.pdv_galeteria.model.ComboItem;
import com.example.pdv_galeteria.model.Produto;
import com.example.pdv_galeteria.repository.ComboRepository;
import com.example.pdv_galeteria.service.ComboService;
import com.example.pdv_galeteria.service.ProdutoService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelaCombosController {

    @Autowired
    private ComboService comboService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ApplicationContext applicationContext;

    private HBox combosContainer; // Não é @FXML

    // Campos da tela (vinculados pelo fx:id)
    @FXML private TextArea nomeComboField;
    @FXML private TextArea precoComboField;
    @FXML private TextArea nomeProdutoField;
    @FXML private TextArea quantidadeField;
    @FXML private TextArea produtosTextArea;
    @FXML private FlowPane comboContainer;

    // Lista temporária para armazenar os produtos do combo
    private final List<ComboItem> itensDoCombo = new ArrayList<>();


    @FXML
    public void initialize() {
        // será chamado automaticamente se o FXML for carregado normalmente
        carregarCombos();
    }

    // Método para receber o container da TelaProdutos
    public void setCombosContainer(HBox combosContainer) {
        this.combosContainer = combosContainer;
    }

    @FXML
    public void carregarCombos() {
        try {
            if (combosContainer == null) {
                System.out.println("❌ combosContainer não configurado!");
                return;
            }

            System.out.println("🔄 Carregando combos no HBox...");
            combosContainer.getChildren().clear();
            List<Combo> combos = comboService.buscarTodosCombos();

            System.out.println("📊 Combos encontrados: " + (combos != null ? combos.size() : 0));

            if (combos == null || combos.isEmpty()) {
                Label vazio = new Label("Nenhum combo cadastrado");
                vazio.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 20;");
                combosContainer.getChildren().add(vazio);
                return;
            }

            for (Combo combo : combos) {
                VBox card = criarCardCombo(combo);
                combosContainer.getChildren().add(card);
            }

            System.out.println("✅ Cards criados: " + combosContainer.getChildren().size());

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


    private VBox criarCardCombo(Combo combo) {
        VBox card = new VBox();
        card.setSpacing(6);
        card.setPrefWidth(260);
        card.setStyle("-fx-padding: 8; -fx-border-color: #ddd; -fx-border-radius: 6; -fx-background-color: white;");

        Label nome = new Label(combo.getNome() != null ? combo.getNome() : "Sem nome");
        nome.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label preco = new Label(String.format("Preço: R$ %.2f", combo.getPrecoTotal()));
        preco.setStyle("-fx-font-size: 14px; -fx-text-fill: #2a6df4;");

        Label qtd = new Label("Itens: " + (combo.getItensDoCombo() != null ? combo.getItensDoCombo().size() : 0));

        Button btnEditar = new Button("Editar");
        Button btnExcluir = new Button("Excluir");

        btnExcluir.setOnAction(e -> excluirCombo(combo));
        btnEditar.setOnAction(e -> abrirTelaEditarCombo(combo));

        card.getChildren().addAll(nome, preco, qtd, btnEditar, btnExcluir);
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

    /**
     * Abre a tela de cadastro de combos
     */
    @FXML
    public void abrirTelaCombo() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("com/example/pdv_galeteria/Frontend/views/telaCombo.fxml")
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

            // Use o método correto do Service
            Produto produto = produtoService.buscarPrimeiroPorNome(nomeProduto);
            if (produto == null) {
                mostrarAlerta("Erro", "Produto não encontrado: " + nomeProduto, Alert.AlertType.ERROR);
                return;
            }

            // Resto do código...
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

    /**
     * Salva o combo no banco de dados
     */
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
            combo.setPrecoTotal(preco.doubleValue()); // ou use BigDecimal se seu modelo aceitar
            combo.setItensDoCombo(itensDoCombo);

            comboService.salvarCombo(combo);

            mostrarAlerta("Sucesso", "Combo salvo com sucesso!", Alert.AlertType.INFORMATION);

            // Limpa todos os campos e a lista
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

    /**
     * Atualiza o campo de texto com a lista de produtos adicionados
     */
    private void atualizarListaDeProdutos() {
        StringBuilder sb = new StringBuilder();
        for (ComboItem item : itensDoCombo) {
            sb.append(item.getProduto().getNome())
              .append(" - Quantidade: ").append(item.getQuantidade())
              .append("\n");
        }
        produtosTextArea.setText(sb.toString());
    }

    /**
     * Exibe alertas simples na tela
     */
    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
