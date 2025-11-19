package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.model.ItemPedido;
import com.example.pdv_galeteria.model.Pedido;
import com.example.pdv_galeteria.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository){
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public Pedido criarPedido(Pedido pedido){
        pedido.recalcularTotal();
        return pedidoRepository.save(pedido);
    }

    public Optional<Pedido> buscarPorId(Long id){
        return pedidoRepository.findById(id);
    }
}
