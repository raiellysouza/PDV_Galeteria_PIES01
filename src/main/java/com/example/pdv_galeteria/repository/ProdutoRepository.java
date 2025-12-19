package com.example.pdv_galeteria.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.pdv_galeteria.model.Produto;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByAtivoTrue();
    List<Produto> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome);
    Optional<Produto> findByNomeIgnoreCaseAndAtivoTrue(String nome);
    Optional<Produto> findByNomeIgnoreCase(String nome);

    List<Produto> findByCategoriaAndAtivoTrue(String categoria);

    List<Produto> findByQuantidadeLessThanAndAtivoTrue(Integer quantidade);

    List<Produto> findByPrecoBetweenAndAtivoTrue(Double precoMin, Double precoMax);
}