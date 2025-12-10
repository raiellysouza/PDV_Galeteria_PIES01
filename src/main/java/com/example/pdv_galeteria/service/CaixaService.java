package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.model.Caixa;
import com.example.pdv_galeteria.model.StatusCaixa;
import com.example.pdv_galeteria.model.MovimentoCaixa;
import com.example.pdv_galeteria.model.TipoMovimentoCaixa;
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
        return caixaRepository.findCaixaDoDia();
    }

    public Optional<Caixa> getCaixaAbertoDoDia() {
        return caixaRepository.findCaixaAbertoDoDia();
    }

    public boolean existeCaixaDoDia() {
        return caixaRepository.existsCaixaDoDia();
    }

    public String getStatusTextoBotao() {
        try {
            boolean caixaExiste = existeCaixaDoDia();
            boolean caixaAberto = getCaixaAbertoDoDia().isPresent();

            System.out.println("getStatusTextoBotao() - caixaExiste: " + caixaExiste + ", caixaAberto: " + caixaAberto);

            return caixaAberto ? "Fechar Caixa" : "Abrir Caixa";

        } catch (Exception e) {
            System.err.println("Erro em getStatusTextoBotao(): " + e.getMessage());
            return "Abrir Caixa";
        }
    }

    public boolean podeAbrirCaixa() {
        Optional<Caixa> caixaOpt = getCaixaDoDia();
        return caixaOpt.isEmpty() || caixaOpt.get().getStatus() != StatusCaixa.ABERTO;
    }

    public Caixa abrirCaixa(BigDecimal valorInicial) {
        if (!podeAbrirCaixa()) {
            throw new RuntimeException("Não é possível abrir caixa! Verifique se já existe um caixa aberto.");
        }

        Optional<Caixa> caixaExistenteOpt = caixaRepository.findByDataCaixa(LocalDate.now());

        if (caixaExistenteOpt.isPresent()) {
            Caixa caixa = caixaExistenteOpt.get();
            if (caixa.getStatus() == StatusCaixa.ABERTO) {
                throw new RuntimeException("Já existe um caixa aberto para hoje!");
            }

            caixa.setStatus(StatusCaixa.ABERTO);
            caixa.setDataAbertura(LocalDateTime.now());
            caixa.setValorInicial(valorInicial);
            caixa.setSaldoAtual(valorInicial);
            caixa.setTotalEntradas(BigDecimal.ZERO);
            caixa.setTotalSaidas(BigDecimal.ZERO);
            caixa.setDataFechamento(null);
            caixa.setValorFinal(null);
            caixa.setUpdatedAt(LocalDateTime.now());

            return caixaRepository.save(caixa);
        } else {
            Caixa novoCaixa = new Caixa();
            novoCaixa.setDataCaixa(LocalDate.now());
            novoCaixa.setDataAbertura(LocalDateTime.now());
            novoCaixa.setValorInicial(valorInicial);
            novoCaixa.setSaldoAtual(valorInicial);
            novoCaixa.setStatus(StatusCaixa.ABERTO);
            novoCaixa.setTotalEntradas(BigDecimal.ZERO);
            novoCaixa.setTotalSaidas(BigDecimal.ZERO);
            novoCaixa.setCreatedAt(LocalDateTime.now());
            novoCaixa.setUpdatedAt(LocalDateTime.now());

            return caixaRepository.save(novoCaixa);
        }
    }

    @Transactional
    public Caixa fecharCaixa(BigDecimal valorFinal) {
        Caixa caixa = getCaixaAbertoDoDia()
                .orElseThrow(() -> new RuntimeException("Não há caixa aberto para fechar!"));

        BigDecimal saldoFinal = movimentoCaixaService.calcularSaldoAtual(caixa);

        if (valorFinal == null) {
            valorFinal = saldoFinal;
        }

        caixa.setValorFinal(valorFinal);
        caixa.setSaldoAtual(valorFinal);
        caixa.setDataFechamento(LocalDateTime.now());
        caixa.setStatus(StatusCaixa.FECHADO);
        caixa.setObservacoes("Caixa fechado com valor final: R$ " + formatarValor(valorFinal));
        caixa.setUpdatedAt(LocalDateTime.now());

        return caixaRepository.save(caixa);
    }

    private String formatarValor(BigDecimal valor) {
        return String.format("R$ %.2f", valor).replace(".", ",");
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
        Optional<Caixa> caixaOpt = getCaixaDoDia();
        if (caixaOpt.isPresent()) {
            return movimentoCaixaService.calcularSaldoAtual(caixaOpt.get());
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getTotalEntradasDoDia() {
        Optional<Caixa> caixaOpt = getCaixaDoDia();
        if (caixaOpt.isPresent()) {
            return movimentoCaixaService.calcularTotalEntradas(caixaOpt.get());
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getTotalSaidasDoDia() {
        Optional<Caixa> caixaOpt = getCaixaDoDia();
        if (caixaOpt.isPresent()) {
            return movimentoCaixaService.calcularTotalSaidas(caixaOpt.get());
        }
        return BigDecimal.ZERO;
    }

    @Transactional
    public void adicionarEntradaSimples(BigDecimal valor, String descricao) {
        Caixa caixa = getCaixaAbertoDoDia()
                .orElseThrow(() -> new RuntimeException("Não há caixa aberto!"));

        caixa.setSaldoAtual(caixa.getSaldoAtual().add(valor));
        caixa.setTotalEntradas(caixa.getTotalEntradas().add(valor));
        caixa.setUpdatedAt(LocalDateTime.now());

        if (descricao != null && !descricao.trim().isEmpty()) {
            String obsAtual = caixa.getObservacoes() != null ? caixa.getObservacoes() + "\n" : "";
            caixa.setObservacoes(obsAtual + "Entrada: " + descricao + " - R$ " + valor);
        }

        caixaRepository.save(caixa);
    }

    @Transactional
    public void adicionarSaidaSimples(BigDecimal valor, String descricao) {
        Caixa caixa = getCaixaAbertoDoDia()
                .orElseThrow(() -> new RuntimeException("Não há caixa aberto!"));

        if (caixa.getSaldoAtual().compareTo(valor) < 0) {
            throw new RuntimeException("Saldo insuficiente! Saldo atual: R$ " +
                    caixa.getSaldoAtual() + ", Tentativa de saída: R$ " + valor);
        }

        caixa.setSaldoAtual(caixa.getSaldoAtual().subtract(valor));
        caixa.setTotalSaidas(caixa.getTotalSaidas().add(valor));
        caixa.setUpdatedAt(LocalDateTime.now());

        if (descricao != null && !descricao.trim().isEmpty()) {
            String obsAtual = caixa.getObservacoes() != null ? caixa.getObservacoes() + "\n" : "";
            caixa.setObservacoes(obsAtual + "Saída: " + descricao + " - R$ " + valor);
        }

        caixaRepository.save(caixa);
    }

    @Transactional
    public void atualizarSaldoCaixa(Long caixaId, BigDecimal novoSaldo) {
        Caixa caixa = caixaRepository.findById(caixaId)
                .orElseThrow(() -> new RuntimeException("Caixa não encontrado!"));

        caixa.setSaldoAtual(novoSaldo);
        caixa.setUpdatedAt(LocalDateTime.now());
        caixaRepository.save(caixa);
    }

    @Transactional
    public Caixa fecharCaixaComValorFinal(Long caixaId, BigDecimal valorFinal) {
        Caixa caixa = caixaRepository.findById(caixaId)
                .orElseThrow(() -> new RuntimeException("Caixa não encontrado!"));

        if (caixa.getStatus() != StatusCaixa.ABERTO) {
            throw new RuntimeException("Este caixa não está aberto!");
        }

        caixa.setValorFinal(valorFinal);
        caixa.setSaldoAtual(valorFinal);
        caixa.setDataFechamento(LocalDateTime.now());
        caixa.setStatus(StatusCaixa.FECHADO);
        caixa.setUpdatedAt(LocalDateTime.now());

        return caixaRepository.save(caixa);
    }
}