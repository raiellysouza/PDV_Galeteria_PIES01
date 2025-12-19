package com.example.pdv_galeteria.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "entregadores")
public class Entregador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusEntregador status = StatusEntregador.DISPONIVEL;

    @Column(name = "entregas_hoje")
    private Integer entregasHoje = 0;

    @OneToMany(mappedBy = "entregador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Entrega> entregas = new ArrayList<>();

    public Entregador() {}

    public Entregador(String nome, String telefone) {
        this.nome = nome;
        this.telefone = telefone;
        this.status = StatusEntregador.DISPONIVEL;
        this.entregasHoje = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public StatusEntregador getStatus() {
        return status;
    }

    public void setStatus(StatusEntregador status) {
        this.status = status;
    }

    public Integer getEntregasHoje() {
        return entregasHoje;
    }

    public void setEntregasHoje(Integer entregasHoje) {
        this.entregasHoje = entregasHoje;
    }

    public List<Entrega> getEntregas() {
        return entregas;
    }

    public void setEntregas(List<Entrega> entregas) {
        this.entregas = entregas;
    }

    public boolean isAtivo() {
        return status != StatusEntregador.INATIVO;
    }

    public boolean isDisponivel() {
        return status == StatusEntregador.DISPONIVEL;
    }

    @Override
    public String toString() {
        return "Entregador{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", telefone='" + telefone + '\'' +
                ", status=" + status +
                ", entregasHoje=" + entregasHoje +
                '}';
    }
}