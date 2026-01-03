package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.PdvGaleteriaApplication;
import com.example.pdv_galeteria.dto.ProdutoMaisVendidoDTO;
import com.example.pdv_galeteria.dto.RelatorioVendasDTO;
import com.example.pdv_galeteria.model.UsuarioSessao;
import com.example.pdv_galeteria.service.RelatorioService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
public class RelatoriosController {

    @FXML private Label labelNomeUsuario;

    @FXML private Label labelTotalHoje;
    @FXML private Label labelPedidosHoje;
    @FXML private Label labelTotalSemana;
    @FXML private Label labelPedidosSemana;
    @FXML private Label labelTotalMes;
    @FXML private Label labelPedidosMes;

    @FXML private Label labelProduto1Nome;
    @FXML private Label labelProduto1Quantidade;
    @FXML private Label labelProduto1Valor;
    @FXML private Label labelProduto2Nome;
    @FXML private Label labelProduto2Quantidade;
    @FXML private Label labelProduto2Valor;
    @FXML private Label labelProduto3Nome;
    @FXML private Label labelProduto3Quantidade;
    @FXML private Label labelProduto3Valor;

    @Autowired
    private UsuarioSessao usuarioSessao;

    @Autowired
    private RelatorioService relatorioService;

    @FXML
    public void initialize() {
        System.out.println("RelatoriosController inicializando...");

        if (labelNomeUsuario != null && usuarioSessao != null) {
            labelNomeUsuario.setText(usuarioSessao.getNomeUsuario());
            System.out.println("Nome do usuário definido: " + usuarioSessao.getNomeUsuario());
        }

        Platform.runLater(() -> {
            try {
                carregarDadosRelatorios();
                System.out.println("Dados dos relatórios carregados com sucesso!");
            } catch (Exception e) {
                System.err.println("Erro ao carregar dados: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void carregarDadosRelatorios() {
        try {
            System.out.println("Iniciando carregamento de dados dos relatórios...");

            carregarRelatorioHoje();
            carregarRelatorioSemana();
            carregarRelatorioMes();

            carregarProdutosMaisVendidos();

        } catch (Exception e) {
            System.err.println("Erro crítico ao carregar dados dos relatórios: " + e.getMessage());
            e.printStackTrace();

            definirValoresPadrao();
        }
    }

    private void carregarRelatorioHoje() {
        try {
            System.out.println("Carregando relatório de hoje...");
            RelatorioVendasDTO relatorio = relatorioService.getRelatorioVendasHoje();
            System.out.println("Relatório de hoje carregado: " + relatorio);

            if (labelTotalHoje != null) {
                labelTotalHoje.setText(relatorio.getTotalVendasFormatado());
            }
            if (labelPedidosHoje != null) {
                labelPedidosHoje.setText(relatorio.getTotalPedidosFormatado());
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar relatório de hoje: " + e.getMessage());
            if (labelTotalHoje != null) labelTotalHoje.setText("R$ 0,00");
            if (labelPedidosHoje != null) labelPedidosHoje.setText("0 pedidos");
        }
    }

    private void carregarRelatorioSemana() {
        try {
            System.out.println("Carregando relatório da semana...");
            RelatorioVendasDTO relatorio = relatorioService.getRelatorioVendasSemana();
            System.out.println("Relatório da semana carregado: " + relatorio);

            if (labelTotalSemana != null) {
                labelTotalSemana.setText(relatorio.getTotalVendasFormatado());
            }
            if (labelPedidosSemana != null) {
                labelPedidosSemana.setText(relatorio.getTotalPedidosFormatado());
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar relatório da semana: " + e.getMessage());
            if (labelTotalSemana != null) labelTotalSemana.setText("R$ 0,00");
            if (labelPedidosSemana != null) labelPedidosSemana.setText("0 pedidos");
        }
    }

    private void carregarRelatorioMes() {
        try {
            System.out.println("Carregando relatório do mês...");
            RelatorioVendasDTO relatorio = relatorioService.getRelatorioVendasMes();
            System.out.println("Relatório do mês carregado: " + relatorio);

            if (labelTotalMes != null) {
                labelTotalMes.setText(relatorio.getTotalVendasFormatado());
            }
            if (labelPedidosMes != null) {
                labelPedidosMes.setText(relatorio.getTotalPedidosFormatado());
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar relatório do mês: " + e.getMessage());
            if (labelTotalMes != null) labelTotalMes.setText("R$ 0,00");
            if (labelPedidosMes != null) labelPedidosMes.setText("0 pedidos");
        }
    }

    private void carregarProdutosMaisVendidos() {
        try {
            System.out.println("Carregando produtos mais vendidos...");
            List<ProdutoMaisVendidoDTO> produtos = relatorioService.getProdutosMaisVendidos(3);
            System.out.println("Produtos mais vendidos encontrados: " + produtos.size());

            if (produtos.isEmpty()) {
                System.out.println("Nenhum produto vendido encontrado");
                definirProdutosVazios();
                return;
            }

            if (produtos.size() >= 1) {
                ProdutoMaisVendidoDTO p1 = produtos.get(0);
                System.out.println("Produto 1: " + p1);

                if (labelProduto1Nome != null) labelProduto1Nome.setText(p1.getNomeProduto());
                if (labelProduto1Quantidade != null) labelProduto1Quantidade.setText(p1.getQuantidadeVendidaFormatada());
                if (labelProduto1Valor != null) labelProduto1Valor.setText(p1.getValorTotalFormatado());
            } else {
                definirProdutoVazio(1);
            }

            if (produtos.size() >= 2) {
                ProdutoMaisVendidoDTO p2 = produtos.get(1);
                System.out.println("Produto 2: " + p2);

                if (labelProduto2Nome != null) labelProduto2Nome.setText(p2.getNomeProduto());
                if (labelProduto2Quantidade != null) labelProduto2Quantidade.setText(p2.getQuantidadeVendidaFormatada());
                if (labelProduto2Valor != null) labelProduto2Valor.setText(p2.getValorTotalFormatado());
            } else {
                definirProdutoVazio(2);
            }

            if (produtos.size() >= 3) {
                ProdutoMaisVendidoDTO p3 = produtos.get(2);
                System.out.println("Produto 3: " + p3);

                if (labelProduto3Nome != null) labelProduto3Nome.setText(p3.getNomeProduto());
                if (labelProduto3Quantidade != null) labelProduto3Quantidade.setText(p3.getQuantidadeVendidaFormatada());
                if (labelProduto3Valor != null) labelProduto3Valor.setText(p3.getValorTotalFormatado());
            } else {
                definirProdutoVazio(3);
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar produtos mais vendidos: " + e.getMessage());
            e.printStackTrace();
            definirProdutosVazios();
        }
    }

    private void definirProdutoVazio(int numero) {
        switch (numero) {
            case 1:
                if (labelProduto1Nome != null) labelProduto1Nome.setText("Nenhum produto");
                if (labelProduto1Quantidade != null) labelProduto1Quantidade.setText("0 unidades");
                if (labelProduto1Valor != null) labelProduto1Valor.setText("R$ 0,00");
                break;
            case 2:
                if (labelProduto2Nome != null) labelProduto2Nome.setText("Nenhum produto");
                if (labelProduto2Quantidade != null) labelProduto2Quantidade.setText("0 unidades");
                if (labelProduto2Valor != null) labelProduto2Valor.setText("R$ 0,00");
                break;
            case 3:
                if (labelProduto3Nome != null) labelProduto3Nome.setText("Nenhum produto");
                if (labelProduto3Quantidade != null) labelProduto3Quantidade.setText("0 unidades");
                if (labelProduto3Valor != null) labelProduto3Valor.setText("R$ 0,00");
                break;
        }
    }

    private void definirProdutosVazios() {
        definirProdutoVazio(1);
        definirProdutoVazio(2);
        definirProdutoVazio(3);
    }

    private void definirValoresPadrao() {
        if (labelTotalHoje != null) labelTotalHoje.setText("R$ 0,00");
        if (labelPedidosHoje != null) labelPedidosHoje.setText("0 pedidos");
        if (labelTotalSemana != null) labelTotalSemana.setText("R$ 0,00");
        if (labelPedidosSemana != null) labelPedidosSemana.setText("0 pedidos");
        if (labelTotalMes != null) labelTotalMes.setText("R$ 0,00");
        if (labelPedidosMes != null) labelPedidosMes.setText("0 pedidos");

        definirProdutosVazios();
    }

    @FXML
    private void handleExportarPDF() {
        try {
            System.out.println("Exportando relatório para PDF...");

            LocalDate hoje = LocalDate.now();
            LocalDate inicioMes = LocalDate.of(hoje.getYear(), hoje.getMonth(), 1);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String nomeArquivo = "relatorio_vendas_" + hoje.format(formatter) + ".pdf";
            java.nio.file.Path caminho = java.nio.file.Paths.get("relatorios", nomeArquivo);

            java.nio.file.Files.createDirectories(caminho.getParent());

            relatorioService.gerarRelatorioVendas(inicioMes, hoje, caminho);

            mostrarMensagemSucesso("Relatório exportado com sucesso!\nArquivo: " + caminho.toAbsolutePath());

        } catch (Exception e) {
            System.err.println("Erro ao exportar PDF: " + e.getMessage());
            e.printStackTrace();
            mostrarMensagemErro("Erro ao exportar PDF: " + e.getMessage());
        }
    }

    @FXML
    private void handleVerDetalhesSemana() {
        try {
            RelatorioVendasDTO relatorio = relatorioService.getRelatorioVendasSemana();
            mostrarDetalhesRelatorio("Relatório de Vendas - Semana", relatorio);
        } catch (Exception e) {
            mostrarMensagemErro("Erro ao carregar detalhes: " + e.getMessage());
        }
    }

    @FXML
    private void handleVerDetalhesMes() {
        try {
            RelatorioVendasDTO relatorio = relatorioService.getRelatorioVendasMes();
            mostrarDetalhesRelatorio("Relatório de Vendas - Mês", relatorio);
        } catch (Exception e) {
            mostrarMensagemErro("Erro ao carregar detalhes: " + e.getMessage());
        }
    }

    private void mostrarDetalhesRelatorio(String titulo, RelatorioVendasDTO relatorio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText("Detalhes do Relatório");
        alert.setContentText(
                titulo + "\n\n" +
                        "Total de Vendas: " + relatorio.getTotalVendasFormatado() + "\n" +
                        "Total de Pedidos: " + relatorio.getTotalPedidosFormatado() + "\n" +
                        "Total de Itens: " + relatorio.getQuantidadeItens() + "\n" +
                        "Valor Médio por Pedido: " + relatorio.getValorMedioPedidoFormatado()
        );
        alert.showAndWait();
    }

    @FXML
    private void sairParaLogin() {
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
            popupStage.initOwner(currentStage);
            popupStage.setResizable(false);
            popupStage.centerOnScreen();

            popupStage.showAndWait();

            if (controller.isConfirmado()) {
                voltarParaTelaLogin(currentStage);
            }
        } catch (Exception e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmação de Saída");
            alert.setHeaderText("Deseja realmente sair?");
            alert.setContentText("Você será redirecionado para a tela de login.");

            Optional<ButtonType> resultado = alert.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                if (usuarioSessao != null) {
                    usuarioSessao.logout();
                }
                voltarParaTelaLogin((Stage) alert.getDialogPane().getScene().getWindow());
            }
        }
    }

    private void voltarParaTelaLogin(Stage currentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaLogin.fxml"));

            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();

            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Login");
            currentStage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void abrirTelaDashboard(ActionEvent actionEvent) {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaDashBoard.fxml", "Dashboard", actionEvent);
    }

    public void abrirTelaEstoque(ActionEvent actionEvent) {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaProdutos.fxml", "Estoque", actionEvent);
    }

    @FXML
    private void abrirTelaCaixa(ActionEvent event) {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaCaixa.fxml", "Controle de Caixa", event);
    }

    public void abrirTelaVendas(ActionEvent actionEvent) {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaRegistroPedido.fxml", "Registro de Pedidos", actionEvent);
    }

    public void abrirTelaEntregadores(ActionEvent actionEvent) {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaEntregadores.fxml", "Entregadores", actionEvent);
    }

    public void abrirTelaConfiguracoes(ActionEvent actionEvent) {
        navegarParaTela("/com/example/pdv_galeteria/Frontend/views/TelaConfiguracao.fxml", "Configurações", actionEvent);
    }

    private void navegarParaTela(String fxmlPath, String titulo, ActionEvent actionEvent) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new RuntimeException("Arquivo FXML não encontrado: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("Erro ao navegar para " + titulo + ": " + e.getMessage());
            e.printStackTrace();
            mostrarMensagemErro("Erro ao abrir " + titulo + ": " + e.getMessage());
        }
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

    private void mostrarMensagemSucesso(String mensagem) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sucesso");
            alert.setHeaderText(null);
            alert.setContentText(mensagem);
            alert.showAndWait();
        });
    }

    @FXML
    private void abrirRelatorioHojeDetalhado() {
        try {
            System.out.println("Abrindo tela de relatório de hoje...");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pdv_galeteria/Frontend/views/TelaRelatorioHoje.fxml"));

            loader.setControllerFactory(PdvGaleteriaApplication.getSpringContext()::getBean);

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Relatório de Vendas - Hoje");
            stage.setResizable(false);
            stage.centerOnScreen();

            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            stage.show();

        } catch (Exception e) {
            System.err.println("Erro ao abrir relatório de hoje: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Não foi possível abrir o relatório");
            alert.setContentText("Erro: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleVerDetalhesHoje() {
        abrirRelatorioHojeDetalhado();
    }
}