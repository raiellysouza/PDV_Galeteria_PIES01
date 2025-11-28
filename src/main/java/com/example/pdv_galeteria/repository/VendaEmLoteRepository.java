package com.example.pdv_galeteria.repository;

import com.example.pdv_galeteria.model.VendaEmLote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VendaEmLoteRepository extends JpaRepository<VendaEmLote, Long> {

    List<VendaEmLote> findByCriadoEmBetween(LocalDateTime inicio, LocalDateTime fim);

    List<VendaEmLote> findByOrigemIgnoreCase(String origem);

}
