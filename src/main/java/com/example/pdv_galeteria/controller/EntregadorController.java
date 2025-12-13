package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.PdvGaleteriaApplication;
import com.example.pdv_galeteria.model.Entregador;
import com.example.pdv_galeteria.model.StatusEntregador;
import com.example.pdv_galeteria.repository.EntregadorRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import java.util.Optional;
import com.example.pdv_galeteria.service.CaixaService;

@Service
public class EntregadorController {

    @FXML private VBox containerEntregadores;
    @FXML private Label labelTotalEntregadores;
    @FXML private Label labelAtivosHoje;
    @FXML private Label labelEntregasHoje;
    @FXML private TextField txtNomeCompletoPopup;
    @FXML private TextField txtTelefonePopup;

    @Autowired
    private EntregadorRepository entregadorRepository;

    @FXML
    public void initialize() {
        System.out.println("=== INICIALIZANDO CONTROLLER ENTREGADORES ===");
        carregarEntregadores();
    }

    private void carregarEntregadores() {
        try {
            System.out.println("Carregando entregadores do banco...");

            containerEntregadores.getChildren().clear();

            VBox tituloBox = criarTitulo();
            containerEntregadores.getChildren().add(tituloBox);

            List<Entregador> entregadores = entregadorRepository.findAllByOrderByNomeAsc();
            System.out.println("Encontrados " + entregadores.size() + " entregadores");

            for (Entregador entregador : entregadores) {
                HBox card = criarCardEntregador(entregador);
                containerEntregadores.getChildren().add(card);
            }

            atualizarEstatisticas(entregadores);

        } catch (Exception e) {
            System.err.println("ERRO ao carregar entregadores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox criarTitulo() {
        VBox tituloBox = new VBox(5);
        tituloBox.setPadding(new Insets(0, 0, 20, 0));

        Label titulo = new Label("Entregadores do Dia");
        titulo.setTextFill(Color.web("#111827"));
        titulo.setFont(Font.font("System Bold", 22));

        Label subtitulo = new Label("Liste todos os entregadores cadastrados");
        subtitulo.setTextFill(Color.web("#6b7280"));
        subtitulo.setFont(Font.font(14));

        tituloBox.getChildren().addAll(titulo, subtitulo);
        return tituloBox;
    }

    private HBox criarCardEntregador(Entregador entregador) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: #F8FAFC;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #E5E7EB;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;"
        );
        card.setPadding(new Insets(15, 20, 15, 20));

        StackPane iconPane = new StackPane();
        Circle circleBg = new Circle(24, Color.web("#DBEAFE"));
        SVGPath personIcon = new SVGPath();
        personIcon.setContent("M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z");
        personIcon.setFill(Color.web("#3B82F6"));
        iconPane.getChildren().addAll(circleBg, personIcon);

        VBox infoBox = new VBox(2);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label nomeLabel = new Label(entregador.getNome());
        nomeLabel.setTextFill(Color.web("#111827"));
        nomeLabel.setFont(Font.font("System Bold", 16));

        Label telefoneLabel = new Label(entregador.getTelefone());
        telefoneLabel.setTextFill(Color.web("#6B7280"));
        telefoneLabel.setFont(Font.font(12));

        infoBox.getChildren().addAll(nomeLabel, telefoneLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox entregasBox = new VBox(2);
        entregasBox.setAlignment(Pos.CENTER_RIGHT);

        Label entregasTexto = new Label("Entregas hoje");
        entregasTexto.setTextFill(Color.web("#6B7280"));
        entregasTexto.setFont(Font.font(10));

        HBox contadorBox = new HBox(8);
        contadorBox.setAlignment(Pos.CENTER);

        Button btnMenos = new Button("-");
        btnMenos.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #6B7280;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 0 0 2 0;"
        );
        btnMenos.setOnAction(e -> alterarEntregas(entregador, -1));

        Label entregasCount = new Label(String.valueOf(entregador.getEntregasHoje()));
        entregasCount.setTextFill(Color.web("#111827"));
        entregasCount.setFont(Font.font("System Bold", 16));
        entregasCount.setMinWidth(20);
        entregasCount.setAlignment(Pos.CENTER);

        Button btnMais = new Button("+");
        btnMais.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #6B7280;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 0 0 2 0;"
        );
        btnMais.setOnAction(e -> alterarEntregas(entregador, 1));

        contadorBox.getChildren().addAll(btnMais, entregasCount, btnMenos);
        entregasBox.getChildren().addAll(entregasTexto, contadorBox);

        Label statusLabel = new Label(entregador.getStatus().getDescricao().toLowerCase());
        statusLabel.setStyle(
                "-fx-background-color: #F97316;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-font-size: 12;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 4 12;" +
                        "-fx-min-width: 56;" +
                        "-fx-alignment: center;"
        );

        switch (entregador.getStatus()) {
            case DISPONIVEL -> statusLabel.setStyle(
                    "-fx-background-color: #F97316;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 12;" +
                            "-fx-font-size: 12;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 4 12;" +
                            "-fx-min-width: 56;" +
                            "-fx-alignment: center;"
            );
            case EM_ENTREGA -> statusLabel.setStyle(
                    "-fx-background-color: #3B82F6;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 12;" +
                            "-fx-font-size: 12;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 4 12;" +
                            "-fx-min-width: 56;" +
                            "-fx-alignment: center;"
            );
            case INATIVO -> statusLabel.setStyle(
                    "-fx-background-color: #6B7280;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 12;" +
                            "-fx-font-size: 12;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 4 12;" +
                            "-fx-min-width: 56;" +
                            "-fx-alignment: center;"
            );
        }

        Button btnAcao = new Button();
        btnAcao.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #374151;" +
                        "-fx-border-color: #D1D5DB;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 12;" +
                        "-fx-padding: 4 12;" +
                        "-fx-font-weight: normal;"
        );
        btnAcao.setOnAction(e -> alternarStatus(entregador));

        if (entregador.getStatus() == StatusEntregador.INATIVO) {
            btnAcao.setText("Ativar");
        } else {
            btnAcao.setText("Desativar");
        }

        btnAcao.setOnMouseEntered(e -> btnAcao.setStyle(
                "-fx-background-color: #F9FAFB;" +
                        "-fx-text-fill: #1F2937;" +
                        "-fx-border-color: #9CA3AF;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 12;" +
                        "-fx-padding: 4 12;" +
                        "-fx-font-weight: normal;"
        ));

        btnAcao.setOnMouseExited(e -> btnAcao.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #374151;" +
                        "-fx-border-color: #D1D5DB;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 12;" +
                        "-fx-padding: 4 12;" +
                        "-fx-font-weight: normal;"
        ));

        HBox rightBox = new HBox(15);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.getChildren().addAll(entregasBox, statusLabel, btnAcao);

        card.getChildren().addAll(iconPane, infoBox, spacer, rightBox);
        return card;
    }

    private void alterarEntregas(Entregador entregador, int quantidade) {
        try {
            int novasEntregas = entregador.getEntregasHoje() + quantidade;
            if (novasEntregas >= 0) {
                entregador.setEntregasHoje(novasEntregas);
                entregadorRepository.save(entregador);
                carregarEntregadores();
                System.out.println("Entregas atualizadas: " + entregador.getNome() + " = " + novasEntregas);
            }
        } catch (Exception e) {
            System.err.println("Erro ao alterar entregas: " + e.getMessage());
        }
    }

    private void alternarStatus(Entregador entregador) {
        try {
            if (entregador.getStatus() == StatusEntregador.INATIVO) {
                entregador.setStatus(StatusEntregador.DISPONIVEL);
            } else {
                entregador.setStatus(StatusEntregador.INATIVO);
            }
            entregadorRepository.save(entregador);
            carregarEntregadores();
            System.out.println("Status alterado: " + entregador.getNome() + " = " + entregador.getStatus());
        } catch (Exception e) {
            System.err.println("Erro ao alterar status: " + e.getMessage());
        }
    }

    private void atualizarEstatisticas(List<Entregador> entregadores) {
        try {
            long total = entregadores.size();
            long ativos = entregadores.stream()
                    .filter(e -> e.getStatus() != StatusEntregador.INATIVO)
                    .count();
            long entregas = entregadores.stream()
                    .filter(e -> e.getStatus() != StatusEntregador.INATIVO)
                    .mapToInt(Entregador::getEntregasHoje)
                    .sum();

            Platform.runLater(() -> {
                labelTotalEntregadores.setText(String.valueOf(total));
                labelAtivosHoje.setText(String.valueOf(ativos));
                labelEntregasHoje.setText(String.valueOf(entregas));
            });

            System.out.println("Estatísticas: Total=" + total + ", Ativos=" + ativos + ", Entregas=" + entregas);

        } catch (Exception e) {
            System.err.println("Erro ao atualizar estatísticas: " + e.getMessage());
        }
    }

    @FXML
    public void abrirPopupCadastroFXML() {
        try {
            System.out.println("Abrindo popup FXML de cadastro...");

            if (txtNomeCompletoPopup == null || txtTelefonePopup == null) {
                System.out.println("Campos do popup não injetados. Carregando FXML...");

                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/example/pdv_galeteria/Frontend/views/PopupCadastroEntregador.fxml"
                ));

                loader.setController(this);

                Parent root = loader.load();

                System.out.println("Campos após load - Nome: " + (txtNomeCompletoPopup != null));
                System.out.println("Campos após load - Telefone: " + (txtTelefonePopup != null));

                if (txtNomeCompletoPopup != null) txtNomeCompletoPopup.clear();
                if (txtTelefonePopup != null) txtTelefonePopup.clear();

                Stage popupStage = new Stage();
                popupStage.setTitle("Cadastrar Entregador");
                popupStage.setScene(new Scene(root));
                popupStage.initModality(Modality.APPLICATION_MODAL);

                if (containerEntregadores != null && containerEntregadores.getScene() != null) {
                    popupStage.initOwner(containerEntregadores.getScene().getWindow());
                }

                popupStage.setResizable(false);

                Platform.runLater(() -> {
                    if (txtNomeCompletoPopup != null) {
                        txtNomeCompletoPopup.requestFocus();
                    }
                });

                popupStage.showAndWait();

            } else {
                System.out.println("Campos já injetados. Reutilizando...");

                txtNomeCompletoPopup.clear();
                txtTelefonePopup.clear();

                Stage stage = (Stage) txtNomeCompletoPopup.getScene().getWindow();

                if (stage != null) {
                    stage.requestFocus();
                    stage.toFront();
                }
            }

        } catch (Exception e) {
            System.err.println("ERRO ao abrir popup FXML: " + e.getMessage());
            e.printStackTrace();

            System.out.println("Usando Dialog como fallback...");
            abrirPopupCadastroDialog();
        }
    }

    private void abrirPopupCadastroDialog() {
        System.out.println("Abrindo popup de cadastro (Dialog)...");

        Dialog<Entregador> dialog = new Dialog<>();
        dialog.setTitle("Novo Entregador");
        dialog.setHeaderText("Cadastrar novo entregador");

        ButtonType salvarButton = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(salvarButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome completo");

        TextField telefoneField = new TextField();
        telefoneField.setPromptText("(85) 99999-9999");

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("Telefone:"), 0, 1);
        grid.add(telefoneField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(nomeField::requestFocus);

        dialog.setResultConverter(button -> {
            if (button == salvarButton) {
                String nome = nomeField.getText().trim();
                String telefone = telefoneField.getText().trim();

                if (nome.isEmpty() || telefone.isEmpty()) {
                    mostrarErro("Campos obrigatórios", "Preencha todos os campos.");
                    return null;
                }

                Entregador novo = new Entregador();
                novo.setNome(nome);
                novo.setTelefone(formatarTelefone(telefone));
                novo.setStatus(StatusEntregador.DISPONIVEL);
                novo.setEntregasHoje(0);

                return novo;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(novoEntregador -> {
            try {
                System.out.println("Salvando: " + novoEntregador);

                if (entregadorRepository.existsByTelefone(novoEntregador.getTelefone())) {
                    mostrarErro("Telefone já cadastrado", "Já existe um entregador com este telefone.");
                    return;
                }

                Entregador salvo = entregadorRepository.save(novoEntregador);
                System.out.println("Salvo com ID: " + salvo.getId());

                carregarEntregadores();

                Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                sucesso.setTitle("Sucesso");
                sucesso.setHeaderText(null);
                sucesso.setContentText("Entregador cadastrado com sucesso!");
                sucesso.showAndWait();

            } catch (Exception e) {
                System.err.println("ERRO ao salvar: " + e.getMessage());
                e.printStackTrace();
                mostrarErro("Erro ao salvar", e.getMessage());
            }
        });
    }

    @FXML
    public void abrirPopupCadastro() {
        System.out.println("Abrindo popup de cadastro...");
        abrirPopupCadastroFXML();
    }

    private String formatarTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            return "";
        }

        String numeros = telefone.replaceAll("\\D", "");

        if (numeros.length() == 11) {
            return "(" + numeros.substring(0, 2) + ") " +
                    numeros.substring(2, 7) + "-" +
                    numeros.substring(7);
        } else if (numeros.length() == 10) {
            return "(" + numeros.substring(0, 2) + ") " +
                    numeros.substring(2, 6) + "-" +
                    numeros.substring(6);
        }

        return telefone;
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert erro = new Alert(Alert.AlertType.ERROR);
        erro.setTitle("Erro");
        erro.setHeaderText(titulo);
        erro.setContentText(mensagem);
        erro.showAndWait();
    }

    @FXML
    public void salvarEntregador() {
        System.out.println("Salvando entregador do popup...");

        try {
            if (txtNomeCompletoPopup == null || txtTelefonePopup == null) {
                System.err.println("Campos do popup não encontrados! Usando Dialog...");
                abrirPopupCadastro();
                return;
            }

            String nome = txtNomeCompletoPopup.getText().trim();
            String telefone = txtTelefonePopup.getText().trim();

            if (nome.isEmpty()) {
                mostrarErro("Campo obrigatório", "Digite o nome do entregador.");
                txtNomeCompletoPopup.requestFocus();
                return;
            }

            if (telefone.isEmpty()) {
                mostrarErro("Campo obrigatório", "Digite o telefone do entregador.");
                txtTelefonePopup.requestFocus();
                return;
            }

            String telefoneFormatado = formatarTelefone(telefone);

            if (entregadorRepository.existsByTelefone(telefoneFormatado)) {
                mostrarErro("Telefone já cadastrado", "Já existe um entregador com este telefone.");
                txtTelefonePopup.requestFocus();
                txtTelefonePopup.selectAll();
                return;
            }

            Entregador novo = new Entregador();
            novo.setNome(nome);
            novo.setTelefone(telefoneFormatado);
            novo.setStatus(StatusEntregador.DISPONIVEL);
            novo.setEntregasHoje(0);

            Entregador salvo = entregadorRepository.save(novo);
            System.out.println("Entregador salvo do popup: " + salvo);

            Stage stage = (Stage) txtNomeCompletoPopup.getScene().getWindow();
            stage.close();

            carregarEntregadores();

            Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
            sucesso.setTitle("Sucesso");
            sucesso.setHeaderText(null);
            sucesso.setContentText("Entregador cadastrado com sucesso!");
            sucesso.showAndWait();

        } catch (Exception e) {
            System.err.println("Erro ao salvar entregador do popup: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao salvar", "Não foi possível salvar o entregador.");
        }
    }

    @FXML
    private void abrirTelaDashboard(ActionEvent event) {
        try {
            System.out.println("Abrindo tela de dashboard...");

            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaDashBoard.fxml");
            if (fxmlUrl == null) {
                System.err.println("Arquivo não encontrado: Dashboard.fxml");
                mostrarAlerta("Erro", "Tela de dashboard não disponível", Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");
            stage.centerOnScreen();

            System.out.println("Tela de dashboard carregada com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao abrir dashboard: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao abrir dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void abrirTelaVendas(ActionEvent event) {
        try {
            System.out.println("Abrindo tela de vendas...");

            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaRegistroPedido.fxml");
            if (fxmlUrl == null) {
                System.err.println("Arquivo FXML não encontrado: TelaRegistroPedido.fxml");
                mostrarAlerta("Erro", "Arquivo da tela de vendas não encontrado!", Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() == null) {
                try {
                    Parent root = loader.load();
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Registro de Pedidos");
                    stage.centerOnScreen();
                    System.out.println("Tela de vendas aberta sem Spring");
                    return;
                } catch (Exception fallbackException) {
                    fallbackException.printStackTrace();
                    mostrarAlerta("Erro", "Erro ao abrir tela: " + fallbackException.getMessage(), Alert.AlertType.ERROR);
                    return;
                }
            }

            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Registro de Pedidos");
            stage.centerOnScreen();

            System.out.println("Tela de vendas aberta com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir tela de vendas: " + e.getMessage());
            mostrarAlerta("Erro", "Erro ao abrir tela de vendas: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void abrirTelaEstoque(ActionEvent event) {
        try {
            System.out.println("Abrindo tela de estoque...");

            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaProdutos.fxml");
            if (fxmlUrl == null) {
                System.err.println("Arquivo não encontrado: TelaProdutos.fxml");
                mostrarAlerta("Erro", "Tela de estoque não disponível", Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Estoque");
            stage.centerOnScreen();

            System.out.println("Tela de estoque carregada com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao abrir estoque: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao abrir tela de estoque: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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
            Parent root = loader.load();

            CaixaController controller = loader.getController();
            if (PdvGaleteriaApplication.getSpringContext() != null) {
                CaixaService caixaService = PdvGaleteriaApplication.getSpringContext().getBean(CaixaService.class);
                controller.setCaixaService(caixaService);
                System.out.println("CaixaService injetado manualmente");
            }

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

    @FXML
    private void abrirTelaConfiguracoes(ActionEvent event) {
        try {
            System.out.println("Abrindo tela de configurações...");

            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaConfiguracoes.fxml");
            if (fxmlUrl == null) {
                System.err.println("Arquivo FXML não encontrado: TelaConfiguracoes.fxml");
                mostrarAlerta("Erro", "Tela de configurações não disponível", Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Configurações");
            stage.centerOnScreen();

            System.out.println("Tela de configurações aberta com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao abrir tela de configurações: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao abrir configurações: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void sairParaLogin(ActionEvent event) {
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
            popupStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            popupStage.setResizable(false);
            popupStage.centerOnScreen();

            popupStage.showAndWait();

            if (controller.isConfirmado()) {
                System.out.println("Usuário confirmou saída, voltando para login...");
                voltarParaTelaLogin(event);
            } else {
                System.out.println("Usuário cancelou a saída.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir pop-up de confirmação: " + e.getMessage());

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmação de Saída");
            alert.setHeaderText("Deseja realmente sair?");
            alert.setContentText("Você será redirecionado para a tela de login.");

            Optional<ButtonType> resultado = alert.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                voltarParaTelaLogin(event);
            }
        }
    }

    private void voltarParaTelaLogin(ActionEvent event) {
        try {
            System.out.println("Iniciando processo de volta para login...");

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

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

            System.out.println("Tela de login carregada com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao voltar para login: " + e.getMessage());
            reiniciarAplicacaoCompleta(event);
        }
    }

    private void reiniciarAplicacaoCompleta(ActionEvent event) {
        try {
            System.out.println("Tentando reiniciar aplicação completamente...");

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
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

            Stage stage = (Stage) labelAtivosHoje.getScene().getWindow();
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

            Stage stage = (Stage) labelAtivosHoje.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Configurações");
            stage.centerOnScreen();

        } catch (Exception e) {
        }
    }
}