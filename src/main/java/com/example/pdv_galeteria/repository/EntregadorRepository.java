package com.example.pdv_galeteria.repository;

import com.example.pdv_galeteria.model.Entregador;
import com.example.pdv_galeteria.model.StatusEntregador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntregadorRepository extends JpaRepository<Entregador, Long> {

    // Métodos que o Spring Data JPA consegue gerar automaticamente
    List<Entregador> findAllByOrderByNomeAsc();

    List<Entregador> findByNomeContainingIgnoreCase(String nome);

    List<Entregador> findByStatus(StatusEntregador status);

    boolean existsByTelefone(String telefone);

    // Métodos que precisam de queries customizadas

    // 1. Buscar todos exceto INATIVO
    @Query("SELECT e FROM Entregador e WHERE e.status != 'INATIVO' ORDER BY e.nome")
    List<Entregador> findAtivos();

    // 2. Contar todos exceto INATIVO
    @Query("SELECT COUNT(e) FROM Entregador e WHERE e.status != 'INATIVO'")
    long countAtivos();

    // 3. Somar entregas hoje de ativos
    @Query("SELECT COALESCE(SUM(e.entregasHoje), 0) FROM Entregador e WHERE e.status != 'INATIVO'")
    Integer sumEntregasHojeAtivos();

    // 4. Buscar por status diferente (alternativa)
    @Query("SELECT e FROM Entregador e WHERE e.status <> :status")
    List<Entregador> findByStatusNot(@Param("status") StatusEntregador status);

    // 5. Contar por status diferente
    @Query("SELECT COUNT(e) FROM Entregador e WHERE e.status <> :status")
    long countByStatusNot(@Param("status") StatusEntregador status);

    // 6. Somar entregas por status diferente
    @Query("SELECT COALESCE(SUM(e.entregasHoje), 0) FROM Entregador e WHERE e.status <> :status")
    Integer sumEntregasHojeByStatusNot(@Param("status") StatusEntregador status);
}