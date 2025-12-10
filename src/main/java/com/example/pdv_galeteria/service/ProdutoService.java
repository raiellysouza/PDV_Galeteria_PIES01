package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.dto.EntradaEstoqueRequest;
import com.example.pdv_galeteria.model.Produto;
import com.example.pdv_galeteria.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import com.example.pdv_galeteria.dto.EntradaEstoqueRequest;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EstoqueEntradaService estoqueEntradaService;

    @Transactional
    public Produto salvar(Produto produto) {
        Produto salvo = produtoRepository.save(produto);

        EntradaEstoqueRequest entrada = new EntradaEstoqueRequest();
        entrada.setProdutoId(salvo.getId());
        entrada.setQuantidade(salvo.getQuantidade());
        entrada.setObservacao("Entrada inicial do produto");

        estoqueEntradaService.registrarEntrada(entrada);

        return salvo;
    }

    public Produto buscarPrimeiroPorNome(String nome) {
        List<Produto> produtos = produtoRepository.findByNomeContainingIgnoreCase(nome);
        if (produtos != null && !produtos.isEmpty()) {
            return produtos.get(0);
        }
        return null;
    }

    public List<Produto> buscarListaPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome);
    }

    public Produto buscarPorNome(String nome) {
        return buscarPrimeiroPorNome(nome);
    }

    public Optional<Produto> buscarPorNomeExato(String nome) {
        return produtoRepository.findByNomeIgnoreCase(nome);
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }

    public void deletar(Long id) {
        produtoRepository.deleteById(id);
    }
}