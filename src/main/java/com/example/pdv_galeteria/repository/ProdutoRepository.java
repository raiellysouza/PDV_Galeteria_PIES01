package com.example.pdv_galeteria.repository;

import com.example.pdv_galeteria.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Busca por nome contendo (retorna lista)
    List<Produto> findByNomeContainingIgnoreCase(String nome);

    // Busca por nome exato (retorna Optional)
    Optional<Produto> findByNomeIgnoreCase(String nome);
}