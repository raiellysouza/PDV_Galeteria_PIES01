package com.example.pdv_galeteria.repository;

import com.example.pdv_galeteria.model.EstoqueEntrada;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class EstoqueEntradaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public EstoqueEntrada salvar(EstoqueEntrada entrada) {
        entityManager.persist(entrada);
        return entrada;
    }

    public List<EstoqueEntrada> listar() {
        return entityManager.createQuery("FROM EstoqueEntrada", EstoqueEntrada.class).getResultList();
    }
}
