package com.example.pdv_galeteria.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.pdv_galeteria.dto.RelatorioMovimentoCaixaDTO;
import com.example.pdv_galeteria.model.MovimentoCaixa;
import com.example.pdv_galeteria.model.TipoMovimentoCaixa;

@Repository
public interface MovimentoCaixaRepository extends JpaRepository<MovimentoCaixa, Long> {

    List<MovimentoCaixa> findByCaixaIdOrderByDataHoraDesc(Long caixaId);

    List<MovimentoCaixa> findByCaixaIdOrderByDataHoraAsc(Long caixaId);

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM MovimentoCaixa m WHERE m.caixa.id = :caixaId AND m.tipo = :tipo")
    BigDecimal somarPorTipo(Long caixaId, TipoMovimentoCaixa tipo);

    List<MovimentoCaixa> findByCaixaId(Long caixaId);

    List<MovimentoCaixa> findByDataHoraBetweenOrderByDataHoraDesc(
            LocalDateTime inicio,
            LocalDateTime fim
    );

    @Query("""
        SELECT new com.example.pdv_galeteria.dto.RelatorioMovimentoCaixaDTO(
            m.id,
            m.dataHora,
            m.valor,
            m.tipo,
            m.descricao
        )
        FROM MovimentoCaixa m
        WHERE m.dataHora BETWEEN :inicio AND :fim
          AND m.tipo = com.example.pdv_galeteria.model.TipoMovimentoCaixa.ENTRADA
        ORDER BY m.dataHora ASC
    """)
    List<RelatorioMovimentoCaixaDTO> buscarMovimentosParaRelatorio(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    List<MovimentoCaixa> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

}
