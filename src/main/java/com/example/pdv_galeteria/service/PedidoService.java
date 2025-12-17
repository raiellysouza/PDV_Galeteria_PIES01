package com.example.pdv_galeteria.service;

import com.example.pdv_galeteria.model.*;
import com.example.pdv_galeteria.repository.CaixaRepository;
import com.example.pdv_galeteria.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    @Autowired
    @Qualifier("caixaRepository")
    private CaixaRepository caixaRepository;

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

    @Transactional(readOnly = true)
    public List<Pedido> listarTodosComItens() {
        return pedidoRepository.findAllWithItens();
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPorStatusComItens(StatusPedido status) {
        return pedidoRepository.findByStatusWithItens(status);
    }

    @Transactional(readOnly = true)
    public Pedido carregarFormasPagamento(Long id) {
        return pedidoRepository.findByIdWithFormasPagamento(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));
    }

    @Transactional
    public Pedido atualizar(Long id, Pedido pedidoDetalhes) {
        Pedido pedidoExistente = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (pedidoDetalhes.getCliente() != null) {
            pedidoExistente.setCliente(pedidoDetalhes.getCliente());
        }

        if (pedidoDetalhes.getFormasPagamento() != null && !pedidoDetalhes.getFormasPagamento().isEmpty()) {
            pedidoExistente.getFormasPagamento().clear();
            pedidoDetalhes.getFormasPagamento().forEach(fp -> {
                FormaPagamentoPedido novaForma = new FormaPagamentoPedido();
                novaForma.setTipo(fp.getTipo());
                novaForma.setValor(fp.getValor());
                novaForma.setPedido(pedidoExistente);
                pedidoExistente.getFormasPagamento().add(novaForma);
            });
            pedidoExistente.atualizarValorPago();
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

    @Transactional(readOnly = true)
    public Pedido buscarPedidoComItens(Long id) {
        return pedidoRepository.buscarPedidoComItens(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));
    }

    public Pedido buscarPedidoParaImpressao(Long id) {
        return pedidoRepository.buscarPedidoParaImpressao(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));
    }

    public void atualizarCliente(Long pedidoId, String novoCliente) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        pedido.setCliente(novoCliente);
        pedidoRepository.save(pedido);
    }

    public Optional<Caixa> getUltimoCaixaFechadoDoDia() {
        try {
            LocalDate hoje = LocalDate.now();

            List<Caixa> caixasHoje = caixaRepository.findByDataAberturaBetween(
                    hoje.atStartOfDay(),
                    hoje.atTime(23, 59, 59)
            );

            return caixasHoje.stream()
                    .filter(c -> c.getStatus() == StatusCaixa.FECHADO)
                    .max(Comparator.comparing(Caixa::getDataFechamento));

        } catch (Exception e) {
            System.err.println("Erro ao buscar último caixa fechado: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Caixa> getCaixaAtual() {
        return getCaixaAbertoDoDia();
    }

    public Optional<Caixa> getCaixaAbertoDoDia() {
        try {
            LocalDate hoje = LocalDate.now();

            List<Caixa> caixasHoje = caixaRepository.findByDataAberturaBetween(
                    hoje.atStartOfDay(),
                    hoje.atTime(23, 59, 59)
            );

            return caixasHoje.stream()
                    .filter(c -> c.getStatus() == StatusCaixa.ABERTO)
                    .findFirst();

        } catch (Exception e) {
            System.err.println("Erro ao buscar caixa aberto: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPedidosDoDia() {
        LocalDate hoje = LocalDate.now();

        List<Pedido> todosPedidos = pedidoRepository.findAllWithItens();

        return todosPedidos.stream()
                .filter(p -> p.getCriadoEm() != null && p.getCriadoEm().toLocalDate().equals(hoje))
                .collect(Collectors.toList());
    }
}