package com.example.pdv_galeteria.repository;

import com.example.pdv_galeteria.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByNomeContainingIgnoreCase(String nome);

    Optional<Produto> findByNomeIgnoreCase(String nome);
}