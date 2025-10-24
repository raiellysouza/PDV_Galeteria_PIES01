package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.model.Combo;
import com.example.pdv_galeteria.repository.ComboRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ComboService {

    @Autowired
    private ComboRepository comboRepository;

    public Combo salvar(Combo combo) {
        return comboRepository.save(combo);
    }

    public List<Combo> listarTodos() {
        return comboRepository.findAll();
    }

    public Optional<Combo> buscarPorId(Long id) {
        return comboRepository.findById(id);
    }

    public void deletar(Long id) {
        comboRepository.deleteById(id);
    }
}