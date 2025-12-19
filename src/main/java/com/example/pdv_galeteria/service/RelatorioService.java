package com.example.pdv_galeteria.service;

import java.nio.file.Path;
import java.time.LocalDate;

public interface RelatorioService {

    void gerarRelatorioVendas(
            LocalDate dataInicio,
            LocalDate dataFim,
            Path caminhoArquivo
    );

}

