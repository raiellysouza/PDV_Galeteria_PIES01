package com.example.pdv_galeteria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "itens_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String produto;
    private Integer quantidade;
    private Double precoUnitario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_em_lote_id")
    private VendaEmLote vendaEmLote;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    public ItemPedido(){}

    public ItemPedido(String produto, Integer quantidade, Double precoUnitario){
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public Long getId(){return id;}
    public void setId(Long id){this.id = id;}
    public String getProduto(){return produto;}
    public void setProduto(String produto){this.produto = produto;}
    public Integer getQuantidade(){return quantidade;}
    public void setQuantidade(Integer quantidade){this.quantidade = quantidade;}
    public Double getPrecoUnitario(){return precoUnitario;}
    public void setPrecoUnitario(Double precoUnitario){this.precoUnitario = precoUnitario;}
    public Pedido getPedido(){return pedido;}
    public void setPedido(Pedido pedido){this.pedido = pedido;}

    public VendaEmLote getVendaEmLote(){return vendaEmLote;}
    public void setVendaEmLote(VendaEmLote vendaEmLote){
        this.vendaEmLote = vendaEmLote;}
}
