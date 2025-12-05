package com.example.pdv_galeteria.repository;

import com.example.pdv_galeteria.model.Combo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboRepository extends JpaRepository<Combo, Long> {

    List<Combo> findByNomeContainingIgnoreCase(String nome);
}