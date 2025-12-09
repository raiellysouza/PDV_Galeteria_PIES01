package com.example.pdv_galeteria.repository;

import com.example.pdv_galeteria.model.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface EstoqueRepository extends JpaRepository<Estoque, Long> {
    Optional<Estoque> findByProdutoId(Long produtoId);
}