package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.dto.EntradaEstoqueRequest;
import com.example.pdv_galeteria.dto.EntradaEstoqueResponse;
import com.example.pdv_galeteria.service.EstoqueEntradaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntradaEstoqueController {

    @Autowired
    private EstoqueEntradaService estoqueEntradaService;

    public EntradaEstoqueResponse adicionarEntrada(Long produtoId, Integer quantidade, String observacao) {
        EntradaEstoqueRequest request = new EntradaEstoqueRequest();
        request.setProdutoId(produtoId);
        request.setQuantidade(quantidade);
        request.setObservacao(observacao);

        return estoqueEntradaService.registrarEntrada(request);
    }
}
