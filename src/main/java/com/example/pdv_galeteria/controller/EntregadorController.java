package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.Entregador;
import com.example.pdv_galeteria.model.StatusEntregador;
import com.example.pdv_galeteria.service.EntregadorService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Controller
public class EntregadorController implements Initializable {

    @Autowired
    private EntregadorService entregadorService;

    @FXML private TableView<Entregador> tableView;
    @FXML private TableColumn<Entregador, Long> colId;
    @FXML private TableColumn<Entregador, String> colNome;
    @FXML private TableColumn<Entregador, String> colTelefone;
    @FXML private TableColumn<Entregador, String> colStatus;
    @FXML private TextField txtBusca;
    @FXML private Button btnNovoEntregador;
    @FXML private Label labelTotalEntregadores;
    @FXML private Label labelAtivosHoje;
    @FXML private Label labelEntregasHoje;
    @FXML private VBox containerEntregadores;

    @FXML private TextField txtNomeCompletoPopup;
    @FXML private TextField txtTelefonePopup;

    private Stage popupStage;
    private ObservableList<Entregador> entregadoresList = FXCollections.observableArrayList();
    private Entregador entregadorParaEdicao;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabela();
        carregarEntregadores();
        configurarBusca();
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nomeCompleto"));
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tableView.setRowFactory(tv -> new TableRow<Entregador>() {
            @Override
            protected void updateItem(Entregador entregador, boolean empty) {
                super.updateItem(entregador, empty);
                if (entregador == null || empty) {
                    setStyle("");
                } else {
                    if (entregador.getStatus() == StatusEntregador.INATIVO) {
                        setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        tableView.setItems(entregadoresList);
    }
    private void carregarEntregadores() {
        try {
            entregadoresList.clear();
            entregadoresList.addAll(entregadorService.listarTodos());

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro",
                    "Erro ao carregar entregadores: " + e.getMessage());
        }
    }

    private void configurarBusca() {
        txtBusca.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                carregarEntregadores();
            } else {
                buscarEntregadores(newValue.trim());
            }
        });
    }

    private void buscarEntregadores(String termo) {
        try {
            entregadoresList.clear();
            entregadoresList.addAll(entregadorService.buscarPorNome(termo));
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro",
                    "Erro na busca: " + e.getMessage());
        }
    }

    @FXML
    private void abrirPopupCadastro() {
        abrirPopup(null);
    }

    @FXML
    private void editarEntregador() {
        Entregador selecionado = tableView.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            abrirPopup(selecionado);
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "Atenção",
                    "Selecione um entregador para editar.");
        }
    }

    @FXML
    private void excluirEntregador() {
        Entregador selecionado = tableView.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atenção",
                    "Selecione um entregador para excluir.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText("Excluir Entregador");
        confirmacao.setContentText("Tem certeza que deseja excluir " + selecionado.getNomeCompleto() + "?");

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                try {
                    entregadorService.excluirEntregador(selecionado.getId());
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                            "Entregador excluído com sucesso!");
                    carregarEntregadores();
                } catch (Exception e) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Erro",
                            "Erro ao excluir: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void atualizarStatusParaDisponivel() {
        atualizarStatus(StatusEntregador.DISPONIVEL);
    }

    @FXML
    private void atualizarStatusParaEmEntrega() {
        atualizarStatus(StatusEntregador.EM_ENTREGA);
    }

    private void atualizarStatus(StatusEntregador status) {
        Entregador selecionado = tableView.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atenção",
                    "Selecione um entregador para alterar o status.");
            return;
        }

        try {
            entregadorService.atualizarStatus(selecionado.getId(), status);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                    "Status atualizado para: " + status.getDescricao());
            carregarEntregadores();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro",
                    "Erro ao atualizar status: " + e.getMessage());
        }
    }

    private void abrirPopup(Entregador entregadorParaEditar) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaCadastroEntregadores.fxml")
            );

            loader.setController(this);

            Parent root = loader.load();

            this.entregadorParaEdicao = entregadorParaEditar;
            if (entregadorParaEditar != null) {
                txtNomeCompletoPopup.setText(entregadorParaEditar.getNomeCompleto());
                txtTelefonePopup.setText(entregadorParaEditar.getTelefone());
            } else {
                limparCamposPopup();
            }

            popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(tableView.getScene().getWindow());

            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.setTitle(entregadorParaEditar != null ? "Editar Entregador" : "Cadastrar Entregador");
            popupStage.setResizable(false);

            popupStage.showAndWait();

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro",
                    "Não foi possível abrir a tela de cadastro: " + e.getMessage());
        }
    }

    @FXML
    private void salvarEntregador() {
        if (validarCamposPopup()) {
            try {
                String nome = txtNomeCompletoPopup.getText().trim();
                String telefone = txtTelefonePopup.getText().trim();

                if (entregadorParaEdicao != null) {
                    entregadorParaEdicao.setNomeCompleto(nome);
                    entregadorParaEdicao.setTelefone(telefone);
                    entregadorService.atualizarEntregador(entregadorParaEdicao);
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                            "Entregador atualizado com sucesso!");
                } else {
                    entregadorService.cadastrarEntregador(nome, telefone);
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                            "Entregador cadastrado com sucesso!");
                }

                fecharPopup();
                carregarEntregadores();

            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro",
                        "Erro ao salvar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void cancelarCadastro() {
        fecharPopup();
    }

    private boolean validarCamposPopup() {
        if (txtNomeCompletoPopup.getText() == null || txtNomeCompletoPopup.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atenção",
                    "Informe o nome completo do entregador.");
            txtNomeCompletoPopup.requestFocus();
            return false;
        }

        String telefone = txtTelefonePopup.getText().trim();
        if (telefone.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atenção",
                    "Informe o telefone do entregador.");
            txtTelefonePopup.requestFocus();
            return false;
        }

        String numeros = telefone.replaceAll("\\D", "");
        if (numeros.length() < 10) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atenção",
                    "Telefone inválido. Deve conter DDD + número (mínimo 10 dígitos).");
            txtTelefonePopup.requestFocus();
            return false;
        }

        return true;
    }

    private void limparCamposPopup() {
        if (txtNomeCompletoPopup != null) txtNomeCompletoPopup.clear();
        if (txtTelefonePopup != null) txtTelefonePopup.clear();
        entregadorParaEdicao = null;
    }

    private void fecharPopup() {
        if (popupStage != null) {
            popupStage.close();
            popupStage = null;
        }
        limparCamposPopup();
    }

    @FXML
    private void abrirTelaVendas() {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaRegistroPedido.fxml", "Registro de Pedidos");
    }

    @FXML
    private void abrirTelaEstoque() {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaProdutos.fxml", "Estoque");
    }

    @FXML
    private void abrirTelaCaixa() {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaCaixa.fxml", "Controle de Caixa");
    }

    @FXML
    private void sairParaLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaSairPrograma.fxml"));

            Parent root = loader.load();
            ConfirmacaoSaidaController controller = loader.getController();

            Stage popupStage = new Stage();
            controller.setPopupStage(popupStage);

            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Confirmação de Saída");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(tableView.getScene().getWindow());
            popupStage.setResizable(false);
            popupStage.centerOnScreen();

            popupStage.showAndWait();

            if (controller.isConfirmado()) {
                voltarParaTelaLogin();
            }

        } catch (Exception e) {
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

    private void navegarParaTela(String fxmlPath, String titulo) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Arquivo " + fxmlPath + " não encontrado!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (com.example.pdv_galeteria.PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(com.example.pdv_galeteria.PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            Stage stage = (Stage) tableView.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao abrir tela: " + e.getMessage());
        }
    }

    private void voltarParaTelaLogin() {
        try {
            Stage stage = (Stage) tableView.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaLogin.fxml"));

            if (com.example.pdv_galeteria.PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(com.example.pdv_galeteria.PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            reiniciarAplicacaoCompleta();
        }
    }

    private void reiniciarAplicacaoCompleta() {
        try {
            Stage stage = (Stage) tableView.getScene().getWindow();
            stage.close();

            com.example.pdv_galeteria.PdvGaleteriaApplication.relaunchApplication();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao reiniciar aplicação");
            alert.setContentText("Por favor, feche e abra o programa manualmente.");
            alert.showAndWait();

            javafx.application.Platform.exit();
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML
    private void desativarEntregador() {
        Entregador selecionado = tableView.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atenção",
                    "Selecione um entregador para desativar.");
            return;
        }

        if (selecionado.getStatus() == StatusEntregador.INATIVO) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Informação",
                    "Este entregador já está desativado.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Desativação");
        confirmacao.setHeaderText("Desativar Entregador");
        confirmacao.setContentText("Tem certeza que deseja desativar o entregador " +
                selecionado.getNomeCompleto() + "?\n\n" +
                "Observação: Entregadores desativados não poderão ser atribuídos a novas entregas.");

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                try {
                    entregadorService.atualizarStatus(selecionado.getId(), StatusEntregador.INATIVO);
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                            "Entregador desativado com sucesso!");
                    carregarEntregadores();
                } catch (Exception e) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Erro",
                            "Erro ao desativar entregador: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void abrirTelaRelatorios() {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaRelatorios.fxml", "Relatórios");
    }

    @FXML
    private void abrirTelaConfiguracao() {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaConfiguracao.fxml", "Configurações");
    }

    @FXML
    private void abrirTelaDashboard() {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaDashboard.fxml", "Dashboard");
    }

    private void carregarEntregadoresNoContainer() {
        try {
            containerEntregadores.getChildren().clear();
            List<Entregador> entregadores = entregadorService.listarTodos();

            for (Entregador entregador : entregadores) {
                HBox cardEntregador = criarCardEntregador(entregador);
                containerEntregadores.getChildren().add(cardEntregador);
            }
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao carregar entregadores: " + e.getMessage());
        }
    }

    private HBox criarCardEntregador(Entregador entregador) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-padding: 15;");

        return card;
    }
}