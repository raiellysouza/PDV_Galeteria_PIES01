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
        return caixaRepository.findCaixaDoDia();
    }

    public Optional<Caixa> getCaixaAbertoDoDia() {
        return caixaRepository.findCaixaAbertoDoDia();
    }

    public String getStatusTextoBotao() {
        Optional<Caixa> caixaOpt = getCaixaAbertoDoDia();
        return caixaOpt.isPresent() ? "Fechar Caixa" : "Abrir Caixa";
    }

    public boolean podeAbrirCaixa() {
        return !getCaixaAbertoDoDia().isPresent();
    }

    public Caixa abrirCaixa(BigDecimal valorInicial) {
        if (!podeAbrirCaixa()) {
            throw new RuntimeException("Já existe um caixa aberto hoje!");
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

        Optional<Caixa> caixaExistenteOpt = caixaRepository.findByDataCaixa(LocalDate.now());

        if (caixaExistenteOpt.isPresent()) {
            Caixa caixa = caixaExistenteOpt.get();
            caixa.setStatus(StatusCaixa.ABERTO);
            caixa.setDataAbertura(LocalDateTime.now());
            caixa.setValorInicial(valorInicial);
            caixa.setSaldoAtual(valorInicial);
            caixa.setTotalEntradas(BigDecimal.ZERO);
            caixa.setTotalSaidas(BigDecimal.ZERO);
            caixa.setObservacoes("Reabertura de caixa");
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
            novoCaixa.setObservacoes("Abertura de caixa");

            if (novoCaixa.getCreatedAt() == null) {
                novoCaixa.setCreatedAt(LocalDateTime.now());
            }
            if (novoCaixa.getUpdatedAt() == null) {
                novoCaixa.setUpdatedAt(LocalDateTime.now());
            }

            return caixaRepository.save(novoCaixa);
        }
    }

    @Transactional
    public Caixa fecharCaixa(BigDecimal valorFinal) {
        Caixa caixa = getCaixaAbertoDoDia()
                .orElseThrow(() -> new RuntimeException("Não há caixa aberto para fechar!"));
  
    public Caixa fecharCaixa(String observacoes) {

        Optional<Caixa> caixaOpt = getCaixaDoDia();

        if (!caixaOpt.isPresent()) {
            throw new RuntimeException("Não há caixa para hoje!");
        }

        Caixa caixa = caixaOpt.get();

        if (valorFinal == null) {
            valorFinal = caixa.getSaldoAtual();
        }

        caixa.setValorFinal(valorFinal);
        caixa.setDataFechamento(LocalDateTime.now());
        caixa.setStatus(StatusCaixa.FECHADO);
        caixa.setObservacoes(observacoes);

        caixa.setValorFinal(movimentoCaixaService.calcularSaldoAtual(caixa));

        caixa.setUpdatedAt(LocalDateTime.now());

        return caixaRepository.save(caixa);
    }
  
    @Transactional
    public MovimentoCaixa registrarEntrada(BigDecimal valor, String descricao, String referenciaExterna) {
        return movimentoCaixaService.registrarEntrada(valor, descricao, referenciaExterna);
    }

    public boolean existeCaixaDoDia() {
        return caixaRepository.existsCaixaDoDia();
    }

    public void adicionarEntrada(BigDecimal valor, String descricao) {
        Caixa caixa = getCaixaAbertoDoDia()
                .orElseThrow(() -> new RuntimeException("Não há caixa aberto!"));

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

        if (descricao != null && !descricao.trim().isEmpty()) {
            String obsAtual = caixa.getObservacoes() != null ? caixa.getObservacoes() + "\n" : "";
            caixa.setObservacoes(obsAtual + "Entrada: " + descricao);
        }

        caixaRepository.save(caixa);
    }

    public void adicionarSaida(BigDecimal valor, String descricao) {

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
            caixa.setObservacoes(obsAtual + "Saída: " + descricao);
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

            System.out.println("Saldo do caixa " + caixaId + " atualizado para: R$ " + novoSaldo);
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

            System.out.println("Caixa " + caixaId + " fechado com valor final: R$ " + valorFinal);
            return caixaRepository.save(caixa);
        }
    }
}
