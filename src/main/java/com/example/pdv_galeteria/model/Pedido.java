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

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "endereco", length = 255)
    private String endereco;

    @Column(name = "ponto_referencia", length = 255)
    private String pontoReferencia;

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @Column(name = "valor_pago")
    private Double valorPago;

    @Column(name = "troco")
    private Double troco;

    @Column(name = "tempo_estimado", length = 20)
    private String tempoEstimado;

    @Column(name = "taxa_entrega")
    private Double taxaEntrega;

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
    public String getTelefone(){return telefone;}
    public void setTelefone(String telefone){this.telefone = telefone;}
    public String getEndereco(){return endereco;}
    public void setEndereco(String endereco){this.endereco = endereco;}
    public String getPontoReferencia(){return pontoReferencia;}
    public void setPontoReferencia(String pontoReferencia){this.pontoReferencia = pontoReferencia;}
    public String getObservacoes(){return observacoes;}
    public void setObservacoes(String observacoes){this.observacoes = observacoes;}
    public Double getValorPago(){return valorPago;}
    public void setValorPago(Double valorPago){this.valorPago = valorPago;}
    public Double getTroco(){return troco;}
    public void setTroco(Double troco){this.troco = troco;}
    public String getTempoEstimado(){return tempoEstimado;}
    public void setTempoEstimado(String tempoEstimado){this.tempoEstimado = tempoEstimado;}
    public Double getTaxaEntrega(){return taxaEntrega;}
    public void setTaxaEntrega(Double taxaEntrega){this.taxaEntrega = taxaEntrega;}
    public List<ItemPedido> getItens(){return itens;}
    public void setItens(List<ItemPedido> itens){this.itens = itens;}
}