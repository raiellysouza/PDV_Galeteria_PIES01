package com.example.pdv_galeteria.repository;

import com.example.pdv_galeteria.model.Caixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CaixaRepository extends JpaRepository<Caixa, Long> {

    @Query("SELECT c FROM Caixa c WHERE c.dataCaixa = CURRENT_DATE")
    Optional<Caixa> findCaixaDoDia();

    @Query("SELECT COUNT(c) > 0 FROM Caixa c WHERE c.dataCaixa = CURRENT_DATE")
    boolean existsCaixaDoDia();

    @Query("SELECT c FROM Caixa c WHERE c.dataCaixa = CURRENT_DATE AND c.status = 'ABERTO'")
    Optional<Caixa> findCaixaAbertoDoDia();
}