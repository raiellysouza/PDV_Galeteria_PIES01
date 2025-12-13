package com.example.pdv_galeteria.repository;

import com.example.pdv_galeteria.model.Caixa;
import com.example.pdv_galeteria.model.StatusCaixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CaixaRepository extends JpaRepository<Caixa, Long> {

    @Query("SELECT c FROM Caixa c WHERE c.dataCaixa = CURRENT_DATE")
    Optional<Caixa> findCaixaDoDia();

    @Query("SELECT c FROM Caixa c WHERE c.dataCaixa = CURRENT_DATE AND c.status = 'ABERTO'")
    Optional<Caixa> findCaixaAbertoDoDia();

    @Query("SELECT COUNT(c) > 0 FROM Caixa c WHERE c.dataCaixa = CURRENT_DATE")
    boolean existsCaixaDoDia();

    Optional<Caixa> findByDataCaixa(LocalDate dataCaixa);

    Optional<Caixa> findByDataCaixaAndStatus(LocalDate dataCaixa, StatusCaixa status);

    List<Caixa> findByStatus(StatusCaixa status);

    List<Caixa> findByDataAberturaBetween(LocalDateTime inicio, LocalDateTime fim);
}