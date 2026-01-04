package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.PdvGaleteriaApplication;
import com.example.pdv_galeteria.model.*;
import com.example.pdv_galeteria.repository.EntregadorRepository;
import com.example.pdv_galeteria.repository.EntregaRepository;
import com.example.pdv_galeteria.repository.PedidoRepository;
import com.example.pdv_galeteria.service.CaixaService;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @FXML public Label lblEntregadorNome;
    @FXML public TextField txtNumeroPedidoPopup;
    @FXML public TextField txtIdIfoodPopup;
    private Entregador entregadorAtual;

    @Autowired private EntregadorRepository entregadorRepository;
    @Autowired private EntregaRepository entregaRepository;
    @Autowired private PedidoRepository pedidoRepository;

    @FXML
    public void initialize() {
        inicializarRepositorios();

        if (containerEntregadores != null) {
            carregarEntregadores();
        }

        if (labelNomeUsuario != null && usuarioSessao != null) {
            labelNomeUsuario.setText(usuarioSessao.getNomeUsuario());
        }

        atualizarNomeUsuarioNoMenu();
    }

    private void inicializarRepositorios() {
        if (entregadorRepository == null && PdvGaleteriaApplication.getSpringContext() != null) {
            entregadorRepository = PdvGaleteriaApplication.getSpringContext().getBean(EntregadorRepository.class);
        }

        if (entregaRepository == null && PdvGaleteriaApplication.getSpringContext() != null) {
            entregaRepository = PdvGaleteriaApplication.getSpringContext().getBean(EntregaRepository.class);
        }

        if (pedidoRepository == null && PdvGaleteriaApplication.getSpringContext() != null) {
            pedidoRepository = PdvGaleteriaApplication.getSpringContext().getBean(PedidoRepository.class);
        }
    }

    private void atualizarNomeUsuarioNoMenu() {
        if (labelNomeUsuario != null && usuarioSessao != null) {
            labelNomeUsuario.setText(usuarioSessao.getNomeUsuario());
        }
    }

    private void carregarEntregadores() {
        try {
            if (containerEntregadores == null || entregadorRepository == null) {
                return;
            }

            List<Entregador> entregadores = entregadorRepository.findAllByOrderByNomeAsc();

            Platform.runLater(() -> {
                try {
                    List<Node> nodesParaManter = new ArrayList<>();
                    for (Node node : containerEntregadores.getChildren()) {
                        if (node instanceof VBox && containerEntregadores.getChildren().indexOf(node) == 0) {
                            nodesParaManter.add(node);
                        }
                    }

                    containerEntregadores.getChildren().clear();
                    containerEntregadores.getChildren().addAll(nodesParaManter);

                    if (nodesParaManter.isEmpty()) {
                        containerEntregadores.getChildren().add(criarTitulo());
                    }

                    for (Entregador entregador : entregadores) {
                        containerEntregadores.getChildren().add(criarCardEntregadorCompleto(entregador));
                    }

                    atualizarEstatisticas(entregadores);

                    containerEntregadores.layout();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox criarEntregasBox(Entregador entregador, List<Entrega> entregas) {
        VBox entregasBox = new VBox(10);
        entregasBox.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 6; -fx-padding: 15;");

        Label tituloEntregas = new Label("Entregas realizadas hoje:");
        tituloEntregas.setTextFill(Color.web("#111827"));
        tituloEntregas.setFont(Font.font("System Bold", 14));
        entregasBox.getChildren().add(tituloEntregas);

        GridPane tabela = new GridPane();
        tabela.setHgap(15);
        tabela.setVgap(8);
        tabela.setPadding(new Insets(5, 0, 0, 0));

        tabela.add(criarHeaderLabel("ID Pedido"), 0, 0);
        tabela.add(criarHeaderLabel("Nº Pedido"), 1, 0);
        tabela.add(criarHeaderLabel("Cliente"), 2, 0);
        tabela.add(criarHeaderLabel("Valor"), 3, 0);
        tabela.add(criarHeaderLabel("Horário"), 4, 0);

        int row = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Entrega entrega : entregas.stream().limit(5).collect(Collectors.toList())) {
            Pedido pedido = entrega.getPedido();

            String idPedidoTexto = pedido != null ? "#" + pedido.getId() : "N/A";
            Label lblId = criarDetalheLabel(idPedidoTexto);
            if (pedido != null) {
                lblId.setStyle("-fx-font-weight: bold; -fx-text-fill: #3B82F6;");
            }

            Label lblNumero = criarDetalheLabel(entrega.getNumeroPedido());

            String cliente = pedido != null && pedido.getCliente() != null ?
                    pedido.getCliente() : "Cliente não informado";
            Label lblCliente = criarDetalheLabel(cliente);

            String valorTexto;
            if (pedido != null) {
                BigDecimal valorFinal = pedido.getTotalFinal();
                if (valorFinal != null) {
                    valorTexto = String.format("R$ %.2f", valorFinal.doubleValue());
                } else {
                    valorTexto = String.format("R$ %.2f", pedido.getTotal());
                }
            } else {
                valorTexto = "R$ 0,00";
            }
            Label lblValor = criarDetalheLabel(valorTexto);
            lblValor.setStyle("-fx-font-weight: bold; -fx-text-fill: #059669;");

            Label lblHorario = criarDetalheLabel(entrega.getDataHora().format(formatter));

            tabela.add(lblId, 0, row);
            tabela.add(lblNumero, 1, row);
            tabela.add(lblCliente, 2, row);
            tabela.add(lblValor, 3, row);
            tabela.add(lblHorario, 4, row);

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

    private Label criarHeaderLabel(String texto) {
        Label label = new Label(texto);
        label.setTextFill(Color.web("#6B7280"));
        label.setFont(Font.font("System Bold", 12));
        return label;
    }

    private Label criarDetalheLabel(String texto) {
        Label label = new Label(texto);
        label.setTextFill(Color.web("#111827"));
        label.setFont(Font.font(12));
        return label;
    }

    @FXML
    private void abrirPopupAdicionarEntrega(Entregador entregador) {
        try {
            this.entregadorAtual = entregador;
            abrirPopupAdicionarEntregaFXML(entregador);
        } catch (Exception e) {
            e.printStackTrace();
            abrirPopupAdicionarEntregaDialog(entregador);
        }
    }

    private void abrirPopupAdicionarEntregaFXML(Entregador entregador) {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/PopupAdicionarEntrega.fxml");
            if (fxmlUrl == null) {
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
            e.printStackTrace();
            abrirPopupAdicionarEntregaDialog(entregador);
        }
    }

    private void abrirPopupAdicionarEntregaDialog(Entregador entregador) {
        try {
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

            TextField txtNumeroPedido = criarTextField("Ex: 001");
            TextField txtIdIfood = criarTextField("Ex: IF-12345");

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

                    Optional<Pedido> pedidoOpt = pedidoRepository.findByNumeroPedido(numeroPedido);
                    if (pedidoOpt.isEmpty()) {
                        mostrarErro("Pedido não encontrado", "Não existe pedido com número: " + numeroPedido);
                        return null;
                    }

                    Pedido pedido = pedidoOpt.get();

                    if (!pedido.isEntrega()) {
                        mostrarErro("Pedido não é para entrega", "Este pedido é para retirada na loja.");
                        return null;
                    }

                    if (pedido.getEntregadorAssociado() != null && !pedido.getEntregadorAssociado().getId().equals(entregador.getId())) {
                        mostrarErro("Pedido já tem entregador", "Este pedido já está com: " + pedido.getEntregadorAssociado().getNome());
                        return null;
                    }

                    Entrega novaEntrega = new Entrega();
                    novaEntrega.setEntregador(entregador);
                    novaEntrega.setNumeroPedido(numeroPedido);
                    novaEntrega.setIdIfood(idIfood.isEmpty() ? null : idIfood);
                    novaEntrega.setDataHora(LocalDateTime.now());
                    novaEntrega.setPedido(pedido);

                    return novaEntrega;
                }
                return null;
            });

            dialog.showAndWait().ifPresent(novaEntrega -> {
                salvarEntrega(novaEntrega, entregador);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TextField criarTextField(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.setPrefWidth(200);
        textField.setStyle("-fx-padding: 8; -fx-border-color: #D1D5DB; -fx-border-radius: 4;");
        return textField;
    }

    private void salvarEntrega(Entrega novaEntrega, Entregador entregador) {
        try {
            Entrega salva = entregaRepository.save(novaEntrega);

            Optional<Pedido> pedidoOpt = pedidoRepository.findByNumeroPedido(novaEntrega.getNumeroPedido());
            if (pedidoOpt.isPresent()) {
                Pedido pedido = pedidoOpt.get();
                pedido.setEntregadorAssociado(entregador);
                pedido.setEntregador(entregador.getNome());
                pedido.setStatusEntrega("EM_ROTA");
                pedidoRepository.save(pedido);
            }

            if (entregador.getStatus() == StatusEntregador.DISPONIVEL) {
                entregador.setStatus(StatusEntregador.EM_ENTREGA);
            }

            List<Entrega> entregasHoje = buscarEntregasHoje(entregador);
            entregador.setEntregasHoje(entregasHoje.size());
            entregadorRepository.save(entregador);

            atualizarTelaCompleta();

            Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
            sucesso.setTitle("Sucesso");
            sucesso.setHeaderText(null);
            sucesso.setContentText("Entrega adicionada com sucesso para " + entregador.getNome() + "!");
            sucesso.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao salvar", "Não foi possível salvar a entrega.");
        }
    }

    private void alternarStatus(Entregador entregador) {
        try {
            if (entregador.getStatus() == StatusEntregador.INATIVO) {
                entregador.setStatus(StatusEntregador.DISPONIVEL);
                mostrarAlerta("Entregador Ativado", entregador.getNome() + " foi ativado e agora está disponível para entregas.");
            } else {
                Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
                confirmacao.setTitle("Confirmar Desativação");
                confirmacao.setHeaderText("Desativar " + entregador.getNome() + "?");
                confirmacao.setContentText("O entregador não poderá receber novas entregas enquanto estiver inativo.");

                Optional<ButtonType> resultado = confirmacao.showAndWait();
                if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                    entregador.setStatus(StatusEntregador.INATIVO);
                    mostrarAlerta("Entregador Desativado", entregador.getNome() + " foi desativado.");
                } else {
                    return;
                }
            }

            entregadorRepository.save(entregador);

            atualizarTelaCompleta();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro", "Não foi possível alterar o status do entregador.");
        }
    }

    @FXML
    public void abrirPopupCadastro() {
        abrirTelaCadastroEntregadorFXML();
    }

    private void abrirTelaCadastroEntregadorFXML() {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaCadastroEntregadores.fxml");
            if (fxmlUrl == null) {
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
            e.printStackTrace();
            abrirPopupCadastroDialog();
        }
    }

    private void abrirPopupCadastroDialog() {
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

        TextField nomeField = criarTextField("Nome completo");
        TextField telefoneField = criarTextField("(85) 99999-9999");

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
            salvarNovoEntregador(novoEntregador);
        });
    }

    private String formatarTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            return "";
        }

        String numeros = telefone.replaceAll("\\D", "");

        if (numeros.length() == 11) {
            return "(" + numeros.substring(0, 2) + ") " + numeros.substring(2, 7) + "-" + numeros.substring(7);
        } else if (numeros.length() == 10) {
            return "(" + numeros.substring(0, 2) + ") " + numeros.substring(2, 6) + "-" + numeros.substring(6);
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

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle(titulo);
        info.setHeaderText(null);
        info.setContentText(mensagem);
        info.showAndWait();
    }

    @FXML
    public void salvarEntregador() {
        try {
            if (txtNomeCompletoPopup == null || txtTelefonePopup == null) {
                abrirPopupCadastroDialog();
                return;
            }

            String nome = txtNomeCompletoPopup.getText().trim();
            String telefone = txtTelefonePopup.getText().trim();

            if (nome.isEmpty() || telefone.isEmpty()) {
                mostrarErro("Campo obrigatório", "Digite o nome e telefone do entregador.");
                return;
            }

            String telefoneFormatado = formatarTelefone(telefone);

            if (entregadorRepository == null) {
                inicializarRepositorios();
                if (entregadorRepository == null) {
                    mostrarErro("Erro", "Serviço não disponível. Tente novamente.");
                    return;
                }
            }

            if (entregadorRepository.existsByTelefone(telefoneFormatado)) {
                mostrarErro("Telefone já cadastrado", "Já existe um entregador com este telefone.");
                return;
            }

            Entregador novo = new Entregador();
            novo.setNome(nome);
            novo.setTelefone(telefoneFormatado);
            novo.setStatus(StatusEntregador.DISPONIVEL);
            novo.setEntregasHoje(0);

            salvarNovoEntregador(novo);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao salvar", "Não foi possível salvar o entregador.");
        }
    }

    @FXML
    private void abrirTelaDashboard(ActionEvent event) {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaDashBoard.fxml", "Dashboard", event);
    }

    @FXML
    private void abrirTelaVendas(ActionEvent event) {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaRegistroPedido.fxml", "Registro de Pedidos", event);
    }

    @FXML
    private void abrirTelaEstoque(ActionEvent event) {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaProdutos.fxml", "Estoque", event);
    }

    @FXML
    private void abrirTelaCaixa(ActionEvent event) {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaCaixa.fxml", "Controle de Caixa", event);
    }

    @FXML
    private void abrirTelaConfiguracoes(ActionEvent event) {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaConfiguracoes.fxml", "Configurações", event);
    }

    @FXML
    private void abrirTelaRelatorios(ActionEvent event) {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaRelatorio.fxml", "Relatórios", event);
    }

    @FXML
    private void sairParaLogin(ActionEvent event) {
        try {
            if (usuarioSessao != null) {
                usuarioSessao.logout();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaSairPrograma.fxml"));

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

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
                voltarParaTelaLogin(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaLogin.fxml"));

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            reiniciarAplicacaoCompleta(event);
        }
    }

    private void reiniciarAplicacaoCompleta(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
            PdvGaleteriaApplication.relaunchApplication();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    private void navegarParaTela(String fxmlPath, String titulo, ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            if (PdvGaleteriaApplication.getSpringContext() != null) {
                loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);
            }

            Parent root = loader.load();

            if (fxmlPath.contains("TelaCaixa.fxml")) {
                CaixaController controller = loader.getController();
                if (PdvGaleteriaApplication.getSpringContext() != null) {
                    CaixaService caixaService = PdvGaleteriaApplication.getSpringContext().getBean(CaixaService.class);
                    controller.setCaixaService(caixaService);
                }
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void fecharPopupCadastro() {
        try {
            Stage stage = (Stage) txtNomeCompletoPopup.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
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

            Optional<Pedido> pedidoOpt = buscarPedido(numeroPedidoOuId);

            if (pedidoOpt.isEmpty()) {
                mostrarErro("Pedido não encontrado", "Não existe pedido com: " + numeroPedidoOuId);
                txtNumeroPedidoPopup.requestFocus();
                txtNumeroPedidoPopup.selectAll();
                return;
            }

            Pedido pedido = pedidoOpt.get();

            if (!validarPedidoParaEntrega(pedido)) {
                return;
            }

            if (entregaRepository.existsByNumeroPedido(pedido.getNumeroPedido())) {
                mostrarErro("Entrega já registrada", "Já existe uma entrega registrada para o pedido: " + pedido.getNumeroPedido());
                txtNumeroPedidoPopup.requestFocus();
                txtNumeroPedidoPopup.selectAll();
                return;
            }

            Entrega novaEntrega = criarNovaEntregaComPedido(pedido, idIfood);
            entregaRepository.save(novaEntrega);

            atualizarPedido(pedido);

            atualizarEntregador();

            Stage stage = (Stage) txtNumeroPedidoPopup.getScene().getWindow();
            stage.close();

            atualizarTelaCompleta();

            Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
            sucesso.setTitle("Sucesso");
            sucesso.setHeaderText(null);
            sucesso.setContentText("Entrega registrada para " + entregadorAtual.getNome() + "!");
            sucesso.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro", "Não foi possível salvar a entrega: " + e.getMessage());
        }
    }

    private Entrega criarNovaEntregaComPedido(Pedido pedido, String idIfood) {
        Entrega novaEntrega = new Entrega();
        novaEntrega.setEntregador(entregadorAtual);
        novaEntrega.setNumeroPedido(pedido.getNumeroPedido());
        novaEntrega.setIdIfood(idIfood.isEmpty() ? null : idIfood);
        novaEntrega.setDataHora(LocalDateTime.now());
        novaEntrega.setPedido(pedido);
        return novaEntrega;
    }

    private Optional<Pedido> buscarPedido(String numeroPedidoOuId) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findByNumeroPedido(numeroPedidoOuId);

        if (pedidoOpt.isEmpty() && numeroPedidoOuId.matches("\\d+")) {
            try {
                Long pedidoId = Long.parseLong(numeroPedidoOuId);
                pedidoOpt = pedidoRepository.findById(pedidoId);
            } catch (NumberFormatException e) {
            }
        }

        return pedidoOpt;
    }

    private boolean validarPedidoParaEntrega(Pedido pedido) {
        if (!pedido.isEntrega()) {
            mostrarErro("Pedido não é para entrega", "Este pedido é para retirada na loja.\nNão pode ser associado a entregador.");
            txtNumeroPedidoPopup.requestFocus();
            txtNumeroPedidoPopup.selectAll();
            return false;
        }

        if (pedido.getEntregadorAssociado() != null && !pedido.getEntregadorAssociado().getId().equals(entregadorAtual.getId())) {
            mostrarErro("Pedido já tem entregador", "Este pedido já está com: " + pedido.getEntregadorAssociado().getNome());
            txtNumeroPedidoPopup.requestFocus();
            txtNumeroPedidoPopup.selectAll();
            return false;
        }

        return true;
    }

    private Entrega criarNovaEntrega(Pedido pedido, String idIfood) {
        Entrega novaEntrega = new Entrega();
        novaEntrega.setEntregador(entregadorAtual);
        novaEntrega.setNumeroPedido(pedido.getNumeroPedido());
        novaEntrega.setIdIfood(idIfood.isEmpty() ? null : idIfood);
        novaEntrega.setDataHora(LocalDateTime.now());
        novaEntrega.setPedido(pedido);
        return novaEntrega;
    }

    private void atualizarPedido(Pedido pedido) {
        pedido.setEntregadorAssociado(entregadorAtual);
        pedido.setEntregador(entregadorAtual.getNome());
        pedido.setStatusEntrega("EM_ROTA");
        pedidoRepository.save(pedido);
    }

    private void atualizarEntregador() {
        if (entregadorAtual.getStatus() == StatusEntregador.DISPONIVEL) {
            entregadorAtual.setStatus(StatusEntregador.EM_ENTREGA);
        }

        List<Entrega> entregasHojeAtualizadas = buscarEntregasHoje(entregadorAtual);
        entregadorAtual.setEntregasHoje(entregasHojeAtualizadas.size());
        entregadorRepository.save(entregadorAtual);
    }

    private void atualizarCard(Long entregadorId) {
        Platform.runLater(() -> {
            try {
                Optional<Entregador> entregadorOpt = entregadorRepository.findById(entregadorId);
                if (entregadorOpt.isEmpty()) {
                    atualizarTelaCompleta();
                    return;
                }

                Entregador entregadorAtualizado = entregadorOpt.get();

                List<Entregador> todosEntregadores = entregadorRepository.findAllByOrderByNomeAsc();
                atualizarEstatisticas(todosEntregadores);

                boolean encontrou = false;
                for (int i = 0; i < containerEntregadores.getChildren().size(); i++) {
                    Node node = containerEntregadores.getChildren().get(i);
                    if (node instanceof VBox) {
                        VBox card = (VBox) node;
                        if (cardContainsEntregador(card, entregadorAtualizado.getNome())) {
                            VBox novoCard = criarCardEntregadorCompleto(entregadorAtualizado);
                            containerEntregadores.getChildren().set(i, novoCard);
                            encontrou = true;
                            break;
                        }
                    }
                }

                if (!encontrou) {
                    atualizarTelaCompleta();
                }

            } catch (Exception e) {
                e.printStackTrace();
                atualizarTelaCompleta();
            }
        });
    }

    private boolean cardContainsEntregador(VBox card, String nomeEntregador) {
        try {
            for (Node node : card.getChildren()) {
                if (node instanceof HBox) {
                    HBox hbox = (HBox) node;
                    for (Node child : hbox.getChildren()) {
                        if (child instanceof VBox) {
                            VBox vbox = (VBox) child;
                            for (Node labelNode : vbox.getChildren()) {
                                if (labelNode instanceof Label) {
                                    Label label = (Label) labelNode;
                                    if (label.getText().equals(nomeEntregador)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
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
            e.printStackTrace();
        }
    }

    private void atualizarEstatisticas(List<Entregador> entregadores) {
        try {
            final long total = entregadores.size();
            final long ativos = entregadores.stream().filter(e -> e.getStatus() != StatusEntregador.INATIVO).count();

            long entregasHojeCalc = 0;
            LocalDate hoje = LocalDate.now();

            for (Entregador entregador : entregadores) {
                if (entregador.getStatus() != StatusEntregador.INATIVO) {
                    try {
                        List<Entrega> entregasHoje = buscarEntregasHoje(entregador);
                        entregasHojeCalc += entregasHoje.size();
                    } catch (Exception e) {
                    }
                }
            }

            final long entregasHojeTotal = entregasHojeCalc;

            Platform.runLater(() -> {
                if (labelTotalEntregadores != null) {
                    labelTotalEntregadores.setText(String.valueOf(total));
                }
                if (labelAtivosHoje != null) {
                    labelAtivosHoje.setText(String.valueOf(ativos));
                }
                if (labelEntregasHoje != null) {
                    labelEntregasHoje.setText(String.valueOf(entregasHojeTotal));
                }
            });

        } catch (Exception e) {
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
        cardCompleto.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 15;");

        HBox topBox = criarTopBoxEntregador(entregador);
        cardCompleto.getChildren().add(topBox);

        List<Entrega> entregasHoje = buscarEntregasHoje(entregador);

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

        long entregasHojeCount = entregador.getEntregasHoje();

        Label entregasCount = new Label(String.valueOf(entregasHojeCount));
        entregasCount.setTextFill(Color.web("#111827"));
        entregasCount.setFont(Font.font("System Bold", 16));
        entregasCount.setMinWidth(20);
        entregasCount.setAlignment(Pos.CENTER);

        entregasBox.getChildren().addAll(entregasTexto, entregasCount);

        Label statusLabel = criarStatusLabel(entregador.getStatus());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnAddEntrega = criarBotaoAddEntrega(entregador);
        Button btnAcao = criarBotaoAcao(entregador);

        buttonBox.getChildren().addAll(btnAddEntrega, statusLabel, btnAcao);

        HBox rightBox = new HBox(15);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.getChildren().addAll(entregasBox, buttonBox);

        card.getChildren().addAll(iconPane, infoBox, spacer, rightBox);

        return card;
    }

    private Label criarStatusLabel(StatusEntregador status) {
        Label statusLabel = new Label(status.getDescricao().toLowerCase());

        switch (status) {
            case DISPONIVEL -> {
                statusLabel.setText("ativo");
                statusLabel.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 4 12; -fx-min-width: 56; -fx-alignment: center;");
            }
            case EM_ENTREGA -> {
                statusLabel.setText("em entrega");
                statusLabel.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 4 12; -fx-min-width: 56; -fx-alignment: center;");
            }
            case INATIVO -> statusLabel.setStyle("-fx-background-color: #6B7280; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 4 12; -fx-min-width: 56; -fx-alignment: center;");
        }

        return statusLabel;
    }

    private Button criarBotaoAddEntrega(Entregador entregador) {
        Button btnAddEntrega = new Button("+ Add Entrega");
        btnAddEntrega.setStyle("-fx-background-color: #F97316; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12; -fx-padding: 6 12;");
        btnAddEntrega.setOnAction(e -> abrirPopupAdicionarEntrega(entregador));

        if (entregador.getStatus() == StatusEntregador.INATIVO) {
            btnAddEntrega.setDisable(true);
            btnAddEntrega.setStyle("-fx-background-color: #9CA3AF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-font-size: 12; -fx-padding: 6 12;");
        }

        btnAddEntrega.setOnMouseEntered(e -> {
            if (!btnAddEntrega.isDisabled()) {
                btnAddEntrega.setStyle("-fx-background-color: #EA580C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12; -fx-padding: 6 12;");
            }
        });

        btnAddEntrega.setOnMouseExited(e -> {
            if (!btnAddEntrega.isDisabled()) {
                btnAddEntrega.setStyle("-fx-background-color: #F97316; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12; -fx-padding: 6 12;");
            }
        });

        return btnAddEntrega;
    }

    private Button criarBotaoAcao(Entregador entregador) {
        Button btnAcao = new Button();
        btnAcao.setStyle("-fx-background-color: transparent; -fx-text-fill: #374151; -fx-border-color: #D1D5DB; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 12; -fx-padding: 4 12; -fx-font-weight: normal;");
        btnAcao.setOnAction(e -> alternarStatus(entregador));

        if (entregador.getStatus() == StatusEntregador.INATIVO) {
            btnAcao.setText("Ativar");
        } else {
            btnAcao.setText("Desativar");
        }

        btnAcao.setOnMouseEntered(e -> btnAcao.setStyle("-fx-background-color: #F9FAFB; -fx-text-fill: #1F2937; -fx-border-color: #9CA3AF; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 12; -fx-padding: 4 12; -fx-font-weight: normal;"));
        btnAcao.setOnMouseExited(e -> btnAcao.setStyle("-fx-background-color: transparent; -fx-text-fill: #374151; -fx-border-color: #D1D5DB; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 12; -fx-padding: 4 12; -fx-font-weight: normal;"));

        return btnAcao;
    }

    private List<Entrega> buscarEntregasHoje(Entregador entregador) {
        try {
            LocalDate hoje = LocalDate.now();
            LocalDateTime inicioDoDia = hoje.atStartOfDay();
            LocalDateTime fimDoDia = hoje.atTime(23, 59, 59);

            return entregaRepository.findEntregasHojeByEntregadorAlt(
                    entregador, inicioDoDia, fimDoDia);

        } catch (Exception e) {
            System.err.println("Erro na query findEntregasHojeByEntregadorAlt: " + e.getMessage());
            e.printStackTrace();

            try {
                List<Entrega> todasEntregas = entregaRepository.findByEntregadorWithPedido(entregador);
                LocalDate hoje = LocalDate.now();

                return todasEntregas.stream()
                        .filter(entrega -> entrega.getDataHora().toLocalDate().equals(hoje))
                        .collect(Collectors.toList());
            } catch (Exception ex) {
                System.err.println("Erro no fallback: " + ex.getMessage());
                ex.printStackTrace();
                return new ArrayList<>();
            }
        }
    }

    private void atualizarTelaCompleta() {
        Platform.runLater(() -> {
            try {
                if (containerEntregadores != null) {
                    carregarEntregadores();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void refreshEntregadores() {
        if (containerEntregadores != null && containerEntregadores.getScene() != null) {
            carregarEntregadores();
        }
    }

    private void salvarNovoEntregador(Entregador novoEntregador) {
        try {
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

            entregadorRepository.save(novoEntregador);

            if (containerEntregadores != null) {
                Platform.runLater(() -> {
                    try {
                        carregarEntregadores();

                        if (txtNomeCompletoPopup != null && txtNomeCompletoPopup.getScene() != null) {
                            Stage stage = (Stage) txtNomeCompletoPopup.getScene().getWindow();
                            stage.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
            sucesso.setTitle("Sucesso");
            sucesso.setHeaderText(null);
            sucesso.setContentText("Entregador cadastrado com sucesso!");
            sucesso.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao salvar", e.getMessage());
        }
    }
}