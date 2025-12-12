package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.model.ItemPedido;
import com.example.pdv_galeteria.model.VendaEmLote;
import com.example.pdv_galeteria.service.VendaEmLoteService;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class VendaEmLoteController {

    private final VendaEmLoteService vendaEmLoteService;

    public VendaEmLoteController(VendaEmLoteService vendaEmLoteService) {
        this.vendaEmLoteService = vendaEmLoteService;
    }

    public void criarVendaEmLote(String origem, List<ItemPedido> itens) {
        VendaEmLote venda = new VendaEmLote();
        venda.setOrigem(origem);
        venda.setCriadoEm(LocalDateTime.now());
        venda.setItens(itens);

        for (ItemPedido item : itens) {
            item.setVendaEmLote(venda);
        }

        venda.recalcularTotal();

        vendaEmLoteService.criarVendaEmLote(venda);
    }
}
