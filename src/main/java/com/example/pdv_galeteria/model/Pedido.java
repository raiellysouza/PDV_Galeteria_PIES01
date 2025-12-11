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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status = StatusPedido.REGISTRADO;

    @Column(name = "forma_pagamento", length = 50)
    private String formaPagamento;

    @Column(name = "tipo_entrega", length = 50)
    private String tipoEntrega;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    public Pedido() {
        this.status = StatusPedido.REGISTRADO;
    }

    public Pedido(String cliente) {
        this();
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
    public void setCriadoEm(LocalDateTime criadoEm){this.criadoEm = criadoEm;}
    public Double getTotal(){return total;}
    public void setTotal(Double total){this.total = total;}
    public StatusPedido getStatus(){return status;}
    public void setStatus(StatusPedido status){this.status = status;}
    public String getFormaPagamento(){return formaPagamento;}
    public void setFormaPagamento(String formaPagamento){this.formaPagamento = formaPagamento;}
    public String getTipoEntrega(){return tipoEntrega;}
    public void setTipoEntrega(String tipoEntrega){this.tipoEntrega = tipoEntrega;}
    public List<ItemPedido> getItens(){return itens;}
    public void setItens(List<ItemPedido> itens){this.itens = itens;}
}