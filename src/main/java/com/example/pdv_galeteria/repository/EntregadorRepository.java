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

    List<Entregador> findAllByOrderByNomeAsc();

    List<Entregador> findByNomeContainingIgnoreCase(String nome);

    List<Entregador> findByStatus(StatusEntregador status);

    boolean existsByTelefone(String telefone);


    @Query("SELECT e FROM Entregador e WHERE e.status != 'INATIVO' ORDER BY e.nome")
    List<Entregador> findAtivos();

    @Query("SELECT COUNT(e) FROM Entregador e WHERE e.status != 'INATIVO'")
    long countAtivos();

    @Query("SELECT COALESCE(SUM(e.entregasHoje), 0) FROM Entregador e WHERE e.status != 'INATIVO'")
    Integer sumEntregasHojeAtivos();

    @Query("SELECT e FROM Entregador e WHERE e.status <> :status")
    List<Entregador> findByStatusNot(@Param("status") StatusEntregador status);

    @Query("SELECT COUNT(e) FROM Entregador e WHERE e.status <> :status")
    long countByStatusNot(@Param("status") StatusEntregador status);

    @Query("SELECT COALESCE(SUM(e.entregasHoje), 0) FROM Entregador e WHERE e.status <> :status")
    Integer sumEntregasHojeByStatusNot(@Param("status") StatusEntregador status);
}