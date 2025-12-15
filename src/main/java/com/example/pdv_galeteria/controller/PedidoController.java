package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.Pedido;
import com.example.pdv_galeteria.model.StatusPedido;
import com.example.pdv_galeteria.service.PedidoService;
import com.example.pdv_galeteria.service.ImpressaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Component
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ImpressaoService impressaoService;

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

 
    public boolean imprimirComanda(Long pedidoId) {
        try {
            Optional<Pedido> pedidoOpt = buscarPorId(pedidoId);
            if (pedidoOpt.isPresent()) {
                return impressaoService.imprimirComanda(pedidoOpt.get());
            }
            return false;
        } catch (Exception e) {
            System.err.println("Erro ao imprimir comanda do pedido #" + pedidoId + ": " + e.getMessage());
            return false;
        }
    }

 
    public File gerarArquivoImpressao(Long pedidoId) {
        try {
            Optional<Pedido> pedidoOpt = buscarPorId(pedidoId);
            if (pedidoOpt.isPresent()) {
                return impressaoService.salvarArquivoImpressao(pedidoOpt.get());
            }
            return null;
        } catch (Exception e) {
            System.err.println("Erro ao gerar arquivo de impressão do pedido #" + pedidoId + ": " + e.getMessage());
            return null;
        }
    }


    public String obterPreviewComanda(Long pedidoId) {
        try {
            Optional<Pedido> pedidoOpt = buscarPorId(pedidoId);
            if (pedidoOpt.isPresent()) {
                return impressaoService.gerarPreviewComanda(pedidoOpt.get());
            }
            return null;
        } catch (Exception e) {
            System.err.println("Erro ao gerar preview da comanda do pedido #" + pedidoId + ": " + e.getMessage());
            return null;
        }
    }


    public File salvarPreviewComanda(Long pedidoId) {
        try {
            Optional<Pedido> pedidoOpt = buscarPorId(pedidoId);
            if (pedidoOpt.isPresent()) {
                return impressaoService.salvarPreviewComanda(pedidoOpt.get());
            }
            return null;
        } catch (Exception e) {
            System.err.println("Erro ao salvar preview da comanda do pedido #" + pedidoId + ": " + e.getMessage());
            return null;
        }
    }
}
