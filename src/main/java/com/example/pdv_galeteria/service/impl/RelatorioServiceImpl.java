package com.example.pdv_galeteria.service.Impl;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.pdv_galeteria.dto.RelatorioMovimentoCaixaDTO;
import com.example.pdv_galeteria.repository.MovimentoCaixaRepository;
import com.example.pdv_galeteria.service.RelatorioService;
import com.example.pdv_galeteria.util.GeradorRelatorioPDF;

@Service
public class RelatorioServiceImpl implements RelatorioService {

    private final MovimentoCaixaRepository movimentoCaixaRepository;

    public RelatorioServiceImpl(MovimentoCaixaRepository movimentoCaixaRepository) {
        this.movimentoCaixaRepository = movimentoCaixaRepository;
    }

    @Override
    public void gerarRelatorioVendas(
            LocalDate inicio,
            LocalDate fim,
            Path caminhoArquivo
    ) {

        List<RelatorioMovimentoCaixaDTO> movimentos =
                movimentoCaixaRepository.buscarMovimentosParaRelatorio(
                        inicio.atStartOfDay(),
                        fim.atTime(23, 59, 59)
                );

        GeradorRelatorioPDF.gerar(movimentos, caminhoArquivo);
    }
}
