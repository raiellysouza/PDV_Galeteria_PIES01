package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.model.Entregador;
import com.example.pdv_galeteria.model.StatusEntregador;
import com.example.pdv_galeteria.repository.EntregadorRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EntregadorService {

    @Autowired
    private EntregadorRepository entregadorRepository;

    public Entregador cadastrarEntregador(String nome, String telefone) {
        if (entregadorRepository.existsByTelefone(telefone)) {
            throw new RuntimeException("Já existe um entregador com este telefone!");
        }

        Entregador entregador = new Entregador();
        entregador.setNomeCompleto(nome);
        entregador.setTelefone(formatarTelefone(telefone));
        entregador.setStatus(StatusEntregador.DISPONIVEL);

        return entregadorRepository.save(entregador);
    }

    public List<Entregador> listarTodos() {
        return entregadorRepository.findAll();
    }

    public List<Entregador> buscarPorNome(String nome) {
        return entregadorRepository.findByNomeCompletoContainingIgnoreCase(nome);
    }

    public Optional<Entregador> buscarPorId(Long id) {
        return entregadorRepository.findById(id);
    }

    public Entregador atualizarEntregador(Entregador entregador) {
        Entregador existente = entregadorRepository.findById(entregador.getId())
                .orElseThrow(() -> new RuntimeException("Entregador não encontrado"));

        if (!existente.getTelefone().equals(entregador.getTelefone()) &&
                entregadorRepository.existsByTelefone(entregador.getTelefone())) {
            throw new RuntimeException("Já existe um entregador com este telefone!");
        }

        existente.setNomeCompleto(entregador.getNomeCompleto());
        existente.setTelefone(formatarTelefone(entregador.getTelefone()));

        return entregadorRepository.save(existente);
    }

    public void atualizarStatus(Long id, StatusEntregador novoStatus) {
        Entregador entregador = entregadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entregador não encontrado"));

        entregador.setStatus(novoStatus);
        entregadorRepository.save(entregador);
    }

    public void excluirEntregador(Long id) {
        if (!entregadorRepository.existsById(id)) {
            throw new RuntimeException("Entregador não encontrado");
        }
        entregadorRepository.deleteById(id);
    }

    public List<Entregador> listarDisponiveis() {
        return entregadorRepository.findByStatus(StatusEntregador.DISPONIVEL);
    }

    private String formatarTelefone(String telefone) {
        String numeros = telefone.replaceAll("\\D", "");

        if (numeros.length() == 11) {
            return "(" + numeros.substring(0, 2) + ") " +
                    numeros.substring(2, 7) + "-" +
                    numeros.substring(7);
        }
        else if (numeros.length() == 10) {
            return "(" + numeros.substring(0, 2) + ") " +
                    numeros.substring(2, 6) + "-" +
                    numeros.substring(6);
        }

        return telefone;
    }
}