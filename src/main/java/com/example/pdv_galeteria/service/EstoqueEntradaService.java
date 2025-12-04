package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.dto.EntradaEstoqueRequest;
import com.example.pdv_galeteria.dto.EntradaEstoqueResponse;
import com.example.pdv_galeteria.model.EstoqueEntrada;
import com.example.pdv_galeteria.repository.EstoqueEntradaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class EstoqueEntradaService {

    @Autowired
    private EstoqueEntradaRepository estoqueEntradaRepository;

    @Autowired
    private EstoqueService estoqueService;

    public EntradaEstoqueResponse registrarEntrada(EntradaEstoqueRequest request) {
        EstoqueEntrada entrada = new EstoqueEntrada();
        entrada.setProdutoId(request.getProdutoId());
        entrada.setQuantidade(request.getQuantidade());
        entrada.setObservacao(request.getObservacao());
        entrada.setDataEntrada(LocalDateTime.now());

        estoqueEntradaRepository.salvar(entrada);

        estoqueService.adicionarAoEstoque(
            request.getProdutoId(),
            request.getQuantidade()
        );

        EntradaEstoqueResponse response = new EntradaEstoqueResponse();
        response.setId(entrada.getId());
        response.setProdutoId(entrada.getProdutoId());
        response.setQuantidade(entrada.getQuantidade());
        response.setObservacao(entrada.getObservacao());
        response.setDataEntrada(entrada.getDataEntrada());

        return response;
    }
}
