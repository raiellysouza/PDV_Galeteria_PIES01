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

    public List<Combo> listarTodos() {
        return comboService.buscarTodosCombos();
    }


    public Optional<Combo> buscarPorId(Long id) {
  

        try {
            return Optional.of(comboService.buscarComboPorId(id));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

   
    public Combo salvar(Combo combo) {
        return comboService.salvarCombo(combo);
    }

  
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

    
    public void deletar(Long id) {
        comboService.deletarCombo(id);
    }
}
