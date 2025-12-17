package com.example.pdv_galeteria.repository;

import com.example.pdv_galeteria.model.Pedido;
import com.example.pdv_galeteria.model.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.itens WHERE p.id = :id")
    Optional<Pedido> buscarPedidoComItens(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.itens WHERE p.id = :id")
    Optional<Pedido> buscarPedidoParaImpressao(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.itens ORDER BY p.criadoEm DESC")
    List<Pedido> findAllWithItens();

    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.itens WHERE p.status = :status ORDER BY p.criadoEm DESC")
    List<Pedido> findByStatusWithItens(@Param("status") StatusPedido status);

    @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.formasPagamento WHERE p.id = :id")
    Optional<Pedido> findByIdWithFormasPagamento(@Param("id") Long id);

    List<Pedido> findByStatusOrderByCriadoEmDesc(StatusPedido status);

    List<Pedido> findAllByOrderByCriadoEmDesc();
}