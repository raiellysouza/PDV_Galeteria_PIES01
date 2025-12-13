package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.model.Caixa;
import com.example.pdv_galeteria.model.MovimentoCaixa;
import com.example.pdv_galeteria.model.StatusCaixa;
import com.example.pdv_galeteria.model.TipoMovimentoCaixa;
import com.example.pdv_galeteria.repository.CaixaRepository;
import com.example.pdv_galeteria.repository.MovimentoCaixaRepository;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MovimentoCaixaService {

    @Autowired
    private CaixaRepository caixaRepository;

    @Autowired
    private MovimentoCaixaRepository movimentoCaixaRepository;

    @Transactional
    public MovimentoCaixa registrarEntrada(BigDecimal valor, String descricao, String referenciaExterna) {
        return registrarMovimento(TipoMovimentoCaixa.ENTRADA, valor, descricao, referenciaExterna);
    }

    @Transactional
    public MovimentoCaixa registrarSaida(BigDecimal valor, String descricao, String referenciaExterna) {
        return registrarMovimento(TipoMovimentoCaixa.SAIDA, valor, descricao, referenciaExterna);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoAtual(Caixa caixa) {
        if (caixa == null || caixa.getId() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal entradas = movimentoCaixaRepository.somarPorTipo(caixa.getId(), TipoMovimentoCaixa.ENTRADA);
        BigDecimal saidas = movimentoCaixaRepository.somarPorTipo(caixa.getId(), TipoMovimentoCaixa.SAIDA);

        BigDecimal base = caixa.getValorInicial() != null ? caixa.getValorInicial() : BigDecimal.ZERO;
        entradas = entradas != null ? entradas : BigDecimal.ZERO;
        saidas = saidas != null ? saidas : BigDecimal.ZERO;

        return base.add(entradas).subtract(saidas);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularTotalEntradas(Caixa caixa) {
        if (caixa == null || caixa.getId() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal entradas = movimentoCaixaRepository.somarPorTipo(caixa.getId(), TipoMovimentoCaixa.ENTRADA);
        return entradas != null ? entradas : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularTotalSaidas(Caixa caixa) {
        if (caixa == null || caixa.getId() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal saidas = movimentoCaixaRepository.somarPorTipo(caixa.getId(), TipoMovimentoCaixa.SAIDA);
        return saidas != null ? saidas : BigDecimal.ZERO;
    }

    private MovimentoCaixa registrarMovimento(TipoMovimentoCaixa tipo, BigDecimal valor, String descricao, String referenciaExterna) {
        Caixa caixa = caixaRepository.findCaixaAbertoDoDia()
                .orElseThrow(() -> new RuntimeException("Nenhum caixa aberto para registrar movimentos."));

        if (caixa.getStatus() != StatusCaixa.ABERTO) {
            throw new RuntimeException("Não é possível registrar movimentos em um caixa fechado.");
        }

        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valor do movimento deve ser maior que zero.");
        }

        MovimentoCaixa movimento = new MovimentoCaixa();
        movimento.setCaixa(caixa);
        movimento.setTipo(tipo);
        movimento.setValor(valor.setScale(2, RoundingMode.HALF_UP));
        movimento.setDescricao(descricao);
        movimento.setReferenciaExterna(referenciaExterna);
        movimento.setDataHora(LocalDateTime.now());

        if (tipo == TipoMovimentoCaixa.ENTRADA) {
            caixa.setTotalEntradas(caixa.getTotalEntradas().add(valor));
            caixa.setSaldoAtual(caixa.getSaldoAtual().add(valor));
        } else if (tipo == TipoMovimentoCaixa.SAIDA) {
            caixa.setTotalSaidas(caixa.getTotalSaidas().add(valor));
            caixa.setSaldoAtual(caixa.getSaldoAtual().subtract(valor));
        }

        caixa.setUpdatedAt(LocalDateTime.now());

        MovimentoCaixa movimentoSalvo = movimentoCaixaRepository.save(movimento);

        caixaRepository.save(caixa);

        return movimentoSalvo;
    }

    public MovimentoCaixa registrarVenda(BigDecimal valor, String numeroVenda) {
        return registrarEntrada(
                valor,
                "Venda #" + numeroVenda,
                "VENDA_" + numeroVenda
        );
    }

    public MovimentoCaixa registrarTroco(BigDecimal valor) {
        return registrarSaida(
                valor,
                "Troco",
                "TROCO_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        );
    }

    public List<MovimentoCaixa> listarMovimentosDoCaixa(Long caixaId) {
        return movimentoCaixaRepository.findByCaixaIdOrderByDataHoraDesc(caixaId);
    }

    public List<MovimentoCaixa> listarMovimentosDoDia(LocalDate data) {
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.atTime(23, 59, 59);

        return movimentoCaixaRepository.findByDataHoraBetweenOrderByDataHoraDesc(inicioDia, fimDia);
    }
}

