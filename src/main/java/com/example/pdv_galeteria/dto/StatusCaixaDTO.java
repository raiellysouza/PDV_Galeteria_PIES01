package com.example.pdv_galeteria.dto;

public class StatusCaixaDTO {
    private boolean caixaExiste;
    private boolean caixaAberto;
    private String textoBotao;

    // Getters e Setters
    public boolean isCaixaExiste() { return caixaExiste; }
    public void setCaixaExiste(boolean caixaExiste) { this.caixaExiste = caixaExiste; }

    public boolean isCaixaAberto() { return caixaAberto; }
    public void setCaixaAberto(boolean caixaAberto) { this.caixaAberto = caixaAberto; }

    public String getTextoBotao() { return textoBotao; }
    public void setTextoBotao(String textoBotao) { this.textoBotao = textoBotao; }
}