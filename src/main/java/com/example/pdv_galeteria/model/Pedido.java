package com.example.pdv_galeteria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cliente;

    private LocalDateTime criadoEm = LocalDateTime.now();

    private Double total = 0.0;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    public Pedido() {}

    public Pedido(String cliente) {
        this.cliente = cliente;
    }

    public void addItem(ItemPedido item){
        item.setPedido(this);
        itens.add(item);
        recalcularTotal();
    }

    public void removeItem(ItemPedido item){
        itens.remove(item);
        item.setPedido(null);
        recalcularTotal();
    }

    public void recalcularTotal(){
        this.total = itens.stream()
                .mapToDouble(i -> i.getQuantidade() * i.getPrecoUnitario())
                .sum();
    }

    public Long getId(){return id;}
    public void setId(Long id){this.id = id;}
    public String getCliente(){return cliente;}
    public void setCliente(String cliente){this.cliente = cliente;}
    public LocalDateTime getCriadoEm(){return criadoEm;}
    public Double getTotal(){return total;}
    public List<ItemPedido> getItens(){return itens;}
}