package com.example.pdv_galeteria.service;

import java.util.List;

import com.example.pdv_galeteria.model.Estoque;
import com.example.pdv_galeteria.repository.EstoqueRepository;

import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EstoqueService {

    @Autowired
    private EstoqueRepository estoqueRepository;

    public void adicionarAoEstoque(Long produtoId, Integer quantidade) {

        Estoque estoque = estoqueRepository.findByProdutoId(produtoId)
                .orElse(null);

        if (estoque == null) {
            estoque = new Estoque();
            estoque.setProdutoId(produtoId);
            estoque.setQuantidade(quantidade);
        } else {
            estoque.setQuantidade(estoque.getQuantidade() + quantidade);
        }

        estoqueRepository.save(estoque);
    }


    public static class RemocaoRequest {
        private Long produtoId;
        private Integer quantidade;

        public RemocaoRequest(Long produtoId, Integer quantidade) {
            this.produtoId = produtoId;
            this.quantidade = quantidade;
        }

        public Long getProdutoId() {
            return produtoId;
        }

        public void setProdutoId(Long produtoId) {
            this.produtoId = produtoId;
        }

        public Integer getQuantidade() {
            return quantidade;
        }

        public void setQuantidade(Integer quantidade) {
            this.quantidade = quantidade;
        }
    }


    public void removerDoEstoque(Long produtoId, Integer quantidade) {
        Estoque estoque = estoqueRepository.findByProdutoId(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado no estoque"));

        if (estoque.getQuantidade() < quantidade) {
            throw new RuntimeException("Estoque insuficiente para o produto ID: " + produtoId);
        }

        estoque.setQuantidade(estoque.getQuantidade() - quantidade);
        estoqueRepository.save(estoque);
    }

    public void removerMultiplosItens(List<RemocaoRequest> itens) {
        for (RemocaoRequest item : itens) {
            removerDoEstoque(item.getProdutoId(), item.getQuantidade());
        }
    }
}