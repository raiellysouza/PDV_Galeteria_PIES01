package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.model.Entregador;
import com.example.pdv_galeteria.model.StatusEntregador;
import com.example.pdv_galeteria.repository.EntregadorRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        entregador.setNome(nome);
        entregador.setTelefone(formatarTelefone(telefone));
        entregador.setStatus(StatusEntregador.DISPONIVEL);
        entregador.setEntregasHoje(0);

        return entregadorRepository.save(entregador);
    }

    public List<Entregador> listarTodos() {
        return entregadorRepository.findAll();
    }

    public List<Entregador> buscarPorNome(String nome) {
        return entregadorRepository.findByNomeContainingIgnoreCase(nome);
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

        existente.setNome(entregador.getNome());
        existente.setTelefone(formatarTelefone(entregador.getTelefone()));
        existente.setStatus(entregador.getStatus());

        return entregadorRepository.save(existente);
    }

    public void atualizarStatus(Long id, StatusEntregador novoStatus) {
        Entregador entregador = entregadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entregador não encontrado"));

        entregador.setStatus(novoStatus);
        entregadorRepository.save(entregador);
    }

    public void alternarStatusAtivo(Long id) {
        Entregador entregador = entregadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entregador não encontrado"));

        if (entregador.getStatus() == StatusEntregador.INATIVO) {
            entregador.setStatus(StatusEntregador.DISPONIVEL);
        } else {
            entregador.setStatus(StatusEntregador.INATIVO);
        }

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

    public List<Entregador> listarAtivos() {
        return entregadorRepository.findAtivos();
    }

    public int contarEntregadoresAtivos() {
        return (int) entregadorRepository.countAtivos();
    }

    public int contarTotalEntregasHoje() {
        return entregadorRepository.sumEntregasHojeAtivos();
    }

    public List<Entregador> listarNaoInativos() {
        return entregadorRepository.findByStatusNot(StatusEntregador.INATIVO);
    }

    public int contarNaoInativos() {
        return (int) entregadorRepository.countByStatusNot(StatusEntregador.INATIVO);
    }

    public int somarEntregasNaoInativos() {
        return entregadorRepository.sumEntregasHojeByStatusNot(StatusEntregador.INATIVO);
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