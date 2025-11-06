package com.example.pdv_galeteria.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.pdv_galeteria.model.Combo;
import com.example.pdv_galeteria.service.ComboService; 

import lombok.Data;


@Data
@Component

public class ComboController {

    @Autowired
    private ComboService comboService;

    /**
     * Busca e retorna todos os Combos.
     * @return 
     */
    public List<Combo> listarTodos() {
        return comboService.buscarTodosCombos();
    }

    /**
     * Busca um Combo por ID.
     * @param id 
     * @return 
     */
    public Optional<Combo> buscarPorId(Long id) {
  

        try {
            return Optional.of(comboService.buscarComboPorId(id));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Salva ou atualiza um novo Combo.
     * @param combo 
     * @return 
     */
    public Combo salvar(Combo combo) {
        return comboService.salvarCombo(combo);
    }

    /**
     * Atualiza um Combo existente.
     * @param id 
     * @param comboDetalhes 
     * @return 
     */
    public Optional<Combo> atualizar(Long id, Combo comboDetalhes) {
        try {
            Combo existing = comboService.buscarComboPorId(id);

            existing.setNome(comboDetalhes.getNome());
            existing.setPrecoTotal(comboDetalhes.getPrecoTotal());
            
            existing.setItensDoCombo(comboDetalhes.getItensDoCombo());


            return Optional.of(comboService.salvarCombo(existing));
            
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Deleta um Combo por ID.
     * @param id 
     */
    public void deletar(Long id) {
        comboService.deletarCombo(id);
    }
}
