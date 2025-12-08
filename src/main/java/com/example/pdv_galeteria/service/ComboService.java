package com.example.pdv_galeteria.service;



import com.example.pdv_galeteria.model.Combo;
import com.example.pdv_galeteria.model.ComboItem;
import com.example.pdv_galeteria.model.Produto;
import com.example.pdv_galeteria.repository.ComboRepository;
import com.example.pdv_galeteria.repository.ProdutoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComboService {

    private final ComboRepository comboRepository;
    private final ProdutoRepository produtoRepository;

    
    @PersistenceContext
    private EntityManager entityManager;
    private Combo save;

    @Autowired
    public ComboService(ComboRepository comboRepository, ProdutoRepository produtoRepository) {
        this.comboRepository = comboRepository;
        this.produtoRepository = produtoRepository;
    }

    /**
     * Salva um novo Combo no banco de dados.
     * @param novoCombo 
     * @return 
     */

    @Transactional
    public Combo salvarCombo(Combo novoCombo) {
        System.out.println("🚨🚨🚨 MÉTODO salvarCombo INICIADO 🚨🚨🚨");

        try {
            // VALIDAÇÕES CRÍTICAS
            if (novoCombo == null) {
                System.err.println("COMBO É NULO!");
                System.err.println("Combo é nulo!");
                throw new IllegalArgumentException("Combo não pode ser nulo");
            }

            System.out.println("📊 Dados do combo recebido:");
            System.out.println("   Nome: " + novoCombo.getNome());
            System.out.println("   Preço: " + novoCombo.getPrecoTotal());
            System.out.println("   Itens: " + (novoCombo.getItensDoCombo() != null ? novoCombo.getItensDoCombo().size() : 0));

            // GARANTIR QUE PREÇO NÃO É NULO
            if (novoCombo.getPrecoTotal() == null) {
                System.out.println("⚠️  Preço era nulo, definindo para 0.0");
                novoCombo.setPrecoTotal(0.0);
            }

            // GARANTIR QUE NOME NÃO É NULO
            if (novoCombo.getNome() == null || novoCombo.getNome().trim().isEmpty()) {
                System.err.println("NOME DO COMBO É NULO OU VAZIO!");
                System.err.println("Nome do combo é nulo ou vazio!");
                throw new IllegalArgumentException("Nome do combo é obrigatório");
            }

            // PROCESSAR ITENS (se existirem)
            if (novoCombo.getItensDoCombo() != null && !novoCombo.getItensDoCombo().isEmpty()) {
                System.out.println("🛒 Processando " + novoCombo.getItensDoCombo().size() + " itens...");

                for (ComboItem item : novoCombo.getItensDoCombo()) {
                    item.setCombo(novoCombo);

                    if (item.getProduto() != null && item.getProduto().getId() != null) {
                        Produto produtoExistente = produtoRepository.findById(item.getProduto().getId())
                                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + item.getProduto().getId()));
                        item.setProduto(produtoExistente);
                    } else {
                        System.err.println("❌ Produto inválido no item: " + item);
                        throw new IllegalArgumentException("Produto inválido no item do combo");
                    }
                }
            } else {
                System.out.println("ℹ️  Combo sem itens - salvando apenas dados básicos");
            }

            System.out.println("💾 Chamando comboRepository.save()...");

            // SALVAR NO BANCO
            Combo salvado = comboRepository.save(novoCombo);

            System.out.println("✅✅✅ COMBO SALVO COM SUCESSO! ID: " + salvado.getId());
            return salvado;

        } catch (Exception e) {
            System.err.println("❌❌❌ ERRO NO salvarCombo:");
            e.printStackTrace();
            throw e; // Re-lança a exceção para o controller
        }
    }
    
    
@Transactional(readOnly = true)
public List<Combo> buscarTodosCombos() {
    List<Combo> combos = comboRepository.findAll();

    combos.forEach(combo -> {
        if (combo.getItensDoCombo() != null) {
            combo.getItensDoCombo().size();
        }
    });

    return combos;
}
    
    
    public Combo buscarComboPorId(Long id) {
        return comboRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Combo não encontrado com ID: " + id));
    }

    /**
     * Deleta um Combo por ID.
     * @param id 
     */

    @Transactional
    public void deletarCombo(Long id) {
        Combo comboParaDeletar = comboRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error, combo não encontrado com ID: " + id));
        
        comboRepository.delete(comboParaDeletar);
    }


    @Transactional
public Combo buscarPorIdComItens(Long id) {
    Combo combo = entityManager.find(Combo.class, id);
    if (combo != null) {
        combo.getItensDoCombo().size(); // inicializa os itens

        // inicializa os produtos de cada item
        combo.getItensDoCombo().forEach(item -> {
            if (item.getProduto() != null) {
                item.getProduto().getNome(); // força carregamento
            }
        });
    }
    return combo;
}

    @Transactional(readOnly = true)
    public List<Combo> buscarCombosPorNome(String termoBusca) {
        try {
            System.out.println("🔍 Buscando combos por: '" + termoBusca + "'");

            // BUSCAR APENAS POR NOME
            List<Combo> combos = comboRepository.findByNomeContainingIgnoreCase(termoBusca);

            // Carrega os itens de cada combo
            combos.forEach(combo -> {
                if (combo.getItensDoCombo() != null) {
                    combo.getItensDoCombo().size(); // Force initialization
                }
            });

            System.out.println("✅ Encontrados " + combos.size() + " combos para: '" + termoBusca + "'");
            return combos;

        } catch (Exception e) {
            System.err.println("❌ Erro ao buscar combos: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

}