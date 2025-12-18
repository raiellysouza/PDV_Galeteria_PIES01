package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.PdvGaleteriaApplication;
import com.example.pdv_galeteria.model.*;
import com.example.pdv_galeteria.repository.EntregadorRepository;
import com.example.pdv_galeteria.repository.EntregaRepository;
import com.example.pdv_galeteria.repository.PedidoRepository;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import java.util.Optional;
import com.example.pdv_galeteria.service.CaixaService;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
public class EntregadorController {

    @FXML private VBox containerEntregadores;
    @FXML private Label labelTotalEntregadores;
    @FXML private Label labelAtivosHoje;
    @FXML private Label labelEntregasHoje;
    @FXML private TextField txtNomeCompletoPopup;
    @FXML private TextField txtTelefonePopup;
    @Autowired private UsuarioSessao usuarioSessao;
    @FXML private Label labelNomeUsuario;
    @FXML  public Label lblEntregadorNome;
    @FXML  public TextField txtNumeroPedidoPopup;
    @FXML  public TextField txtIdIfoodPopup;
    private Entregador entregadorAtual;

    @Autowired
    private EntregadorRepository entregadorRepository;

    @Autowired
    private EntregaRepository entregaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @FXML
    public void initialize() {
        System.out.println("=== INICIALIZANDO CONTROLLER ENTREGADORES ===");

        if (entregadorRepository == null && PdvGaleteriaApplication.getSpringContext() != null) {
            entregadorRepository = PdvGaleteriaApplication.getSpringContext().getBean(EntregadorRepository.class);
            System.out.println("EntregadorRepository injetado manualmente: " + (entregadorRepository != null));
        }

        if (entregaRepository == null && PdvGaleteriaApplication.getSpringContext() != null) {
            entregaRepository = PdvGaleteriaApplication.getSpringContext().getBean(EntregaRepository.class);
            System.out.println("EntregaRepository injetado manualmente: " + (entregaRepository != null));
        }

        if (containerEntregadores != null) {
            carregarEntregadores();
        } else {
            System.err.println("ERRO: containerEntregadores é null! Verifique o FXML.");
        }

        if (pedidoRepository == null && PdvGaleteriaApplication.getSpringContext() != null) {
            pedidoRepository = PdvGaleteriaApplication.getSpringContext().getBean(PedidoRepository.class);
        }

        if (labelNomeUsuario != null && usuarioSessao != null) {
            labelNomeUsuario.setText(usuarioSessao.getNomeUsuario());
        }

        atualizarNomeUsuarioNoMenu();
        carregarEntregadores();
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

    private void carregarEntregadores() {
        try {
            System.out.println("Carregando entregadores do banco...");

            if (containerEntregadores == null) {
                System.err.println("ERRO: containerEntregadores é null!");
                return;
            }

            if (entregadorRepository == null) {
                System.err.println("ERRO: entregadorRepository é null!");
                return;
            }

            containerEntregadores.getChildren().clear();

            VBox tituloBox = criarTitulo();
            containerEntregadores.getChildren().add(tituloBox);

            List<Entregador> entregadores = entregadorRepository.findAllByOrderByNomeAsc();
            System.out.println("Encontrados " + entregadores.size() + " entregadores");

            for (Entregador entregador : entregadores) {
                VBox cardCompleto = criarCardEntregadorCompleto(entregador);
                containerEntregadores.getChildren().add(cardCompleto);
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

    private VBox criarCardEntregadorCompleto(Entregador entregador) {
        VBox cardCompleto = new VBox(15);
        cardCompleto.setStyle(
                "-fx-background-color: #F8FAFC;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #E5E7EB;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-padding: 15;"
        );

        HBox topBox = criarTopBoxEntregador(entregador);
        cardCompleto.getChildren().add(topBox);

        List<Entrega> entregasHoje = entregaRepository.findEntregasHojeByEntregador(entregador);

        if (!entregasHoje.isEmpty()) {
            VBox entregasBox = criarEntregasBox(entregador, entregasHoje);
            cardCompleto.getChildren().add(entregasBox);
        }

        return cardCompleto;
    }

    private HBox criarTopBoxEntregador(Entregador entregador) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);

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

        long entregasHojeCount = entregaRepository.countByEntregadorAndDataHoraToday(entregador);
        Label entregasCount = new Label(String.valueOf(entregasHojeCount));
        entregasCount.setTextFill(Color.web("#111827"));
        entregasCount.setFont(Font.font("System Bold", 16));
        entregasCount.setMinWidth(20);
        entregasCount.setAlignment(Pos.CENTER);

        entregasBox.getChildren().addAll(entregasTexto, entregasCount);

        Label statusLabel = new Label(entregador.getStatus().getDescricao().toLowerCase());

        switch (entregador.getStatus()) {
            case DISPONIVEL -> {
                statusLabel.setText("ativo");
                statusLabel.setStyle(
                        "-fx-background-color: #10B981;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 12;" +
                                "-fx-font-size: 12;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 4 12;" +
                                "-fx-min-width: 56;" +
                                "-fx-alignment: center;"
                );
            }
            case EM_ENTREGA -> {
                statusLabel.setText("ativo");
                statusLabel.setStyle(
                        "-fx-background-color: #f68411;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 12;" +
                                "-fx-font-size: 12;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 4 12;" +
                                "-fx-min-width: 56;" +
                                "-fx-alignment: center;"
                );
            }
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

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnAddEntrega = new Button("+ Add Entrega");
        btnAddEntrega.setStyle(
                "-fx-background-color: #F97316;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 12;" +
                        "-fx-padding: 6 12;"
        );
        btnAddEntrega.setOnAction(e -> abrirPopupAdicionarEntrega(entregador));

        if (entregador.getStatus() == StatusEntregador.INATIVO) {
            btnAddEntrega.setDisable(true);
            btnAddEntrega.setStyle(
                    "-fx-background-color: #9CA3AF;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 6;" +
                            "-fx-font-size: 12;" +
                            "-fx-padding: 6 12;"
            );
        }

        btnAddEntrega.setOnMouseEntered(e -> {
            if (!btnAddEntrega.isDisabled()) {
                btnAddEntrega.setStyle(
                        "-fx-background-color: #EA580C;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-size: 12;" +
                                "-fx-padding: 6 12;"
                );
            }
        });

        btnAddEntrega.setOnMouseExited(e -> {
            if (!btnAddEntrega.isDisabled()) {
                btnAddEntrega.setStyle(
                        "-fx-background-color: #F97316;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-size: 12;" +
                                "-fx-padding: 6 12;"
                );
            }
        });

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

        buttonBox.getChildren().addAll(btnAddEntrega, statusLabel, btnAcao);

        HBox rightBox = new HBox(15);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.getChildren().addAll(entregasBox, buttonBox);

        card.getChildren().addAll(iconPane, infoBox, spacer, rightBox);
        return card;
    }

    private VBox criarEntregasBox(Entregador entregador, List<Entrega> entregas) {
        VBox entregasBox = new VBox(10);
        entregasBox.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-border-color: #E5E7EB;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 6;" +
                        "-fx-padding: 15;"
        );

        Label tituloEntregas = new Label("Entregas realizadas hoje:");
        tituloEntregas.setTextFill(Color.web("#111827"));
        tituloEntregas.setFont(Font.font("System Bold", 14));
        entregasBox.getChildren().add(tituloEntregas);

        GridPane tabela = new GridPane();
        tabela.setHgap(20);
        tabela.setVgap(8);
        tabela.setPadding(new Insets(5, 0, 0, 0));

        Label headerPedido = new Label("Pedido");
        headerPedido.setTextFill(Color.web("#6B7280"));
        headerPedido.setFont(Font.font("System Bold", 12));
        tabela.add(headerPedido, 0, 0);

        Label headerIfood = new Label("ID iFood");
        headerIfood.setTextFill(Color.web("#6B7280"));
        headerIfood.setFont(Font.font("System Bold", 12));
        tabela.add(headerIfood, 1, 0);

        Label headerHorario = new Label("Horário");
        headerHorario.setTextFill(Color.web("#6B7280"));
        headerHorario.setFont(Font.font("System Bold", 12));
        tabela.add(headerHorario, 2, 0);

        int row = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Entrega entrega : entregas.stream().limit(5).collect(Collectors.toList())) {
            Label pedidoLabel = new Label(entrega.getNumeroPedido());
            pedidoLabel.setTextFill(Color.web("#111827"));
            pedidoLabel.setFont(Font.font(12));
            tabela.add(pedidoLabel, 0, row);

            Label ifoodLabel = new Label(entrega.getIdIfood() != null && !entrega.getIdIfood().isEmpty() ? entrega.getIdIfood() : "-");
            ifoodLabel.setTextFill(Color.web("#6B7280"));
            ifoodLabel.setFont(Font.font(12));
            tabela.add(ifoodLabel, 1, row);

            Label horarioLabel = new Label(entrega.getDataHora().format(formatter));
            horarioLabel.setTextFill(Color.web("#6B7280"));
            horarioLabel.setFont(Font.font(12));
            tabela.add(horarioLabel, 2, row);

            row++;
        }

        entregasBox.getChildren().add(tabela);

        if (entregas.size() > 5) {
            Label maisLabel = new Label("... e mais " + (entregas.size() - 5) + " entregas");
            maisLabel.setTextFill(Color.web("#6B7280"));
            maisLabel.setFont(Font.font(10));
            maisLabel.setPadding(new Insets(5, 0, 0, 0));
            entregasBox.getChildren().add(maisLabel);
        }

        return entregasBox;
    }

    @FXML
    private void abrirPopupAdicionarEntrega(Entregador entregador) {
        try {
            System.out.println("Abrindo popup FXML para adicionar entrega ao entregador: " + entregador.getNome());

            this.entregadorAtual = entregador;

            abrirPopupAdicionarEntregaFXML(entregador);

        } catch (Exception e) {
            System.err.println("Erro ao abrir popup de entrega: " + e.getMessage());
            e.printStackTrace();
            abrirPopupAdicionarEntregaDialog(entregador);
        }
    }

    private void abrirPopupAdicionarEntregaFXML(Entregador entregador) {
        try {
            System.out.println("Carregando PopupAdicionarEntrega.fxml...");

            URL fxmlUrl = getClass().getResource(
                    "/com/example/pdv_galeteria/Frontend/views/PopupAdicionarEntrega.fxml"
            );

            if (fxmlUrl == null) {
                System.err.println("ERRO: PopupAdicionarEntrega.fxml não encontrado!");
                mostrarAlerta("Erro", "Arquivo de adicionar entrega não encontrado.", Alert.AlertType.ERROR);
                abrirPopupAdicionarEntregaDialog(entregador);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);


            Parent root = loader.load();

            EntregadorController controller = loader.getController();

            if (controller.lblEntregadorNome != null) {
                controller.lblEntregadorNome.setText("Entregador: " + entregador.getNome());
            }

            controller.entregadorAtual = entregador;

            if (controller.txtNumeroPedidoPopup != null) {
                controller.txtNumeroPedidoPopup.clear();
                controller.txtNumeroPedidoPopup.requestFocus();
            }
            if (controller.txtIdIfoodPopup != null) {
                controller.txtIdIfoodPopup.clear();
            }

            Stage popupStage = new Stage();
            popupStage.setTitle("Adicionar Entrega");
            popupStage.setScene(new Scene(root));
            popupStage.initModality(Modality.APPLICATION_MODAL);

            if (containerEntregadores != null && containerEntregadores.getScene() != null) {
                popupStage.initOwner(containerEntregadores.getScene().getWindow());
            }

            popupStage.setResizable(false);
            popupStage.showAndWait();

        } catch (Exception e) {
            System.err.println("ERRO ao abrir popup FXML de entrega: " + e.getMessage());
            e.printStackTrace();
            abrirPopupAdicionarEntregaDialog(entregador);
        }
    }

    private void abrirPopupAdicionarEntregaDialog(Entregador entregador) {
        try {
            System.out.println("Abrindo dialog de adicionar entrega (fallback)...");

            Dialog<Entrega> dialog = new Dialog<>();
            dialog.setTitle("Adicionar Entrega");
            dialog.setHeaderText("Associar pedido ao entregador: " + entregador.getNome());

            ButtonType salvarButton = new ButtonType("Adicionar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(salvarButton, ButtonType.CANCEL);

            VBox content = new VBox(15);
            content.setPadding(new Insets(20));
            content.setStyle("-fx-background-color: white;");

            Label lblTitulo = new Label("Adicionar Nova Entrega");
            lblTitulo.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #111827;");

            Label lblEntregador = new Label("Entregador: " + entregador.getNome());
            lblEntregador.setStyle("-fx-font-size: 14; -fx-text-fill: #374151;");

            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(15);
            grid.setPadding(new Insets(10, 0, 0, 0));

            TextField txtNumeroPedido = new TextField();
            txtNumeroPedido.setPromptText("Ex: 001");
            txtNumeroPedido.setPrefWidth(200);
            txtNumeroPedido.setStyle("-fx-padding: 8; -fx-border-color: #D1D5DB; -fx-border-radius: 4;");

            TextField txtIdIfood = new TextField();
            txtIdIfood.setPromptText("Ex: IF-12345");
            txtIdIfood.setPrefWidth(200);
            txtIdIfood.setStyle("-fx-padding: 8; -fx-border-color: #D1D5DB; -fx-border-radius: 4;");

            Label lblObrigatorio = new Label("* Campo obrigatório");
            lblObrigatorio.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 11;");

            Label lblOpcional = new Label("Preencha apenas se for uma entrega do iFood");
            lblOpcional.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11;");

            grid.add(new Label("Número do Pedido *:"), 0, 0);
            grid.add(txtNumeroPedido, 1, 0);
            grid.add(lblObrigatorio, 1, 1);

            grid.add(new Label("ID iFood (opcional):"), 0, 2);
            grid.add(txtIdIfood, 1, 2);
            grid.add(lblOpcional, 1, 3);

            content.getChildren().addAll(lblTitulo, lblEntregador, grid);
            dialog.getDialogPane().setContent(content);

            Platform.runLater(txtNumeroPedido::requestFocus);

            dialog.setResultConverter(button -> {
                if (button == salvarButton) {
                    String numeroPedido = txtNumeroPedido.getText().trim();
                    String idIfood = txtIdIfood.getText().trim();

                    if (numeroPedido.isEmpty()) {
                        mostrarErro("Campo obrigatório", "Digite o número do pedido.");
                        return null;
                    }

                    if (entregaRepository.existsByNumeroPedido(numeroPedido)) {
                        mostrarErro("Pedido já registrado", "Já existe uma entrega com este número de pedido.");
                        return null;
                    }

                    Entrega novaEntrega = new Entrega();
                    novaEntrega.setEntregador(entregador);
                    novaEntrega.setNumeroPedido(numeroPedido);
                    novaEntrega.setIdIfood(idIfood.isEmpty() ? null : idIfood);
                    novaEntrega.setDataHora(LocalDateTime.now());

                    return novaEntrega;
                }
                return null;
            });

            dialog.showAndWait().ifPresent(novaEntrega -> {
                try {
                    Entrega salva = entregaRepository.save(novaEntrega);
                    System.out.println("Entrega salva: " + salva.getId());

                    if (entregador.getStatus() == StatusEntregador.DISPONIVEL) {
                        entregador.setStatus(StatusEntregador.EM_ENTREGA);
                        entregadorRepository.save(entregador);
                    }

                    entregador.setEntregasHoje(entregador.getEntregasHoje() + 1);
                    entregadorRepository.save(entregador);

                    carregarEntregadores();

                    Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                    sucesso.setTitle("Sucesso");
                    sucesso.setHeaderText(null);
                    sucesso.setContentText("Entrega adicionada com sucesso para " + entregador.getNome() + "!");
                    sucesso.showAndWait();

                } catch (Exception e) {
                    System.err.println("Erro ao salvar entrega: " + e.getMessage());
                    e.printStackTrace();
                    mostrarErro("Erro ao salvar", "Não foi possível salvar a entrega.");
                }
            });

        } catch (Exception e) {
            System.err.println("Erro ao abrir popup de entrega (dialog): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void alternarStatus(Entregador entregador) {
        try {
            if (entregador.getStatus() == StatusEntregador.INATIVO) {
                entregador.setStatus(StatusEntregador.DISPONIVEL);
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Entregador Ativado");
                info.setHeaderText(null);
                info.setContentText(entregador.getNome() + " foi ativado e agora está disponível para entregas.");
                info.showAndWait();
            } else {
                Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
                confirmacao.setTitle("Confirmar Desativação");
                confirmacao.setHeaderText("Desativar " + entregador.getNome() + "?");
                confirmacao.setContentText("O entregador não poderá receber novas entregas enquanto estiver inativo.");

                Optional<ButtonType> resultado = confirmacao.showAndWait();
                if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                    entregador.setStatus(StatusEntregador.INATIVO);

                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Entregador Desativado");
                    info.setHeaderText(null);
                    info.setContentText(entregador.getNome() + " foi desativado.");
                    info.showAndWait();
                } else {
                    return;
                }
            }

            entregadorRepository.save(entregador);
            carregarEntregadores();
            System.out.println("Status alterado: " + entregador.getNome() + " = " + entregador.getStatus());

        } catch (Exception e) {
            System.err.println("Erro ao alterar status: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro", "Não foi possível alterar o status do entregador.");
        }
    }

    private void atualizarEstatisticas(List<Entregador> entregadores) {
        try {
            final long total = entregadores.size();
            final long ativos = entregadores.stream()
                    .filter(e -> e.getStatus() != StatusEntregador.INATIVO)
                    .count();

            long entregasHojeCalc = 0;
            LocalDate hoje = LocalDate.now();
            LocalDateTime startOfDay = hoje.atStartOfDay();
            LocalDateTime endOfDay = hoje.atTime(LocalTime.MAX);

            for (Entregador entregador : entregadores) {
                if (entregador.getStatus() != StatusEntregador.INATIVO) {
                    try {
                        entregasHojeCalc += entregaRepository.countByEntregadorAndDateRange(
                                entregador, startOfDay, endOfDay
                        );
                    } catch (Exception e) {
                        System.err.println("Erro na consulta de entregas, usando fallback: " + e.getMessage());
                        List<Entrega> entregasHoje = entregaRepository.findByEntregadorOrderByDataHoraDesc(entregador);
                        entregasHojeCalc += entregasHoje.stream()
                                .filter(entrega -> entrega.getDataHora().toLocalDate().equals(hoje))
                                .count();
                    }
                }
            }

            final long entregasHojeTotal = entregasHojeCalc;

            Platform.runLater(() -> {
                labelTotalEntregadores.setText(String.valueOf(total));
                labelAtivosHoje.setText(String.valueOf(ativos));
                labelEntregasHoje.setText(String.valueOf(entregasHojeTotal));
            });

            System.out.println("Estatísticas: Total=" + total + ", Ativos=" + ativos + ", Entregas Hoje=" + entregasHojeTotal);

        } catch (Exception e) {
            System.err.println("Erro ao atualizar estatísticas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void abrirPopupCadastro() {
        System.out.println("Abrindo tela FXML de cadastro de entregador...");
        abrirTelaCadastroEntregadorFXML();
    }

    private void abrirTelaCadastroEntregadorFXML() {
        try {
            System.out.println("Carregando TelaCadastroEntregadores.fxml...");

            URL fxmlUrl = getClass().getResource(
                    "/com/example/pdv_galeteria/Frontend/views/TelaCadastroEntregadores.fxml"
            );

            if (fxmlUrl == null) {
                System.err.println("ERRO: TelaCadastroEntregadores.fxml não encontrado!");
                mostrarAlerta("Erro", "Arquivo de cadastro não encontrado.", Alert.AlertType.ERROR);
                abrirPopupCadastroDialog();
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.setTitle("Cadastrar Novo Entregador");
            popupStage.setScene(new Scene(root));
            popupStage.initModality(Modality.APPLICATION_MODAL);

            if (containerEntregadores != null && containerEntregadores.getScene() != null) {
                popupStage.initOwner(containerEntregadores.getScene().getWindow());
            }

            popupStage.setResizable(false);

            if (txtNomeCompletoPopup != null) {
                txtNomeCompletoPopup.clear();
                txtNomeCompletoPopup.requestFocus();
            }
            if (txtTelefonePopup != null) {
                txtTelefonePopup.clear();
            }

            popupStage.showAndWait();

        } catch (Exception e) {
            System.err.println("ERRO ao abrir tela de cadastro FXML: " + e.getMessage());
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

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        Label lblTitulo = new Label("Cadastro de Entregador");
        lblTitulo.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #111827;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 0, 0));

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome completo");
        nomeField.setPrefWidth(250);
        nomeField.setStyle("-fx-padding: 8; -fx-border-color: #D1D5DB; -fx-border-radius: 4;");

        TextField telefoneField = new TextField();
        telefoneField.setPromptText("(85) 99999-9999");
        telefoneField.setPrefWidth(250);
        telefoneField.setStyle("-fx-padding: 8; -fx-border-color: #D1D5DB; -fx-border-radius: 4;");

        Label lblObrigatorio = new Label("* Campos obrigatórios");
        lblObrigatorio.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11;");

        grid.add(new Label("Nome *:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("Telefone *:"), 0, 1);
        grid.add(telefoneField, 1, 1);
        grid.add(lblObrigatorio, 1, 2);

        content.getChildren().addAll(lblTitulo, grid);
        dialog.getDialogPane().setContent(content);

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

                if (entregadorRepository == null) {
                    if (PdvGaleteriaApplication.getSpringContext() != null) {
                        entregadorRepository = PdvGaleteriaApplication.getSpringContext().getBean(EntregadorRepository.class);
                    }
                    if (entregadorRepository == null) {
                        mostrarErro("Erro", "Serviço não disponível. Tente novamente.");
                        return;
                    }
                }

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
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(titulo);
            alert.setContentText(mensagem);
            alert.showAndWait();
        });
    }

    @FXML
    public void salvarEntregador() {
        System.out.println("Salvando entregador do popup...");

        try {
            if (txtNomeCompletoPopup == null || txtTelefonePopup == null) {
                System.err.println("Campos do popup não encontrados! Usando Dialog...");
                abrirPopupCadastroDialog();
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

            if (entregadorRepository == null) {
                if (PdvGaleteriaApplication.getSpringContext() != null) {
                    entregadorRepository = PdvGaleteriaApplication.getSpringContext().getBean(EntregadorRepository.class);
                }
                if (entregadorRepository == null) {
                    mostrarErro("Erro", "Serviço não disponível. Tente novamente.");
                    return;
                }
            }

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

            fecharPopupCadastro();

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

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

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

            Stage currentStage = (Stage) labelNomeUsuario.getScene().getWindow();

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

    @FXML
    private void fecharPopupCadastro() {
        try {
            Stage stage = (Stage) txtNomeCompletoPopup.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            System.err.println("Erro ao fechar popup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void salvarEntregaPopup() {
        try {
            if (entregadorAtual == null) {
                mostrarErro("Erro", "Entregador não definido.");
                return;
            }

            String numeroPedidoOuId = txtNumeroPedidoPopup.getText().trim();
            String idIfood = txtIdIfoodPopup.getText().trim();

            if (numeroPedidoOuId.isEmpty()) {
                mostrarErro("Campo obrigatório", "Digite o número do pedido.");
                txtNumeroPedidoPopup.requestFocus();
                return;
            }

            Optional<Pedido> pedidoOpt = pedidoRepository.findByNumeroPedido(numeroPedidoOuId);

            if (pedidoOpt.isEmpty()) {
                try {
                    if (numeroPedidoOuId.matches("\\d+")) {
                        Long pedidoId = Long.parseLong(numeroPedidoOuId);
                        pedidoOpt = pedidoRepository.findById(pedidoId);
                    }
                } catch (NumberFormatException e) {
                }
            }

            if (pedidoOpt.isEmpty()) {
                mostrarErro("Pedido não encontrado",
                        "Não existe pedido com: " + numeroPedidoOuId);
                txtNumeroPedidoPopup.requestFocus();
                txtNumeroPedidoPopup.selectAll();
                return;
            }

            Pedido pedido = pedidoOpt.get();

            if (!pedido.isEntrega()) {
                mostrarErro("Pedido não é para entrega",
                        "Este pedido é para retirada na loja.\n" +
                                "Não pode ser associado a entregador.");
                txtNumeroPedidoPopup.requestFocus();
                txtNumeroPedidoPopup.selectAll();
                return;
            }

            if (pedido.getEntregadorAssociado() != null &&
                    !pedido.getEntregadorAssociado().getId().equals(entregadorAtual.getId())) {
                mostrarErro("Pedido já tem entregador",
                        "Este pedido já está com: " + pedido.getEntregadorAssociado().getNome());
                txtNumeroPedidoPopup.requestFocus();
                txtNumeroPedidoPopup.selectAll();
                return;
            }

            Entrega novaEntrega = new Entrega();
            novaEntrega.setEntregador(entregadorAtual);
            novaEntrega.setNumeroPedido(pedido.getNumeroPedido());
            novaEntrega.setIdIfood(idIfood.isEmpty() ? null : idIfood);
            novaEntrega.setDataHora(LocalDateTime.now());

            pedido.setEntregadorAssociado(entregadorAtual);
            pedido.setEntregador(entregadorAtual.getNome());
            pedido.setStatusEntrega("EM_ROTA");
            pedidoRepository.save(pedido);

            entregaRepository.save(novaEntrega);

            if (entregadorAtual.getStatus() == StatusEntregador.DISPONIVEL) {
                entregadorAtual.setStatus(StatusEntregador.EM_ENTREGA);
            }
            entregadorAtual.setEntregasHoje(entregadorAtual.getEntregasHoje() + 1);
            entregadorRepository.save(entregadorAtual);

            Stage stage = (Stage) txtNumeroPedidoPopup.getScene().getWindow();
            stage.close();

            atualizarTelaEntregadoresSimples();

        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro", "Não foi possível salvar a entrega.");
        }
    }

    private void atualizarTelaEntregadoresSimples() {
        try {
            System.out.println("=== ATUALIZAÇÃO SIMPLES INICIADA ===");

            new Thread(() -> {
                try {
                    Thread.sleep(300);

                    Platform.runLater(() -> {
                        try {
                            carregarEntregadores();

                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Sucesso");
                            alert.setHeaderText(null);
                            alert.setContentText("Entrega registrada com sucesso!\nA lista foi atualizada.");
                            alert.showAndWait();

                            System.out.println("=== ATUALIZAÇÃO CONCLUÍDA ===");

                        } catch (Exception e) {
                            System.err.println("Erro no Platform.runLater: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        } catch (Exception e) {
            System.err.println("Erro na atualização simples: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void atualizarCardEntregadorSimples(Long entregadorId) {
        try {
            System.out.println("Atualizando card do entregador ID: " + entregadorId);

            Optional<Entregador> entregadorAtualizadoOpt = entregadorRepository.findById(entregadorId);
            if (entregadorAtualizadoOpt.isEmpty()) {
                System.err.println("Entregador não encontrado no banco!");
                carregarEntregadores();
                return;
            }

            Entregador entregadorAtualizado = entregadorAtualizadoOpt.get();

            List<Entrega> entregasHoje = entregaRepository.findEntregasHojeByEntregador(entregadorAtualizado);

            for (int i = 1; i < containerEntregadores.getChildren().size(); i++) {
                Node node = containerEntregadores.getChildren().get(i);
                if (node instanceof VBox) {
                    VBox cardCompleto = (VBox) node;

                    if (isCardDoEntregador(cardCompleto, entregadorAtualizado)) {
                        System.out.println("Card encontrado, atualizando...");

                        VBox novoCard = criarCardEntregadorCompleto(entregadorAtualizado);
                        containerEntregadores.getChildren().set(i, novoCard);

                        atualizarEstatisticas(entregadorRepository.findAllByOrderByNomeAsc());

                        System.out.println("Card atualizado com sucesso!");
                        return;
                    }
                }
            }

            System.out.println("Card não encontrado, recarregando todos...");
            carregarEntregadores();

        } catch (Exception e) {
            System.err.println("Erro ao atualizar card: " + e.getMessage());
            e.printStackTrace();
            carregarEntregadores();
        }
    }

    private boolean isCardDoEntregador(VBox cardCompleto, Entregador entregador) {
        try {
            for (Node node : cardCompleto.getChildren()) {
                if (node instanceof HBox) {
                    HBox topBox = (HBox) node;

                    for (Node child : topBox.getChildren()) {
                        if (child instanceof VBox) {
                            VBox infoBox = (VBox) child;

                            for (Node labelNode : infoBox.getChildren()) {
                                if (labelNode instanceof Label) {
                                    Label nomeLabel = (Label) labelNode;
                                    if (nomeLabel.getText().equals(entregador.getNome())) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao verificar card: " + e.getMessage());
        }
        return false;
    }

    private void abrirDetalhesPedido(Long pedidoId) {
        try {
            Optional<Pedido> pedidoOpt = pedidoRepository.buscarPedidoComItens(pedidoId);
            if (pedidoOpt.isPresent()) {
                Pedido pedido = pedidoOpt.get();

                Dialog<Void> dialog = new Dialog<>();
                dialog.setTitle("Detalhes do Pedido #" + pedido.getNumeroPedido());

                VBox content = new VBox(10);
                content.setPadding(new Insets(15));

                TableView<ItemPedido> tabelaItens = new TableView<>();

                TableColumn<ItemPedido, String> colProduto = new TableColumn<>("Produto");
                colProduto.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().getProduto() != null ?
                                data.getValue().getProduto() :
                                "Produto não informado")
                );

                TableColumn<ItemPedido, Integer> colQuantidade = new TableColumn<>("Qtd");
                colQuantidade.setCellValueFactory(data ->
                        new SimpleIntegerProperty(data.getValue().getQuantidade() != null ?
                                data.getValue().getQuantidade() : 0).asObject()
                );

                TableColumn<ItemPedido, Double> colPreco = new TableColumn<>("Preço Unit.");
                colPreco.setCellValueFactory(data ->
                        new SimpleDoubleProperty(data.getValue().getPrecoUnitario() != null ?
                                data.getValue().getPrecoUnitario() : 0.0).asObject()
                );

                TableColumn<ItemPedido, Double> colTotal = new TableColumn<>("Total");
                colTotal.setCellValueFactory(data -> {
                    Integer qtd = data.getValue().getQuantidade() != null ? data.getValue().getQuantidade() : 0;
                    Double preco = data.getValue().getPrecoUnitario() != null ? data.getValue().getPrecoUnitario() : 0.0;
                    return new SimpleDoubleProperty(qtd * preco).asObject();
                });

                tabelaItens.getColumns().addAll(colProduto, colQuantidade, colPreco, colTotal);

                tabelaItens.getItems().addAll(pedido.getItens());

                colProduto.setPrefWidth(200);
                colQuantidade.setPrefWidth(80);
                colPreco.setPrefWidth(100);
                colTotal.setPrefWidth(100);

                VBox infoBox = new VBox(5);
                infoBox.setStyle("-fx-background-color: #f9fafb; -fx-padding: 10; -fx-border-radius: 5;");

                Label lblCliente = new Label("Cliente: " + pedido.getCliente());
                Label lblTelefone = new Label("Telefone: " + (pedido.getTelefone() != null ? pedido.getTelefone() : "Não informado"));
                Label lblEndereco = new Label("Endereço: " + pedido.getEnderecoCompleto());
                Label lblTotal = new Label("Total: R$ " + String.format("%.2f", pedido.getTotalFinal().doubleValue()));
                Label lblStatus = new Label("Status: " + pedido.getStatus() +
                        (pedido.getStatusEntrega() != null ? " (" + pedido.getStatusEntrega() + ")" : ""));

                infoBox.getChildren().addAll(lblCliente, lblTelefone, lblEndereco, lblTotal, lblStatus);

                content.getChildren().addAll(infoBox, new Label("Itens do Pedido:"), tabelaItens);

                dialog.getDialogPane().setContent(content);
                dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                dialog.setResizable(true);
                dialog.getDialogPane().setPrefSize(600, 400);

                dialog.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("Erro ao abrir detalhes do pedido: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void fecharPopupAdicionarEntrega() {
        try {
            if (lblEntregadorNome != null && lblEntregadorNome.getScene() != null) {
                Stage stage = (Stage) lblEntregadorNome.getScene().getWindow();
                stage.close();
            } else if (txtNumeroPedidoPopup != null && txtNumeroPedidoPopup.getScene() != null) {
                Stage stage = (Stage) txtNumeroPedidoPopup.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            System.err.println("Erro ao fechar popup de entrega: " + e.getMessage());
            e.printStackTrace();
        }
    }
}