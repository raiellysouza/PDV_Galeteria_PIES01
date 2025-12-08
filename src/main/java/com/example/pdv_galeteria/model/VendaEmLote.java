package com.example.pdv_galeteria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendasemlote")
public class VendaEmLote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String origem;

    private LocalDateTime criadoEm = LocalDateTime.now();

    private Double total = 0.0;

    @OneToMany(
        mappedBy = "vendaEmLote",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<ItemPedido> itens = new ArrayList<>();

    public VendaEmLote() {}

    public VendaEmLote(String origem) {
        this.origem = origem;
    }

    public void addItem(ItemPedido item) {
        item.setVendaEmLote(this);
        itens.add(item);
        recalcularTotal();
    }

    public void removeItem(ItemPedido item) {
        itens.remove(item);
        item.setVendaEmLote(null);
        recalcularTotal();
    }

    public void recalcularTotal() {
        this.total = itens.stream()
                .mapToDouble(i -> i.getQuantidade() * i.getPrecoUnitario())
                .sum();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }

    public LocalDateTime getCriadoEm() { return criadoEm; }

    public Double getTotal() { return total; }

    public List<ItemPedido> getItens() { return itens; }
}
