package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.model.Caixa;
import com.example.pdv_galeteria.model.StatusCaixa;
import com.example.pdv_galeteria.repository.CaixaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CaixaService {

    @Autowired
    private CaixaRepository caixaRepository;

    @Transactional
    public Caixa abrirCaixa(BigDecimal valorInicial, String observacoes) {
        if (caixaRepository.existsCaixaDoDia()) {
            throw new RuntimeException("Já existe um caixa para hoje.");
        }

        if (valorInicial == null || valorInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Valor inicial deve ser maior ou igual a zero.");
        }

        Caixa caixa = new Caixa(valorInicial, observacoes);
        return caixaRepository.save(caixa);
    }

    @Transactional
    public Caixa fecharCaixa(String observacoes) {
        Caixa caixa = caixaRepository.findCaixaDoDia()
                .orElseThrow(() -> new RuntimeException("Nenhum caixa encontrado para hoje."));

        if (caixa.getStatus() == StatusCaixa.FECHADO) {
            throw new RuntimeException("Caixa já está fechado.");
        }

        BigDecimal saldoAtual = caixa.getSaldoAtual();

        caixa.setDataFechamento(LocalDateTime.now());
        caixa.setValorFinal(saldoAtual);
        caixa.setStatus(StatusCaixa.FECHADO);

        if (observacoes != null && !observacoes.trim().isEmpty()) {
            caixa.setObservacoes(observacoes);
        }

        caixa.setUpdatedAt(LocalDateTime.now());

        return caixaRepository.save(caixa);
    }

    public boolean existeCaixaDoDia() {
        return caixaRepository.existsCaixaDoDia();
    }

    public boolean existeCaixaAbertoDoDia() {
        return caixaRepository.findCaixaAbertoDoDia().isPresent();
    }
}