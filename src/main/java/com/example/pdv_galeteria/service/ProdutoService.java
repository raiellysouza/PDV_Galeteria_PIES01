package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.dto.EntradaEstoqueRequest;
import com.example.pdv_galeteria.model.Produto;
import com.example.pdv_galeteria.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EstoqueEntradaService estoqueEntradaService;

    public Produto salvar(Produto produto) {
        if (produto.getId() != null) {
            Optional<Produto> produtoExistente = produtoRepository.findById(produto.getId());
            if (produtoExistente.isPresent()) {
                Produto produtoAtual = produtoExistente.get();
                String novoNome = produto.getNome();

                if (produtoAtual.getNome().equalsIgnoreCase(novoNome)) {
                    return produtoRepository.save(produto);
                }

                Optional<Produto> produtoComMesmoNome = produtoRepository.findByNomeIgnoreCaseAndAtivoTrue(novoNome);
                if (produtoComMesmoNome.isPresent()) {
                    Produto outroProduto = produtoComMesmoNome.get();
                    if (!outroProduto.getId().equals(produto.getId())) {
                        throw new IllegalArgumentException("Já existe um produto ativo com o nome '" + novoNome + "'.\n" +
                                "ID: " + outroProduto.getId() + "\n" +
                                "Status: Ativo\n" +
                                "Use um nome diferente ou edite o produto existente.");
                    }
                }

                return produtoRepository.save(produto);
            }
        }

        String nome = produto.getNome();
        Optional<Produto> produtoExistente = produtoRepository.findByNomeIgnoreCaseAndAtivoTrue(nome);

        if (produtoExistente.isPresent()) {
            Produto outroProduto = produtoExistente.get();
            throw new IllegalArgumentException("Já existe um produto ativo com o nome '" + nome + "'.\n" +
                    "ID: " + outroProduto.getId() + "\n" +
                    "Status: Ativo\n" +
                    "Use um nome diferente ou edite o produto existente.");
        }

        Optional<Produto> produtoDesativado = produtoRepository.findByNomeIgnoreCase(nome);
        if (produtoDesativado.isPresent() && !produtoDesativado.get().getAtivo()) {
            Produto produtoParaReativar = produtoDesativado.get();
            produtoParaReativar.setAtivo(true);
            produtoParaReativar.setPreco(produto.getPreco());
            produtoParaReativar.setQuantidade(produto.getQuantidade());
            produtoParaReativar.setCategoria(produto.getCategoria());
            return produtoRepository.save(produtoParaReativar);
        }

        produto.setAtivo(true);
        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto atualizar(Produto produto) {
        return produtoRepository.save(produto);
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findByAtivoTrue();
    }

    public List<Produto> buscarListaPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(nome);
    }

    @Transactional
    public void desativarProduto(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com ID: " + id));

        produto.setAtivo(false);
        produto.setQuantidade(0);
        produtoRepository.save(produto);

        System.out.println("Produto desativado: " + produto.getNome() + " (ID: " + id + ")");
    }

    @Deprecated
    @Transactional
    public void deletar(Long id) {
        desativarProduto(id);
    }

    public Produto buscarPrimeiroPorNome(String nome) {
        List<Produto> produtos = produtoRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(nome);
        if (produtos != null && !produtos.isEmpty()) {
            return produtos.get(0);
        }
        return null;
    }

    public Produto buscarPorNome(String nome) {
        return buscarPrimeiroPorNome(nome);
    }

    public Optional<Produto> buscarPorNomeExatoIncluindoDesativados(String nome) {
        return produtoRepository.findByNomeIgnoreCase(nome);
    }

    public Optional<Produto> buscarPorNomeExato(String nome) {
        return produtoRepository.findByNomeIgnoreCaseAndAtivoTrue(nome);
    }

    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }

    public Produto salvarIgnorandoDuplicado(Produto produto) {
        if (produto.getId() != null) {
            Optional<Produto> produtoExistente = produtoRepository.findById(produto.getId());
            if (produtoExistente.isPresent()) {
                Produto produtoAtual = produtoExistente.get();
                produtoAtual.setNome(produto.getNome());
                produtoAtual.setPreco(produto.getPreco());
                produtoAtual.setQuantidade(produto.getQuantidade());
                produtoAtual.setCategoria(produto.getCategoria());
                produtoAtual.setAtivo(produto.getAtivo());

                return produtoRepository.save(produtoAtual);
            }
        }

        return salvar(produto);
    }

    @Transactional
    public Produto atualizarProduto(Produto produto) {
        if (produto.getId() == null) {
            throw new IllegalArgumentException("Produto não tem ID para atualizar");
        }

        Optional<Produto> produtoExistente = produtoRepository.findById(produto.getId());
        if (produtoExistente.isPresent()) {
            Produto produtoAtual = produtoExistente.get();

            produtoAtual.setNome(produto.getNome());
            produtoAtual.setPreco(produto.getPreco());
            produtoAtual.setQuantidade(produto.getQuantidade());

            if (produto.getCategoria() != null) {
                produtoAtual.setCategoria(produto.getCategoria());
            }

            return produtoRepository.save(produtoAtual);
        }

        throw new IllegalArgumentException("Produto não encontrado com ID: " + produto.getId());
    }

    public List<Produto> buscarPorCategoria(String categoria) {
        return produtoRepository.findByCategoriaAndAtivoTrue(categoria);
    }

    @Transactional
    public void atualizarEstoque(Long id, Integer quantidade) {
        Optional<Produto> produtoOpt = produtoRepository.findById(id);
        if (produtoOpt.isPresent()) {
            Produto produto = produtoOpt.get();
            produto.setQuantidade(quantidade);
            produtoRepository.save(produto);
        } else {
            throw new IllegalArgumentException("Produto não encontrado com ID: " + id);
        }
    }

    @Transactional
    public void adicionarEstoque(Long id, Integer quantidadeAdicional) {
        Optional<Produto> produtoOpt = produtoRepository.findById(id);
        if (produtoOpt.isPresent()) {
            Produto produto = produtoOpt.get();
            int novaQuantidade = produto.getQuantidade() + quantidadeAdicional;
            produto.setQuantidade(novaQuantidade);
            produtoRepository.save(produto);
        } else {
            throw new IllegalArgumentException("Produto não encontrado com ID: " + id);
        }
    }
}