package com.example.pdv_galeteria.repository;

import com.example.pdv_galeteria.model.Entregador;
import com.example.pdv_galeteria.model.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    List<Entrega> findByEntregadorOrderByDataHoraDesc(Entregador entregador);

    boolean existsByNumeroPedido(String numeroPedido);

    @Query("SELECT COUNT(e) FROM Entrega e WHERE e.entregador = :entregador AND FUNCTION('DATE', e.dataHora) = CURRENT_DATE")
    long countByEntregadorAndDataHoraToday(@Param("entregador") Entregador entregador);

    @Query("SELECT e FROM Entrega e WHERE e.entregador = :entregador AND FUNCTION('DATE', e.dataHora) = CURRENT_DATE ORDER BY e.dataHora DESC")
    List<Entrega> findEntregasHojeByEntregador(@Param("entregador") Entregador entregador);

    @Query("SELECT e FROM Entrega e WHERE e.entregador = :entregador AND FUNCTION('DATE', e.dataHora) = :data ORDER BY e.dataHora DESC")
    List<Entrega> findByEntregadorAndData(@Param("entregador") Entregador entregador, @Param("data") LocalDate data);

    @Query("SELECT e FROM Entrega e WHERE e.entregador = :entregador AND e.dataHora >= :startOfDay AND e.dataHora < :endOfDay ORDER BY e.dataHora DESC")
    List<Entrega> findEntregasHojeByEntregadorAlt(
            @Param("entregador") Entregador entregador,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT COUNT(e) FROM Entrega e WHERE e.entregador = :entregador AND e.dataHora >= :startOfDay AND e.dataHora < :endOfDay")
    long countByEntregadorAndDateRange(
            @Param("entregador") Entregador entregador,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}