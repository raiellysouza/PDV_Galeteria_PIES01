package com.example.pdv_galeteria.repository;

import com.example.pdv_galeteria.model.Pedido;
import com.example.pdv_galeteria.model.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    List<Pedido> findByStatusOrderByCriadoEmDesc(StatusPedido status);

    List<Pedido> findAllByOrderByCriadoEmDesc();
}
