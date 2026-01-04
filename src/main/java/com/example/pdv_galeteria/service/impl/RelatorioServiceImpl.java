package com.example.pdv_galeteria.service.impl;

import com.example.pdv_galeteria.dto.PedidoResumoDTO;
import com.example.pdv_galeteria.dto.RelatorioVendasDTO;
import com.example.pdv_galeteria.dto.ProdutoMaisVendidoDTO;
import com.example.pdv_galeteria.dto.RelatorioMovimentoCaixaDTO;
import com.example.pdv_galeteria.model.ItemPedido;
import com.example.pdv_galeteria.model.Pedido;
import com.example.pdv_galeteria.model.Produto;
import com.example.pdv_galeteria.model.StatusPedido;
import com.example.pdv_galeteria.repository.ItemPedidoRepository;
import com.example.pdv_galeteria.repository.PedidoRepository;
import com.example.pdv_galeteria.repository.MovimentoCaixaRepository;
import com.example.pdv_galeteria.service.RelatorioService;
import com.example.pdv_galeteria.util.GeradorRelatorioPDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RelatorioServiceImpl implements RelatorioService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Autowired
    private MovimentoCaixaRepository movimentoCaixaRepository;

    @Override
    public void gerarRelatorioVendas(LocalDate inicio, LocalDate fim, Path caminhoArquivo) {
        try {
            RelatorioVendasDTO relatorio = getRelatorioVendasPorPeriodo(inicio, fim);
            List<PedidoResumoDTO> ultimasVendas = getUltimasVendasPorPeriodo(inicio, fim, 15);
            List<ProdutoMaisVendidoDTO> produtosMaisVendidos = getProdutosMaisVendidosPorPeriodo(inicio, fim, 10);

            Map<String, BigDecimal> distribuicaoPagamento = calcularDistribuicaoPagamentoPorPeriodo(inicio, fim);

            GeradorRelatorioPDF.gerarRelatorioVendas(
                    relatorio,
                    ultimasVendas,
                    produtosMaisVendidos,
                    distribuicaoPagamento,
                    inicio,
                    fim,
                    caminhoArquivo
            );

            System.out.println("Relatório PDF gerado com sucesso: " + caminhoArquivo.toAbsolutePath());

        } catch (Exception e) {
            System.err.println("Erro ao gerar relatório PDF: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao gerar relatório de vendas: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PedidoResumoDTO> getUltimasVendasPorPeriodo(LocalDate inicio, LocalDate fim, int limite) {
        try {
            LocalDateTime inicioDateTime = inicio.atStartOfDay();
            LocalDateTime fimDateTime = fim.atTime(23, 59, 59);

            List<Pedido> pedidos = pedidoRepository.findByCriadoEmBetween(inicioDateTime, fimDateTime);

            if (pedidos == null || pedidos.isEmpty()) {
                return new ArrayList<>();
            }

            return pedidos.stream()
                    .filter(p -> p.getStatus() != StatusPedido.CANCELADO)
                    .sorted(Comparator.comparing(Pedido::getCriadoEm).reversed())
                    .limit(limite)
                    .map(p -> {
                        String produtos = "Sem produtos";
                        if (p.getItens() != null && !p.getItens().isEmpty()) {
                            produtos = p.getItens().stream()
                                    .map(item -> {
                                        if (item.getProduto() != null) {
                                            return item.getProduto();
                                        }
                                        return "Produto não especificado";
                                    })
                                    .collect(Collectors.joining(", "));
                        }

                        String clienteNome = p.getCliente() != null ? p.getCliente() : "Cliente não identificado";

                        String formaPagamento = "Não informado";
                        if (p.getFormaPagamento() != null) {
                            formaPagamento = formatarFormaPagamento(p.getFormaPagamento());
                        }

                        BigDecimal valor = BigDecimal.valueOf(p.getTotal() != null ? p.getTotal() : 0.0);

                        return new PedidoResumoDTO(
                                p.getId(),
                                clienteNome,
                                produtos,
                                valor,
                                formaPagamento,
                                p.getCriadoEm()
                        );
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Erro ao buscar últimas vendas por período: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private PedidoResumoDTO toPedidoResumoDTO(Pedido pedido) {
        String produtos = "Sem produtos";
        if (pedido.getItens() != null && !pedido.getItens().isEmpty()) {
            produtos = pedido.getItens().stream()
                    .map(item -> item.getProduto() != null ? item.getProduto() : "Produto não especificado")
                    .collect(Collectors.joining(", "));
        }

        return new PedidoResumoDTO(
                pedido.getId(),
                pedido.getCliente() != null ? pedido.getCliente() : "Cliente não identificado",
                produtos,
                BigDecimal.valueOf(pedido.getTotal() != null ? pedido.getTotal() : 0.0),
                pedido.getFormaPagamento() != null ? formatarFormaPagamento(pedido.getFormaPagamento()) : "Não informado",
                pedido.getCriadoEm()
        );
    }

    public Map<String, BigDecimal> getDistribuicaoPagamentoPorPeriodo(LocalDate inicio, LocalDate fim) {
        return calcularDistribuicaoPagamentoPorPeriodo(inicio, fim);
    }

    @Override
    public RelatorioVendasDTO getRelatorioVendasHoje() {
        try {
            System.out.println("=== DEBUG RELATÓRIO HOJE ===");

            LocalDate hoje = LocalDate.now();
            System.out.println("DEBUG: Data hoje = " + hoje);

            LocalDateTime inicio = hoje.atStartOfDay();
            LocalDateTime fim = hoje.atTime(LocalTime.MAX);
            System.out.println("DEBUG: Período = " + inicio + " até " + fim);

            List<Pedido> pedidos = pedidoRepository.findByCriadoEmBetween(inicio, fim);
            System.out.println("DEBUG: Total pedidos encontrados = " + pedidos.size());

            for (Pedido p : pedidos) {
                System.out.println("DEBUG Pedido #" + p.getId() +
                        " - Status: " + p.getStatus() +
                        " - Total: R$ " + p.getTotal() +
                        " - Data: " + p.getCriadoEm() +
                        " - Cliente: " + p.getCliente());
            }

            List<Pedido> pedidosAtivos = pedidos.stream()
                    .filter(p -> p.getStatus() != StatusPedido.CANCELADO)
                    .collect(Collectors.toList());

            System.out.println("DEBUG: Pedidos ativos (não cancelados) = " + pedidosAtivos.size());

            if (pedidosAtivos.isEmpty()) {
                System.out.println("DEBUG: Nenhum pedido ativo encontrado!");
                return new RelatorioVendasDTO(BigDecimal.ZERO, 0, 0);
            }

            BigDecimal totalVendas = BigDecimal.ZERO;
            int totalItens = 0;

            for (Pedido pedido : pedidosAtivos) {
                BigDecimal pedidoTotal = BigDecimal.valueOf(pedido.getTotal() != null ? pedido.getTotal() : 0.0);
                totalVendas = totalVendas.add(pedidoTotal);

                if (pedido.getItens() != null) {
                    totalItens += pedido.getItens().size();
                }
            }

            System.out.println("DEBUG: Total vendas = R$ " + totalVendas);
            System.out.println("DEBUG: Total pedidos ativos = " + pedidosAtivos.size());
            System.out.println("DEBUG: Total itens = " + totalItens);

            RelatorioVendasDTO resultado = new RelatorioVendasDTO(totalVendas, pedidosAtivos.size(), totalItens);
            System.out.println("DEBUG: Resultado final = " + resultado);

            return resultado;

        } catch (Exception e) {
            System.err.println("ERRO CRÍTICO em getRelatorioVendasHoje: " + e.getMessage());
            e.printStackTrace();
            return new RelatorioVendasDTO(BigDecimal.ZERO, 0, 0);
        }
    }

    @Override
    public RelatorioVendasDTO getRelatorioVendasSemana() {
        try {
            LocalDate hoje = LocalDate.now();
            LocalDate inicioSemana = hoje.minusDays(hoje.getDayOfWeek().getValue() - 1);
            return getRelatorioVendasPorPeriodo(inicioSemana, hoje);
        } catch (Exception e) {
            System.err.println("Erro ao obter relatório da semana: " + e.getMessage());
            return new RelatorioVendasDTO(BigDecimal.ZERO, 0, 0);
        }
    }

    @Override
    public RelatorioVendasDTO getRelatorioVendasMes() {
        try {
            LocalDate hoje = LocalDate.now();
            LocalDate inicioMes = LocalDate.of(hoje.getYear(), hoje.getMonth(), 1);
            return getRelatorioVendasPorPeriodo(inicioMes, hoje);
        } catch (Exception e) {
            System.err.println("Erro ao obter relatório do mês: " + e.getMessage());
            return new RelatorioVendasDTO(BigDecimal.ZERO, 0, 0);
        }
    }

    @Override
    public RelatorioVendasDTO getRelatorioVendasPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        try {
            LocalDateTime inicio = dataInicio.atStartOfDay();
            LocalDateTime fim = dataFim.atTime(LocalTime.MAX);

            List<Pedido> pedidos = pedidoRepository.findByCriadoEmBetween(inicio, fim);

            if (pedidos == null || pedidos.isEmpty()) {
                return new RelatorioVendasDTO(BigDecimal.ZERO, 0, 0);
            }

            List<Pedido> pedidosAtivos = pedidos.stream()
                    .filter(pedido -> pedido.getStatus() != StatusPedido.CANCELADO)
                    .collect(Collectors.toList());

            if (pedidosAtivos.isEmpty()) {
                return new RelatorioVendasDTO(BigDecimal.ZERO, 0, 0);
            }

            BigDecimal totalVendas = BigDecimal.ZERO;
            int totalItens = 0;

            for (Pedido pedido : pedidosAtivos) {
                BigDecimal pedidoTotal = BigDecimal.valueOf(pedido.getTotal() != null ? pedido.getTotal() : 0.0);
                totalVendas = totalVendas.add(pedidoTotal);

                if (pedido.getItens() != null) {
                    totalItens += pedido.getItens().size();
                }
            }

            return new RelatorioVendasDTO(totalVendas, pedidosAtivos.size(), totalItens);

        } catch (Exception e) {
            System.err.println("Erro ao obter relatório por período: " + e.getMessage());
            e.printStackTrace();
            return new RelatorioVendasDTO(BigDecimal.ZERO, 0, 0);
        }
    }

    @Override
    public List<ProdutoMaisVendidoDTO> getProdutosMaisVendidos(int limite) {
        try {
            LocalDate hoje = LocalDate.now();
            LocalDate inicioMes = LocalDate.of(hoje.getYear(), hoje.getMonth(), 1);
            return getProdutosMaisVendidosPorPeriodo(inicioMes, hoje, limite);
        } catch (Exception e) {
            System.err.println("Erro ao obter produtos mais vendidos: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<ProdutoMaisVendidoDTO> getProdutosMaisVendidosPorPeriodo(
            LocalDate dataInicio, LocalDate dataFim, int limite) {
        try {
            LocalDateTime inicio = dataInicio.atStartOfDay();
            LocalDateTime fim = dataFim.atTime(LocalTime.MAX);

            List<ItemPedido> itens = itemPedidoRepository.findByPedido_CriadoEmBetween(inicio, fim);

            if (itens == null || itens.isEmpty()) {
                return new ArrayList<>();
            }

            List<ItemPedido> itensAtivos = new ArrayList<>();
            for (ItemPedido item : itens) {
                if (item.getPedido() != null &&
                        item.getPedido().getStatus() != StatusPedido.CANCELADO &&
                        item.getProduto() != null) {
                    itensAtivos.add(item);
                }
            }

            if (itensAtivos.isEmpty()) {
                return new ArrayList<>();
            }

            Map<String, ProdutoDados> dadosProdutos = new HashMap<>();

            for (ItemPedido item : itensAtivos) {
                String nomeProduto = item.getProduto();

                ProdutoDados dados = dadosProdutos.get(nomeProduto);
                if (dados == null) {
                    dados = new ProdutoDados(
                            nomeProduto,
                            "Sem Categoria"
                    );
                    dadosProdutos.put(nomeProduto, dados);
                }

                dados.quantidadeTotal += item.getQuantidade();

                BigDecimal precoUnitario = BigDecimal.valueOf(
                        item.getPrecoUnitario() != null ? item.getPrecoUnitario() : 0.0
                );
                BigDecimal valorItem = precoUnitario.multiply(
                        BigDecimal.valueOf(item.getQuantidade())
                );
                dados.valorTotal = dados.valorTotal.add(valorItem);
            }

            List<ProdutoMaisVendidoDTO> resultado = new ArrayList<>();
            for (ProdutoDados dados : dadosProdutos.values()) {
                resultado.add(new ProdutoMaisVendidoDTO(
                        dados.nomeProduto,
                        dados.categoria,
                        dados.quantidadeTotal,
                        dados.valorTotal
                ));
            }

            return resultado.stream()
                    .sorted(Comparator.comparingInt(ProdutoMaisVendidoDTO::getQuantidadeVendida).reversed())
                    .limit(limite)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Erro ao obter produtos mais vendidos por período: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<PedidoResumoDTO> getUltimasVendasHoje(int limite) {
        try {
            LocalDate hoje = LocalDate.now();
            LocalDateTime inicio = hoje.atStartOfDay();
            LocalDateTime fim = hoje.atTime(LocalTime.MAX);

            List<Pedido> pedidos = pedidoRepository.findByCriadoEmBetween(inicio, fim);

            if (pedidos == null || pedidos.isEmpty()) {
                return new ArrayList<>();
            }

            return pedidos.stream()
                    .filter(p -> p.getStatus() != StatusPedido.CANCELADO)
                    .sorted(Comparator.comparing(Pedido::getCriadoEm).reversed())
                    .limit(limite)
                    .map(p -> {
                        String produtos = "Sem produtos";
                        if (p.getItens() != null && !p.getItens().isEmpty()) {
                            produtos = p.getItens().stream()
                                    .map(item -> {
                                        if (item.getProduto() != null) {
                                            return item.getProduto();
                                        }
                                        return "Produto não especificado";
                                    })
                                    .collect(Collectors.joining(", "));
                        }

                        String clienteNome = p.getCliente() != null ? p.getCliente() : "Cliente não identificado";

                        String formaPagamento = "Não informado";
                        if (p.getFormaPagamento() != null) {
                            formaPagamento = formatarFormaPagamento(p.getFormaPagamento());
                        }

                        BigDecimal valor = BigDecimal.valueOf(p.getTotal() != null ? p.getTotal() : 0.0);

                        return new PedidoResumoDTO(
                                p.getId(),
                                clienteNome,
                                produtos,
                                valor,
                                formaPagamento,
                                p.getCriadoEm()
                        );
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Erro ao buscar últimas vendas: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, BigDecimal> getDistribuicaoPagamentoHoje() {
        try {
            LocalDate hoje = LocalDate.now();
            LocalDateTime inicio = hoje.atStartOfDay();
            LocalDateTime fim = hoje.atTime(LocalTime.MAX);

            List<Pedido> pedidos = pedidoRepository.findByCriadoEmBetween(inicio, fim);

            if (pedidos == null || pedidos.isEmpty()) {
                return criarMapaVazioDistribuicao();
            }

            List<Pedido> pedidosAtivos = pedidos.stream()
                    .filter(p -> p.getStatus() != StatusPedido.CANCELADO)
                    .collect(Collectors.toList());

            if (pedidosAtivos.isEmpty()) {
                return criarMapaVazioDistribuicao();
            }

            Map<String, BigDecimal> distribuicaoValores = new HashMap<>();
            BigDecimal totalGeral = BigDecimal.ZERO;

            for (Pedido pedido : pedidosAtivos) {
                if (pedido.getFormaPagamento() != null && !pedido.getFormaPagamento().trim().isEmpty()) {
                    String formaPagamento = formatarFormaPagamento(pedido.getFormaPagamento());
                    BigDecimal valor = BigDecimal.valueOf(pedido.getTotal() != null ? pedido.getTotal() : 0.0);

                    BigDecimal valorAtual = distribuicaoValores.getOrDefault(formaPagamento, BigDecimal.ZERO);
                    distribuicaoValores.put(formaPagamento, valorAtual.add(valor));

                    totalGeral = totalGeral.add(valor);
                }
            }

            Map<String, BigDecimal> percentuais = new HashMap<>();

            String[] formasPagamento = {"PIX", "CARTÃO CRÉDITO", "CARTÃO DÉBITO", "DINHEIRO"};
            for (String forma : formasPagamento) {
                percentuais.put(forma, BigDecimal.ZERO);
            }

            if (totalGeral.compareTo(BigDecimal.ZERO) > 0) {
                for (Map.Entry<String, BigDecimal> entry : distribuicaoValores.entrySet()) {
                    BigDecimal percentual = entry.getValue()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(totalGeral, 2, RoundingMode.HALF_UP);
                    percentuais.put(entry.getKey(), percentual);
                }
            }

            return percentuais;

        } catch (Exception e) {
            System.err.println("Erro ao calcular distribuição de pagamento: " + e.getMessage());
            e.printStackTrace();
            return criarMapaVazioDistribuicao();
        }
    }

    @Override
    public List<RelatorioMovimentoCaixaDTO> getMovimentosCaixaPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        try {
            return movimentoCaixaRepository.buscarMovimentosParaRelatorio(inicio, fim);
        } catch (Exception e) {
            System.err.println("Erro ao obter movimentos de caixa: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static class ProdutoDados {
        String nomeProduto;
        String categoria;
        int quantidadeTotal = 0;
        BigDecimal valorTotal = BigDecimal.ZERO;

        ProdutoDados(String nomeProduto, String categoria) {
            this.nomeProduto = nomeProduto;
            this.categoria = categoria;
        }
    }

    private String formatarFormaPagamento(String formaPagamento) {
        if (formaPagamento == null || formaPagamento.trim().isEmpty()) {
            return "NÃO INFORMADO";
        }

        formaPagamento = formaPagamento.toUpperCase().trim();

        if (formaPagamento.contains("CREDITO") || formaPagamento.contains("CRÉDITO") ||
                formaPagamento.contains("CRED") || formaPagamento.contains("CARTAO_CREDITO") ||
                formaPagamento.contains("CARTAOCREDITO") || formaPagamento.contains("CARTÃO CRÉDITO") ||
                formaPagamento.contains("CARTÃO DE CRÉDITO")) {
            return "CARTÃO CRÉDITO";
        }

        if (formaPagamento.contains("DEBITO") || formaPagamento.contains("DÉBITO") ||
                formaPagamento.contains("DEB") || formaPagamento.contains("CARTAO_DEBITO") ||
                formaPagamento.contains("CARTAODEBITO") || formaPagamento.contains("CARTÃO DÉBITO") ||
                formaPagamento.contains("CARTÃO DE DÉBITO")) {
            return "CARTÃO DÉBITO";
        }

        if (formaPagamento.contains("PIX")) {
            return "PIX";
        }

        if (formaPagamento.contains("DINHEIRO") || formaPagamento.contains("DINH") ||
                formaPagamento.contains("DIN")) {
            return "DINHEIRO";
        }

        return formaPagamento;
    }

    private Map<String, BigDecimal> criarMapaVazioDistribuicao() {
        Map<String, BigDecimal> mapaVazio = new HashMap<>();
        mapaVazio.put("PIX", BigDecimal.ZERO);
        mapaVazio.put("CARTÃO CRÉDITO", BigDecimal.ZERO);
        mapaVazio.put("CARTÃO DÉBITO", BigDecimal.ZERO);
        mapaVazio.put("DINHEIRO", BigDecimal.ZERO);
        return mapaVazio;
    }

    private Map<String, BigDecimal> calcularDistribuicaoPagamentoPorPeriodo(LocalDate inicio, LocalDate fim) {
        try {
            LocalDateTime inicioDateTime = inicio.atStartOfDay();
            LocalDateTime fimDateTime = fim.atTime(23, 59, 59);

            List<Pedido> pedidos = pedidoRepository.findByCriadoEmBetween(inicioDateTime, fimDateTime);

            List<Pedido> pedidosAtivos = pedidos.stream()
                    .filter(p -> p.getStatus() != StatusPedido.CANCELADO)
                    .collect(Collectors.toList());

            if (pedidosAtivos.isEmpty()) {
                return criarMapaVazioDistribuicao();
            }

            Map<String, BigDecimal> distribuicaoValores = new HashMap<>();
            BigDecimal totalGeral = BigDecimal.ZERO;

            for (Pedido pedido : pedidosAtivos) {
                if (pedido.getFormaPagamento() != null && !pedido.getFormaPagamento().trim().isEmpty()) {
                    String formaPagamento = formatarFormaPagamento(pedido.getFormaPagamento());
                    BigDecimal valor = BigDecimal.valueOf(pedido.getTotal() != null ? pedido.getTotal() : 0.0);

                    BigDecimal valorAtual = distribuicaoValores.getOrDefault(formaPagamento, BigDecimal.ZERO);
                    distribuicaoValores.put(formaPagamento, valorAtual.add(valor));

                    totalGeral = totalGeral.add(valor);
                }
            }

            Map<String, BigDecimal> percentuais = criarMapaVazioDistribuicao();

            if (totalGeral.compareTo(BigDecimal.ZERO) > 0) {
                for (Map.Entry<String, BigDecimal> entry : distribuicaoValores.entrySet()) {
                    BigDecimal percentual = entry.getValue()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(totalGeral, 2, RoundingMode.HALF_UP);
                    percentuais.put(entry.getKey(), percentual);
                }
            }

            return percentuais;

        } catch (Exception e) {
            System.err.println("Erro ao calcular distribuição de pagamento por período: " + e.getMessage());
            e.printStackTrace();
            return criarMapaVazioDistribuicao();
        }
    }

    @Override
    public RelatorioVendasDTO getRelatorioVendasPeriodo(LocalDate inicio, LocalDate fim) {
        return getRelatorioVendasPorPeriodo(inicio, fim);
    }

    @Override
    public Map<LocalDate, RelatorioVendasDTO> getVendasPorDia(LocalDate inicio, LocalDate fim) {
        Map<LocalDate, RelatorioVendasDTO> vendasPorDia = new LinkedHashMap<>();

        try {
            LocalDate dataAtual = inicio;
            while (!dataAtual.isAfter(fim)) {
                RelatorioVendasDTO relatorioDia = getRelatorioVendasPorPeriodo(dataAtual, dataAtual);
                vendasPorDia.put(dataAtual, relatorioDia);
                dataAtual = dataAtual.plusDays(1);
            }
        } catch (Exception e) {
            System.err.println("Erro ao obter vendas por dia: " + e.getMessage());
            e.printStackTrace();
        }

        return vendasPorDia;
    }

    @Override
    public Map<String, BigDecimal> getDistribuicaoPagamentoPeriodo(LocalDate inicio, LocalDate fim) {
        return calcularDistribuicaoPagamentoPorPeriodo(inicio, fim);
    }
}