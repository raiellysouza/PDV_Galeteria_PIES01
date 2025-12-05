package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.model.ItemPedido;
import com.example.pdv_galeteria.model.Pedido;
import com.example.pdv_galeteria.model.StatusPedido;
import com.example.pdv_galeteria.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository){
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public Pedido criarPedido(Pedido pedido){
        if (pedido.getStatus() == null) {
            pedido.setStatus(StatusPedido.REGISTRADO);
        }
        pedido.recalcularTotal();
        return pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long id){
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllByOrderByCriadoEmDesc();
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPorStatus(StatusPedido status) {
        return pedidoRepository.findByStatusOrderByCriadoEmDesc(status);
    }

    @Transactional
    public Pedido atualizar(Long id, Pedido pedidoDetalhes) {
        Pedido pedidoExistente = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (pedidoDetalhes.getCliente() != null) {
            pedidoExistente.setCliente(pedidoDetalhes.getCliente());
        }
        if (pedidoDetalhes.getFormaPagamento() != null) {
            pedidoExistente.setFormaPagamento(pedidoDetalhes.getFormaPagamento());
        }
        if (pedidoDetalhes.getTipoEntrega() != null) {
            pedidoExistente.setTipoEntrega(pedidoDetalhes.getTipoEntrega());
        }

        if (pedidoDetalhes.getItens() != null && !pedidoDetalhes.getItens().isEmpty()) {
            pedidoExistente.getItens().clear();
            for (ItemPedido item : pedidoDetalhes.getItens()) {
                pedidoExistente.addItem(item);
            }
        }

        pedidoExistente.recalcularTotal();

        return pedidoRepository.save(pedidoExistente);
    }

    @Transactional
    public Pedido atualizarStatus(Long id, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        
        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public void excluirPedido(Long id){
        if(!pedidoRepository.existsById(id)){
            throw new RuntimeException("Pedido não encontrado");
        }
        pedidoRepository.deleteById(id);
    }
}

