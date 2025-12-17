package com.example.pdv_galeteria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cliente;

    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(nullable = false)
    private Double total = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status = StatusPedido.REGISTRADO;

    @Column(name = "forma_pagamento", length = 100)
    private String formaPagamento;

    @Column(name = "detalhes_pagamento", length = 500)
    private String detalhesPagamento;

    @Column(name = "canal_venda", length = 20)
    private String canalVenda = "loja";

    @Column(name = "tipo_entrega", length = 20)
    private String tipoEntrega;

    @Column(length = 200)
    private String endereco;

    @Column(length = 20)
    private String numero;

    @Column(length = 20)
    private String telefone;

    @Column(name = "ponto_referencia", length = 200)
    private String pontoReferencia;

    @Column(length = 500)
    private String observacoes;

    @Column(name = "tempo_estimado", length = 50)
    private String tempoEstimado;

    @Column(name = "tempo_previsao")
    private Integer tempoPrevisao;

    @Column(name = "valor_pago")
    private Double valorPago = 0.0;

    @Column(name = "troco")
    private Double troco = 0.0;

    @Column(name = "desconto_percentual", precision = 5, scale = 2)
    private BigDecimal descontoPercentual = BigDecimal.ZERO;

    @Column(name = "desconto_valor", precision = 10, scale = 2)
    private BigDecimal descontoValor = BigDecimal.ZERO;

    @Column(name = "total_com_desconto", precision = 10, scale = 2)
    private BigDecimal totalComDesconto = BigDecimal.ZERO;

    @Column(name = "taxa_entrega", precision = 10, scale = 2)
    private BigDecimal taxaEntrega = BigDecimal.ZERO;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormaPagamentoPedido> formasPagamento = new ArrayList<>();

    @Column(name = "bairro", length = 100)
    private String bairro;

    @Column(name = "cidade", length = 100)
    private String cidade;

    @Column(name = "estado", length = 2)
    private String estado;

    @Column(name = "cep", length = 10)
    private String cep;

    @Column(name = "complemento", length = 200)
    private String complemento;

    @Column(name = "numero_pedido", unique = true)
    private String numeroPedido;

    @Column(name = "entregador", length = 100)
    private String entregador;

    @Column(name = "status_entrega", length = 20)
    private String statusEntrega;

    @Column(name = "data_entrega")
    private LocalDateTime dataEntrega;

    @Column(name = "data_retirada")
    private LocalDateTime dataRetirada;

    @Column(name = "observacao_interna", length = 500)
    private String observacaoInterna;

    @Column(name = "motivo_cancelamento", length = 500)
    private String motivoCancelamento;

    @Column(name = "data_cancelamento")
    private LocalDateTime dataCancelamento;

    @Column(name = "responsavel_cancelamento", length = 100)
    private String responsavelCancelamento;

    @Column(name = "ultima_atualizacao")
    private LocalDateTime ultimaAtualizacao = LocalDateTime.now();

    public Pedido() {
        this.status = StatusPedido.REGISTRADO;
        this.criadoEm = LocalDateTime.now();
        this.ultimaAtualizacao = LocalDateTime.now();
        this.numeroPedido = gerarNumeroPedido();
    }

    public Pedido(String cliente) {
        this();
        this.cliente = cliente;
    }

    private String gerarNumeroPedido() {
        String data = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", (int)(Math.random() * 10000));
        return "PED-" + data + "-" + random;
    }

    public void addItem(ItemPedido item){
        item.setPedido(this);
        itens.add(item);
        recalcularTotal();
        atualizarUltimaAtualizacao();
    }

    public void removeItem(ItemPedido item){
        itens.remove(item);
        item.setPedido(null);
        recalcularTotal();
        atualizarUltimaAtualizacao();
    }

    public void adicionarFormaPagamento(String tipo, BigDecimal valor) {
        FormaPagamentoPedido forma = new FormaPagamentoPedido();
        forma.setTipo(tipo);
        forma.setValor(valor);
        forma.setPedido(this);
        this.formasPagamento.add(forma);

        atualizarValorPago();
        atualizarUltimaAtualizacao();
    }

    public void removerFormaPagamento(FormaPagamentoPedido formaPagamento) {
        this.formasPagamento.remove(formaPagamento);
        formaPagamento.setPedido(null);
        atualizarValorPago();
        atualizarUltimaAtualizacao();
    }

    public void atualizarValorPago() {
        this.valorPago = formasPagamento.stream()
                .mapToDouble(fp -> fp.getValor().doubleValue())
                .sum();

        BigDecimal totalFinal = getTotalFinal();

        if (this.valorPago > totalFinal.doubleValue()) {
            this.troco = this.valorPago - totalFinal.doubleValue();
        } else {
            this.troco = 0.0;
        }
        atualizarUltimaAtualizacao();
    }

    public void recalcularTotal() {
        BigDecimal totalItens = itens.stream()
                .map(i -> BigDecimal.valueOf(i.getPrecoUnitario() * i.getQuantidade()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.total = totalItens.doubleValue();

        if (descontoPercentual != null && descontoPercentual.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal desconto = totalItens.multiply(descontoPercentual)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            this.totalComDesconto = totalItens.subtract(desconto);
        } else if (descontoValor != null && descontoValor.compareTo(BigDecimal.ZERO) > 0) {
            this.totalComDesconto = totalItens.subtract(descontoValor);
        } else {
            this.totalComDesconto = totalItens;
        }
        atualizarUltimaAtualizacao();
    }

    public void aplicarTaxaEntrega(BigDecimal taxa) {
        this.taxaEntrega = taxa != null ? taxa : BigDecimal.ZERO;
        atualizarUltimaAtualizacao();
    }

    public BigDecimal getTotalFinal() {
        BigDecimal totalBase = getTotalComDesconto() != null &&
                getTotalComDesconto().compareTo(BigDecimal.ZERO) > 0 ?
                getTotalComDesconto() :
                BigDecimal.valueOf(getTotal() != null ? getTotal() : 0.0);

        return totalBase.add(getTaxaEntrega() != null ? getTaxaEntrega() : BigDecimal.ZERO);
    }

    public void cancelar(String motivo, String responsavel) {
        this.status = StatusPedido.CANCELADO;
        this.motivoCancelamento = motivo;
        this.responsavelCancelamento = responsavel;
        this.dataCancelamento = LocalDateTime.now();
        atualizarUltimaAtualizacao();
    }

    public void marcarComoEntregue() {
        this.statusEntrega = "ENTREGUE";
        this.dataEntrega = LocalDateTime.now();
        atualizarUltimaAtualizacao();
    }

    public void marcarComoRetirado() {
        this.statusEntrega = "RETIRADO";
        this.dataRetirada = LocalDateTime.now();
        atualizarUltimaAtualizacao();
    }

    private void atualizarUltimaAtualizacao() {
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) {
        this.cliente = cliente;
        atualizarUltimaAtualizacao();
    }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public Double getTotal() {
        return total != null ? total : 0.0;
    }

    public BigDecimal getTaxaEntrega() {
        return taxaEntrega != null ? taxaEntrega : BigDecimal.ZERO;
    }

    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) {
        this.status = status;
        atualizarUltimaAtualizacao();
    }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
        atualizarUltimaAtualizacao();
    }

    public String getDetalhesPagamento() { return detalhesPagamento; }
    public void setDetalhesPagamento(String detalhesPagamento) {
        this.detalhesPagamento = detalhesPagamento;
        atualizarUltimaAtualizacao();
    }

    public String getCanalVenda() { return canalVenda; }
    public void setCanalVenda(String canalVenda) {
        this.canalVenda = canalVenda;
        atualizarUltimaAtualizacao();
    }

    public String getTipoEntrega() { return tipoEntrega; }
    public void setTipoEntrega(String tipoEntrega) {
        this.tipoEntrega = tipoEntrega;
        atualizarUltimaAtualizacao();
    }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) {
        this.endereco = endereco;
        atualizarUltimaAtualizacao();
    }

    public String getNumero() { return numero; }
    public void setNumero(String numero) {
        this.numero = numero;
        atualizarUltimaAtualizacao();
    }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) {
        this.telefone = telefone;
        atualizarUltimaAtualizacao();
    }

    public String getPontoReferencia() { return pontoReferencia; }
    public void setPontoReferencia(String pontoReferencia) {
        this.pontoReferencia = pontoReferencia;
        atualizarUltimaAtualizacao();
    }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
        atualizarUltimaAtualizacao();
    }

    public String getTempoEstimado() { return tempoEstimado; }
    public void setTempoEstimado(String tempoEstimado) {
        this.tempoEstimado = tempoEstimado;
        atualizarUltimaAtualizacao();
    }

    public Integer getTempoPrevisao() { return tempoPrevisao; }
    public void setTempoPrevisao(Integer tempoPrevisao) {
        this.tempoPrevisao = tempoPrevisao;
        atualizarUltimaAtualizacao();
    }

    public Double getValorPago() { return valorPago; }
    public void setValorPago(Double valorPago) {
        this.valorPago = valorPago;
        BigDecimal totalFinal = getTotalFinal();

        if (this.valorPago > totalFinal.doubleValue()) {
            this.troco = this.valorPago - totalFinal.doubleValue();
        } else {
            this.troco = 0.0;
        }
        atualizarUltimaAtualizacao();
    }

    public Double getTroco() { return troco; }
    public void setTroco(Double troco) {
        this.troco = troco;
        atualizarUltimaAtualizacao();
    }

    public BigDecimal getDescontoPercentual() { return descontoPercentual; }
    public void setDescontoPercentual(BigDecimal descontoPercentual) {
        this.descontoPercentual = descontoPercentual;
        recalcularTotal();
    }

    public BigDecimal getDescontoValor() { return descontoValor; }
    public void setDescontoValor(BigDecimal descontoValor) {
        this.descontoValor = descontoValor;
        recalcularTotal();
    }

    public BigDecimal getTotalComDesconto() { return totalComDesconto; }
    public void setTotalComDesconto(BigDecimal totalComDesconto) {
        this.totalComDesconto = totalComDesconto;
        atualizarUltimaAtualizacao();
    }

    public void setTaxaEntrega(BigDecimal taxaEntrega) {
        this.taxaEntrega = taxaEntrega != null ? taxaEntrega : BigDecimal.ZERO;
        atualizarUltimaAtualizacao();
    }

    public void setTaxaEntrega(double taxa) {
        this.taxaEntrega = BigDecimal.valueOf(taxa);
        atualizarUltimaAtualizacao();
    }

    public List<ItemPedido> getItens() { return itens; }
    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
        recalcularTotal();
    }

    public List<FormaPagamentoPedido> getFormasPagamento() { return formasPagamento; }
    public void setFormasPagamento(List<FormaPagamentoPedido> formasPagamento) {
        this.formasPagamento = formasPagamento;
        atualizarValorPago();
    }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) {
        this.bairro = bairro;
        atualizarUltimaAtualizacao();
    }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) {
        this.cidade = cidade;
        atualizarUltimaAtualizacao();
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) {
        this.estado = estado;
        atualizarUltimaAtualizacao();
    }

    public String getCep() { return cep; }
    public void setCep(String cep) {
        this.cep = cep;
        atualizarUltimaAtualizacao();
    }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) {
        this.complemento = complemento;
        atualizarUltimaAtualizacao();
    }

    public String getNumeroPedido() { return numeroPedido; }
    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
        atualizarUltimaAtualizacao();
    }

    public String getEntregador() { return entregador; }
    public void setEntregador(String entregador) {
        this.entregador = entregador;
        atualizarUltimaAtualizacao();
    }

    public String getStatusEntrega() { return statusEntrega; }
    public void setStatusEntrega(String statusEntrega) {
        this.statusEntrega = statusEntrega;
        atualizarUltimaAtualizacao();
    }

    public LocalDateTime getDataEntrega() { return dataEntrega; }
    public void setDataEntrega(LocalDateTime dataEntrega) {
        this.dataEntrega = dataEntrega;
        atualizarUltimaAtualizacao();
    }

    public LocalDateTime getDataRetirada() { return dataRetirada; }
    public void setDataRetirada(LocalDateTime dataRetirada) {
        this.dataRetirada = dataRetirada;
        atualizarUltimaAtualizacao();
    }

    public String getObservacaoInterna() { return observacaoInterna; }
    public void setObservacaoInterna(String observacaoInterna) {
        this.observacaoInterna = observacaoInterna;
        atualizarUltimaAtualizacao();
    }

    public String getMotivoCancelamento() { return motivoCancelamento; }
    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
        atualizarUltimaAtualizacao();
    }

    public LocalDateTime getDataCancelamento() { return dataCancelamento; }
    public void setDataCancelamento(LocalDateTime dataCancelamento) {
        this.dataCancelamento = dataCancelamento;
        atualizarUltimaAtualizacao();
    }

    public String getResponsavelCancelamento() { return responsavelCancelamento; }
    public void setResponsavelCancelamento(String responsavelCancelamento) {
        this.responsavelCancelamento = responsavelCancelamento;
        atualizarUltimaAtualizacao();
    }

    public LocalDateTime getUltimaAtualizacao() { return ultimaAtualizacao; }
    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) {
        this.ultimaAtualizacao = ultimaAtualizacao;
    }

    public String getFormasPagamentoString() {
        if (formasPagamento.isEmpty()) {
            return formaPagamento != null ? formaPagamento : "Não informado";
        }

        StringBuilder sb = new StringBuilder();
        for (FormaPagamentoPedido fp : formasPagamento) {
            sb.append(fp.getTipo())
                    .append(": R$ ")
                    .append(String.format("%.2f", fp.getValor()))
                    .append("; ");
        }
        return sb.toString();
    }

    public String getEnderecoCompleto() {
        if (endereco == null || endereco.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder(endereco);
        if (numero != null && !numero.isEmpty()) {
            sb.append(", ").append(numero);
        }
        if (complemento != null && !complemento.isEmpty()) {
            sb.append(", ").append(complemento);
        }
        if (bairro != null && !bairro.isEmpty()) {
            sb.append(" - ").append(bairro);
        }
        if (cidade != null && !cidade.isEmpty()) {
            sb.append(", ").append(cidade);
        }
        if (estado != null && !estado.isEmpty()) {
            sb.append("/").append(estado);
        }
        if (cep != null && !cep.isEmpty()) {
            sb.append(" - CEP: ").append(cep);
        }
        if (pontoReferencia != null && !pontoReferencia.isEmpty()) {
            sb.append(" (").append(pontoReferencia).append(")");
        }
        return sb.toString();
    }

    public boolean isEntrega() {
        return "Entrega".equals(tipoEntrega);
    }

    public boolean isRetirada() {
        return "Retirada na Loja".equals(tipoEntrega);
    }

    public boolean isOnline() {
        return "Site".equals(canalVenda) || "Ifood".equals(canalVenda) || "Outro".equals(canalVenda);
    }

    public boolean isLoja() {
        return "Loja".equals(canalVenda);
    }

    @Override
    public String toString() {
        return "Pedido #" + numeroPedido + " - " + cliente + " - R$ " + getTotalFinal();
    }
}