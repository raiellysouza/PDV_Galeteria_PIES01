package com.example.pdv_galeteria.repository;

import com.example.pdv_galeteria.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    @Query("SELECT i FROM ItemPedido i WHERE i.pedido.criadoEm BETWEEN :inicio AND :fim")
    List<ItemPedido> findByPedido_CriadoEmBetween(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    List<ItemPedido> findByProduto(String produtoNome);

    List<ItemPedido> findByPedidoId(Long pedidoId);

    @Query("SELECT COALESCE(SUM(i.quantidade), 0) FROM ItemPedido i WHERE i.produto = :produtoNome AND i.pedido.criadoEm BETWEEN :inicio AND :fim")
    Integer sumQuantidadeByProdutoNomeAndPeriodo(
            @Param("produtoNome") String produtoNome,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    @Query("SELECT i FROM ItemPedido i WHERE i.pedido.criadoEm BETWEEN :inicio AND :fim AND i.pedido.status != com.example.pdv_galeteria.model.StatusPedido.CANCELADO")
    List<ItemPedido> findItensAtivosPorPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);
}