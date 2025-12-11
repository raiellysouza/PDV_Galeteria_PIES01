package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.Pedido;
import com.example.pdv_galeteria.model.StatusPedido;
import com.example.pdv_galeteria.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    public List<Pedido> listarTodos() {
        return pedidoService.listarTodos();
    }

    public List<Pedido> listarPorStatus(StatusPedido status) {
        return pedidoService.listarPorStatus(status);
    }

    public Optional<Pedido> buscarPorId(Long id) {
        try {
            return Optional.of(pedidoService.buscarPorId(id));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    public Pedido criarPedido(Pedido pedido) {
        return pedidoService.criarPedido(pedido);
    }

    public Optional<Pedido> atualizar(Long id, Pedido pedidoDetalhes) {
        try {
            Pedido pedidoAtualizado = pedidoService.atualizar(id, pedidoDetalhes);
            return Optional.of(pedidoAtualizado);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    public Optional<Pedido> atualizarStatus(Long id, StatusPedido novoStatus) {
        try {
            Pedido pedidoAtualizado = pedidoService.atualizarStatus(id, novoStatus);
            return Optional.of(pedidoAtualizado);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    public void deletar(Long id) {
        pedidoService.excluirPedido(id);
    }
}
