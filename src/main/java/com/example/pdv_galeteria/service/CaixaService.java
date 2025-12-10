package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.model.Caixa;
import com.example.pdv_galeteria.model.StatusCaixa;
import com.example.pdv_galeteria.model.MovimentoCaixa;
import com.example.pdv_galeteria.repository.CaixaRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class CaixaService {

    @Autowired
    private CaixaRepository caixaRepository;

    @Autowired
    private MovimentoCaixaService movimentoCaixaService;

    public Optional<Caixa> getCaixaDoDia() {
        return caixaRepository.findByDataCaixa(LocalDate.now());
    }

    public Optional<Caixa> getCaixaAbertoDoDia() {
        return caixaRepository.findCaixaAbertoDoDia();
    }

    public boolean existeCaixaDoDia() {
        return caixaRepository.existsCaixaDoDia();
    }

    public boolean podeAbrirCaixa() {
        Optional<Caixa> caixaOpt = getCaixaDoDia();
        if (caixaOpt.isPresent()) {
            return caixaOpt.get().getStatus() != StatusCaixa.ABERTO;
        }
        return true;
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

    public Caixa abrirCaixa(BigDecimal valorInicial, String observacoes) {

        Optional<Caixa> caixaOpt = getCaixaDoDia();

        if (caixaOpt.isPresent()) {
            Caixa caixa = caixaOpt.get();

            if (caixa.getStatus() == StatusCaixa.ABERTO) {
                throw new RuntimeException("Caixa já está aberto hoje!");
            }

            caixa.setStatus(StatusCaixa.ABERTO);
            caixa.setDataAbertura(LocalDateTime.now());
            caixa.setValorInicial(valorInicial);
            caixa.setObservacoes(observacoes);
            caixa.setSaldoAtual(valorInicial);
            caixa.setUpdatedAt(LocalDateTime.now());

            return caixaRepository.save(caixa);

        } else {
            Caixa novoCaixa = new Caixa(valorInicial, observacoes);
            novoCaixa.setDataCaixa(LocalDate.now());
            novoCaixa.setSaldoAtual(valorInicial);

            return caixaRepository.save(novoCaixa);
        }
    }
  
    public Caixa fecharCaixa(String observacoes) {

        Optional<Caixa> caixaOpt = getCaixaDoDia();

        if (!caixaOpt.isPresent()) {
            throw new RuntimeException("Não há caixa para hoje!");
        }

        Caixa caixa = caixaOpt.get();

        if (caixa.getStatus() != StatusCaixa.ABERTO) {
            throw new RuntimeException("Caixa não está aberto!");
        }

        caixa.setStatus(StatusCaixa.FECHADO);
        caixa.setDataFechamento(LocalDateTime.now());
        caixa.setObservacoes(observacoes);

        caixa.setValorFinal(movimentoCaixaService.calcularSaldoAtual(caixa));

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

    public void adicionarEntrada(BigDecimal valor, String descricao) {

        Caixa caixa = getCaixaAbertoDoDia()
                .orElseThrow(() -> new RuntimeException("Não há caixa aberto!"));

        caixa.setSaldoAtual(caixa.getSaldoAtual().add(valor));
        caixa.setTotalEntradas(caixa.getTotalEntradas().add(valor));
        caixa.setUpdatedAt(LocalDateTime.now());

        caixaRepository.save(caixa);
    }

    public void adicionarSaida(BigDecimal valor, String descricao) {

        Caixa caixa = getCaixaAbertoDoDia()
                .orElseThrow(() -> new RuntimeException("Não há caixa aberto!"));

        if (caixa.getSaldoAtual().compareTo(valor) < 0) {
            throw new RuntimeException("Saldo insuficiente!");
        }

        caixa.setSaldoAtual(caixa.getSaldoAtual().subtract(valor));
        caixa.setTotalSaidas(caixa.getTotalSaidas().add(valor));
        caixa.setUpdatedAt(LocalDateTime.now());

        caixaRepository.save(caixa);
    }
}
