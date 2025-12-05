package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.Caixa;
import com.example.pdv_galeteria.model.MovimentoCaixa;
import com.example.pdv_galeteria.service.CaixaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CaixaController {

    @Autowired
    private CaixaService caixaService;

    public Caixa abrirCaixa(BigDecimal valorInicial, String observacoes) {
        return caixaService.abrirCaixa(valorInicial, observacoes);
    }

    public Caixa fecharCaixa(String observacoes) {
        return caixaService.fecharCaixa(observacoes);
    }

    public String getStatusTextoBotao() {
        return caixaService.getStatusTextoBotao();
    }

    public boolean podeAbrirCaixa() {
        return caixaService.podeAbrirCaixa();
    }

    public boolean podeFecharCaixa() {
        return caixaService.podeFecharCaixa();
    }

    public Caixa getCaixaDoDia() {
        return caixaService.getCaixaDoDia().orElse(null);
    }

    public MovimentoCaixa registrarEntrada(BigDecimal valor, String descricao, String referenciaExterna) {
        return caixaService.registrarEntrada(valor, descricao, referenciaExterna);
    }

    public MovimentoCaixa registrarSaida(BigDecimal valor, String descricao, String referenciaExterna) {
        return caixaService.registrarSaida(valor, descricao, referenciaExterna);
    }

    public BigDecimal getSaldoAtualDoDia() {
        return caixaService.getSaldoAtualDoDia();
    }

    public BigDecimal getTotalEntradasDoDia() {
        return caixaService.getTotalEntradasDoDia();
    }

    public BigDecimal getTotalSaidasDoDia() {
        return caixaService.getTotalSaidasDoDia();
    }
}