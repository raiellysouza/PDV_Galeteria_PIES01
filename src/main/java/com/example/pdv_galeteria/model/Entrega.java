package com.example.pdv_galeteria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "entregas")
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "entregador_id", nullable = false)
    private Entregador entregador;

    @Column(name = "numero_pedido", nullable = false, length = 50)
    private String numeroPedido;

    @Column(name = "id_ifood", length = 50)
    private String idIfood;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @PrePersist
    protected void onCreate() {
        dataHora = LocalDateTime.now();
    }

    public Entrega() {}

    public Entrega(Entregador entregador, String numeroPedido) {
        this.entregador = entregador;
        this.numeroPedido = numeroPedido;
        this.dataHora = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Entregador getEntregador() {
        return entregador;
    }

    public void setEntregador(Entregador entregador) {
        this.entregador = entregador;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public String getIdIfood() {
        return idIfood;
    }

    public void setIdIfood(String idIfood) {
        this.idIfood = idIfood;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
        if (pedido != null && pedido.getNumeroPedido() != null) {
            this.numeroPedido = pedido.getNumeroPedido();
        }
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    @Override
    public String toString() {
        return "Entrega{" +
                "id=" + id +
                ", numeroPedido='" + numeroPedido + '\'' +
                ", idIfood='" + idIfood + '\'' +
                ", dataHora=" + dataHora +
                '}';
    }
}