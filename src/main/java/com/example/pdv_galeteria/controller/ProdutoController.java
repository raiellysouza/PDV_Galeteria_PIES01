package com.example.pdv_galeteria.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.pdv_galeteria.model.Produto;
import com.example.pdv_galeteria.service.ProdutoService;

import lombok.Data;

@Data
@Component
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    public List<Produto> listarTodos() {
        return produtoService.listarTodos();
    }

    public Optional<Produto> buscarPorId(Long id) {
        return produtoService.buscarPorId(id);
    }

    public Produto salvar(Produto produto) {
        return produtoService.salvar(produto);
    }

    public Optional<Produto> atualizar(Long id, Produto produto) {
        return produtoService.buscarPorId(id)
                .map(existing -> {
                    existing.setNome(produto.getNome());
                    existing.setPreco(produto.getPreco());
                    existing.setQuantidade(produto.getQuantidade());
                    return produtoService.salvar(existing);
                });
    }

    public void deletar(Long id) {
        produtoService.deletar(id);
    }
}