package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.dto.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface RelatorioService {

    RelatorioVendasDTO getRelatorioVendasHoje();
    RelatorioVendasDTO getRelatorioVendasSemana();
    RelatorioVendasDTO getRelatorioVendasMes();
    RelatorioVendasDTO getRelatorioVendasPorPeriodo(LocalDate dataInicio, LocalDate dataFim);

    RelatorioVendasDTO getRelatorioVendasPeriodo(LocalDate inicio, LocalDate fim);
    Map<LocalDate, RelatorioVendasDTO> getVendasPorDia(LocalDate inicio, LocalDate fim);
    Map<String, BigDecimal> getDistribuicaoPagamentoPeriodo(LocalDate inicio, LocalDate fim);

    List<ProdutoMaisVendidoDTO> getProdutosMaisVendidos(int limite);
    List<ProdutoMaisVendidoDTO> getProdutosMaisVendidosPorPeriodo(LocalDate dataInicio, LocalDate dataFim, int limite);

    List<PedidoResumoDTO> getUltimasVendasHoje(int limite);
    List<PedidoResumoDTO> getUltimasVendasPorPeriodo(LocalDate inicio, LocalDate fim, int limite);

    Map<String, BigDecimal> getDistribuicaoPagamentoHoje();

    List<RelatorioMovimentoCaixaDTO> getMovimentosCaixaPorPeriodo(LocalDateTime inicio, LocalDateTime fim);

    void gerarRelatorioVendas(LocalDate dataInicio, LocalDate dataFim, Path caminhoArquivo);
}