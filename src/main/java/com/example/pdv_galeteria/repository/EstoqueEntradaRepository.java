package com.example.pdv_galeteria.repository;

import com.example.pdv_galeteria.model.EstoqueEntrada;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class EstoqueEntradaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public EstoqueEntrada save(EstoqueEntrada entrada) {
        if (entrada.getId() == null) {
            entityManager.persist(entrada);
            return entrada;
        } else {
            return entityManager.merge(entrada);
        }
    }

    public List<EstoqueEntrada> findAll() {
        return entityManager
                .createQuery("SELECT e FROM EstoqueEntrada e", EstoqueEntrada.class)
                .getResultList();
    }
}