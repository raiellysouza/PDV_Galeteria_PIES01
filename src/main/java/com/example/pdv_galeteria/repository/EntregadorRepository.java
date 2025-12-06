package com.example.pdv_galeteria.repository;

import com.example.pdv_galeteria.model.Entregador;
import com.example.pdv_galeteria.model.StatusEntregador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntregadorRepository extends JpaRepository<Entregador, Long> {

    Optional<Entregador> findByTelefone(String telefone);

    List<Entregador> findByStatus(StatusEntregador status);

    List<Entregador> findByNomeCompletoContainingIgnoreCase(String nome);

    boolean existsByTelefone(String telefone);
}