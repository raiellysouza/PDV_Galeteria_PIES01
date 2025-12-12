package com.example.pdv_galeteria.repository;

import com.example.pdv_galeteria.model.MovimentoCaixa;
import com.example.pdv_galeteria.model.TipoMovimentoCaixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MovimentoCaixaRepository extends JpaRepository<MovimentoCaixa, Long> {

    List<MovimentoCaixa> findByCaixaIdOrderByDataHoraAsc(Long caixaId);

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM MovimentoCaixa m WHERE m.caixa.id = :caixaId AND m.tipo = :tipo")
    BigDecimal somarPorTipo(Long caixaId, TipoMovimentoCaixa tipo);
}

