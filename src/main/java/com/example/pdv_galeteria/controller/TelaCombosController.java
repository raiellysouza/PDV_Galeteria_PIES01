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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
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

    @Autowired
    private ComboRepository comboRepository;
    

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

    @FXML
    public void carregarCombos() {
        try {
            System.out.println("🔄 Carregando combos...");

            comboContainer.getChildren().clear();
            List<Combo> combos = comboService.buscarTodosCombos();

            System.out.println("📊 Combos encontrados: " + (combos != null ? combos.size() : 0));

            if (combos == null || combos.isEmpty()) {
                Label vazio = new Label("Nenhum combo cadastrado");
                vazio.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 20;");
                comboContainer.getChildren().add(vazio);
                return;
            }

            for (Combo combo : combos) {
                System.out.println("🎴 Criando card para: " + combo.getNome());
                VBox card = criarCardCombo(combo);
                comboContainer.getChildren().add(card);
            }

            System.out.println("✅ Cards criados: " + comboContainer.getChildren().size());

        } catch (Exception e) {
            e.printStackTrace();
            comboContainer.getChildren().clear();
            Label erro = new Label("Erro ao carregar combos: " + e.getMessage());
            erro.setStyle("-fx-text-fill: red; -fx-padding: 10;");
            comboContainer.getChildren().add(erro);
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
            System.out.println("🎯 Tentando abrir TelaCombo.fxml...");

            // Tentar diferentes caminhos possíveis
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/telaCombo.fxml");

            if (fxmlUrl == null) {
                // Tentar caminho alternativo
                fxmlUrl = getClass().getResource("/Frontend/views/telaCombo.fxml");
            }

            if (fxmlUrl == null) {
                // Tentar caminho relativo
                fxmlUrl = getClass().getResource("telaCombo.fxml");
            }

            if (fxmlUrl == null) {
                System.err.println("❌ Arquivo FXML não encontrado em nenhum dos caminhos");

                // Listar recursos disponíveis para debug
                System.out.println("📁 Recursos disponíveis:");
                try {
                    java.nio.file.Path resourcesPath = java.nio.file.Paths.get("src/main/resources");
                    if (java.nio.file.Files.exists(resourcesPath)) {
                        java.nio.file.Files.walk(resourcesPath)
                                .filter(path -> path.toString().contains(".fxml"))
                                .forEach(path -> System.out.println("   - " + resourcesPath.relativize(path)));
                    }
                } catch (Exception ex) {
                    System.err.println("Erro ao listar recursos: " + ex.getMessage());
                }

                mostrarAlerta("Erro", "Arquivo da tela de combo não encontrado!", Alert.AlertType.ERROR);
                return;
            }

            System.out.println("✅ Arquivo FXML encontrado: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            // Se estiver usando Spring, descomente a linha abaixo
            loader.setControllerFactory(applicationContext::getBean);

            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Cadastro de Combo");
            stage.setResizable(false);

            stage.showAndWait(); // ou stage.show() se não quiser modal

            System.out.println("✅ Tela de combo aberta com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erro ao abrir tela de combo: " + e.getMessage());
            mostrarAlerta("Erro", "Erro ao abrir tela de combo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Adiciona um produto à lista temporária do combo
     */
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
            System.out.println("🎯 INICIANDO SALVAMENTO DO COMBO...");

            String nomeCombo = nomeComboField.getText().trim();
            String precoStr = precoComboField.getText().trim();

            // VALIDAÇÕES BÁSICAS
            if (nomeCombo.isEmpty()) {
                mostrarAlerta("Aviso", "Nome do combo é obrigatório!", Alert.AlertType.WARNING);
                nomeComboField.requestFocus();
                return;
            }

            if (precoStr.isEmpty()) {
                mostrarAlerta("Aviso", "Preço do combo é obrigatório!", Alert.AlertType.WARNING);
                precoComboField.requestFocus();
                return;
            }

            if (itensDoCombo.isEmpty()) {
                mostrarAlerta("Aviso", "Adicione ao menos um produto ao combo!", Alert.AlertType.WARNING);
                return;
            }

            // CONVERTER PREÇO
            Double precoTotal;
            try {
                precoStr = precoStr.replace(",", ".");
                precoTotal = Double.parseDouble(precoStr);

                if (precoTotal <= 0) {
                    mostrarAlerta("Erro", "Preço deve ser maior que zero!", Alert.AlertType.ERROR);
                    precoComboField.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarAlerta("Erro", "Preço inválido! Use números (ex: 25.90)", Alert.AlertType.ERROR);
                precoComboField.requestFocus();
                return;
            }

            // CRIAR COMBO
            Combo combo = new Combo();
            combo.setNome(nomeCombo);
            combo.setPrecoTotal(precoTotal);
            combo.setItensDoCombo(new ArrayList<>(itensDoCombo)); // Cria nova lista

            System.out.println("📦 Combo criado - chamando service...");

            // SALVAR (AGORA COM TRY-CATCH ESPECÍFICO)
            Combo comboSalvo;
            try {
                comboSalvo = comboService.salvarCombo(combo);
            } catch (Exception e) {
                System.err.println("❌ Erro no service: " + e.getMessage());
                throw e; // Propaga para o catch externo
            }

            // VERIFICAR SE FOI SALVO
            if (comboSalvo == null) {
                throw new RuntimeException("Service retornou combo nulo!");
            }

            if (comboSalvo.getId() == null) {
                throw new RuntimeException("Combo salvo mas ID é nulo!");
            }

            System.out.println("🎉 Combo salvo com ID: " + comboSalvo.getId());

            // LIMPAR E ATUALIZAR
            limparCamposEAtualizar();
            carregarCombos(); // Atualiza a lista

            mostrarAlerta("Sucesso",
                    "Combo salvo com sucesso!\n" +
                            "ID: " + comboSalvo.getId() + "\n" +
                            "Nome: " + comboSalvo.getNome() + "\n" +
                            "Preço: R$ " + comboSalvo.getPrecoTotal(),
                    Alert.AlertType.INFORMATION);

        } catch (IllegalArgumentException e) {
            // Erros de validação
            System.err.println("❌ Erro de validação: " + e.getMessage());
            mostrarAlerta("Erro de Validação", e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            // Erros gerais
            System.err.println("❌ Erro geral: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao salvar combo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Limpa todos os campos e a lista de itens
     */
    private void limparCamposEAtualizar() {
        nomeComboField.clear();
        precoComboField.clear();
        produtosTextArea.clear();
        itensDoCombo.clear();

        // Focar no primeiro campo
        nomeComboField.requestFocus();
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
