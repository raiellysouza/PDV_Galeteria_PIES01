package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.model.VendaEmLote;
import com.example.pdv_galeteria.repository.VendaEmLoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VendaEmLoteService {

    private final VendaEmLoteRepository vendaEmLoteRepository;

    public VendaEmLoteService(VendaEmLoteRepository vendaEmLoteRepository) {
        this.vendaEmLoteRepository = vendaEmLoteRepository;
    }

    @Transactional
    public VendaEmLote criarVendaEmLote(VendaEmLote venda) {
        venda.recalcularTotal();
        return vendaEmLoteRepository.save(venda);
    }

    public VendaEmLote buscarPorId(Long id) {
        return vendaEmLoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda em lote não encontrada"));
    }

    public List<VendaEmLote> buscarPorHorario(LocalDateTime inicio, LocalDateTime fim) {
        return vendaEmLoteRepository.findByCriadoEmBetween(inicio, fim);
    }

    public List<VendaEmLote> buscarPorOrigem(String origem) {
    return vendaEmLoteRepository.findByOrigemIgnoreCase(origem);
    }

    @Transactional
    public void excluirVendaEmLote(Long id) {
        if (!vendaEmLoteRepository.existsById(id)) {
            throw new RuntimeException("Venda em lote não encontrada");
        }
        vendaEmLoteRepository.deleteById(id);
    }

    public Double calcularLucroTotal() {
    return vendaEmLoteRepository.findAll()
            .stream()
            .mapToDouble(VendaEmLote::getTotal)
            .sum();
    }

    public Double calcularLucroPorOrigem(String origem) {
    return vendaEmLoteRepository.findByOrigemIgnoreCase(origem)
            .stream()
            .mapToDouble(VendaEmLote::getTotal)
            .sum();
    }


}

