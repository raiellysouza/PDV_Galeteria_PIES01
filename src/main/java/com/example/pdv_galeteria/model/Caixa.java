package com.example.pdv_galeteria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "caixa")
public class Caixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_caixa", nullable = false)
    private LocalDate dataCaixa;

    @Column(name = "data_abertura")
    private LocalDateTime dataAbertura;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    @Column(name = "valor_inicial", precision = 10, scale = 2)
    private BigDecimal valorInicial;

    @Column(name = "valor_final", precision = 10, scale = 2)
    private BigDecimal valorFinal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCaixa status;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "caixa", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MovimentoCaixa> movimentos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (dataCaixa == null) {
            dataCaixa = LocalDate.now();
        }
        if (status == null) {
            status = StatusCaixa.FECHADO;
        }
        if (saldoAtual == null) {
            saldoAtual = BigDecimal.ZERO;
        }
        if (totalEntradas == null) {
            totalEntradas = BigDecimal.ZERO;
        }
        if (totalSaidas == null) {
            totalSaidas = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Caixa() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = StatusCaixa.FECHADO;
        this.dataCaixa = LocalDate.now();
    }

    public Caixa(BigDecimal valorInicial) {
        this();
        this.valorInicial = valorInicial;
        this.dataAbertura = LocalDateTime.now();
        this.status = StatusCaixa.ABERTO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDataCaixa() {
        return dataCaixa;
    }

    public void setDataCaixa(LocalDate dataCaixa) {
        this.dataCaixa = dataCaixa;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDateTime getDataFechamento() {
        return dataFechamento;
    }

    public void setDataFechamento(LocalDateTime dataFechamento) {
        this.dataFechamento = dataFechamento;
    }

    public BigDecimal getValorInicial() {
        return valorInicial;
    }

    public void setValorInicial(BigDecimal valorInicial) {
        this.valorInicial = valorInicial;
    }

    public BigDecimal getValorFinal() {
        return valorFinal;
    }

    public void setValorFinal(BigDecimal valorFinal) {
        this.valorFinal = valorFinal;
    }

    public StatusCaixa getStatus() {
        return status;
    }

    public void setStatus(StatusCaixa status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public BigDecimal getSaldoAtual() {
        BigDecimal base = (this.valorInicial != null ? this.valorInicial : BigDecimal.ZERO);
        return base.add(getTotalEntradas()).subtract(getTotalSaidas());
    }

    public BigDecimal getTotalEntradas() {
        return movimentos.stream()
                .filter(m -> m.getTipo() == TipoMovimentoCaixa.ENTRADA)
                .map(MovimentoCaixa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalSaidas() {
        return movimentos.stream()
                .filter(m -> m.getTipo() == TipoMovimentoCaixa.SAIDA)
                .map(MovimentoCaixa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<MovimentoCaixa> getMovimentos() {
        return movimentos;
    }

    public void setMovimentos(List<MovimentoCaixa> movimentos) {
        this.movimentos = movimentos;
    }
}
