package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.dto.PedidoResumoDTO;
import com.example.pdv_galeteria.dto.RelatorioVendasDTO;
import com.example.pdv_galeteria.dto.ProdutoMaisVendidoDTO;
import com.example.pdv_galeteria.dto.RelatorioMovimentoCaixaDTO;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface RelatorioService {

    void gerarRelatorioVendas(
            LocalDate dataInicio,
            LocalDate dataFim,
            Path caminhoArquivo
    );

    RelatorioVendasDTO getRelatorioVendasHoje();
    RelatorioVendasDTO getRelatorioVendasSemana();
    RelatorioVendasDTO getRelatorioVendasMes();
    RelatorioVendasDTO getRelatorioVendasPorPeriodo(LocalDate dataInicio, LocalDate dataFim);

    List<ProdutoMaisVendidoDTO> getProdutosMaisVendidos(int limite);
    List<ProdutoMaisVendidoDTO> getProdutosMaisVendidosPorPeriodo(
            LocalDate dataInicio, LocalDate dataFim, int limite);

    List<RelatorioMovimentoCaixaDTO> getMovimentosCaixaPorPeriodo(
            LocalDateTime inicio, LocalDateTime fim);

    List<PedidoResumoDTO> getUltimasVendasHoje(int limite);
    Map<String, BigDecimal> getDistribuicaoPagamentoHoje();
}