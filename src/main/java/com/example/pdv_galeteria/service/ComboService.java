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
     
        if (novoCombo.getItensDoCombo() != null) {
            for (ComboItem item : novoCombo.getItensDoCombo()) {
                item.setCombo(novoCombo); 
                
                if (item.getProduto() != null && item.getProduto().getId() != null) {
                    Produto produtoExistente = produtoRepository.findById(item.getProduto().getId())
                            .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + item.getProduto().getId()));
                    item.setProduto(produtoExistente); 
                }
            }
        }

        return comboRepository.save(novoCombo);
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

}