package com.example.pdv_galeteria.controller;

import com.example.pdv_galeteria.dto.AbrirCaixaDTO;
import com.example.pdv_galeteria.dto.FecharCaixaDTO;
import com.example.pdv_galeteria.dto.StatusCaixaDTO;
import com.example.pdv_galeteria.model.Caixa;
import com.example.pdv_galeteria.service.CaixaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/caixa")
@CrossOrigin(origins = "*")
public class CaixaController {

    @Autowired
    private CaixaService caixaService;

    @PostMapping("/abrir")
    public ResponseEntity<?> abrirCaixa(@RequestBody AbrirCaixaDTO abrirCaixaDTO) {
        try {
            Caixa caixa = caixaService.abrirCaixa(abrirCaixaDTO.getValorInicial(), abrirCaixaDTO.getObservacoes());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Caixa aberto com sucesso");
            response.put("data", caixa);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/fechar")
    public ResponseEntity<?> fecharCaixa(@RequestBody FecharCaixaDTO fecharCaixaDTO) {
        try {
            Caixa caixa = caixaService.fecharCaixa(fecharCaixaDTO.getObservacoes());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Caixa fechado com sucesso");
            response.put("data", caixa);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatusCaixa() {
        try {
            boolean caixaExiste = caixaService.existeCaixaDoDia();
            boolean caixaAberto = caixaService.existeCaixaAbertoDoDia();

            String textoBotao;
            if (!caixaExiste) {
                textoBotao = "Abrir Caixa";
            } else if (caixaAberto) {
                textoBotao = "Fechar Caixa";
            } else {
                textoBotao = "Caixa Já Fechado";
            }

            StatusCaixaDTO status = new StatusCaixaDTO();
            status.setCaixaExiste(caixaExiste);
            status.setCaixaAberto(caixaAberto);
            status.setTextoBotao(textoBotao);

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}