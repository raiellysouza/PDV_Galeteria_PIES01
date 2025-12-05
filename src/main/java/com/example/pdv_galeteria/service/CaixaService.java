package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.model.Caixa;
import com.example.pdv_galeteria.model.StatusCaixa;
import com.example.pdv_galeteria.model.MovimentoCaixa;
import com.example.pdv_galeteria.repository.CaixaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CaixaService {

    @Autowired
    private CaixaRepository caixaRepository;

    @Autowired
    private MovimentoCaixaService movimentoCaixaService;

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

        caixa.setDataFechamento(LocalDateTime.now());
        caixa.setValorFinal(movimentoCaixaService.calcularSaldoAtual(caixa));
        caixa.setStatus(StatusCaixa.FECHADO);

        if (observacoes != null && !observacoes.trim().isEmpty()) {
            caixa.setObservacoes(observacoes);
        }

        caixa.setUpdatedAt(LocalDateTime.now());

        return caixaRepository.save(caixa);
    }

    @Transactional
    public MovimentoCaixa registrarEntrada(BigDecimal valor, String descricao, String referenciaExterna) {
        return movimentoCaixaService.registrarEntrada(valor, descricao, referenciaExterna);
    }

    @Transactional
    public MovimentoCaixa registrarSaida(BigDecimal valor, String descricao, String referenciaExterna) {
        return movimentoCaixaService.registrarSaida(valor, descricao, referenciaExterna);
    }

    public BigDecimal getSaldoAtualDoDia() {
        Caixa caixa = caixaRepository.findCaixaDoDia()
                .orElseThrow(() -> new RuntimeException("Nenhum caixa encontrado para hoje."));
        return movimentoCaixaService.calcularSaldoAtual(caixa);
    }

    public BigDecimal getTotalEntradasDoDia() {
        Caixa caixa = caixaRepository.findCaixaDoDia()
                .orElseThrow(() -> new RuntimeException("Nenhum caixa encontrado para hoje."));
        return movimentoCaixaService.calcularTotalEntradas(caixa);
    }

    public BigDecimal getTotalSaidasDoDia() {
        Caixa caixa = caixaRepository.findCaixaDoDia()
                .orElseThrow(() -> new RuntimeException("Nenhum caixa encontrado para hoje."));
        return movimentoCaixaService.calcularTotalSaidas(caixa);
    }

    public String getStatusTextoBotao() {
        boolean caixaExiste = caixaRepository.existsCaixaDoDia();
        boolean caixaAberto = caixaRepository.findCaixaAbertoDoDia().isPresent();

        if (!caixaExiste) {
            return "Abrir Caixa";
        } else if (caixaAberto) {
            return "Fechar Caixa";
        } else {
            return "Caixa Já Fechado";
        }
    }

    public boolean podeAbrirCaixa() {
        return !caixaRepository.existsCaixaDoDia();
    }

    public boolean podeFecharCaixa() {
        return caixaRepository.findCaixaAbertoDoDia().isPresent();
    }

    public Optional<Caixa> getCaixaDoDia() {
        return caixaRepository.findCaixaDoDia();
    }
}