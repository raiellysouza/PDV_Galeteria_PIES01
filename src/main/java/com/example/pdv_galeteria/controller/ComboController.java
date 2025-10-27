package com.example.pdv_galeteria.controller;
/***
 * import com.example.pdv_galeteria.model.Combo;
 * import com.example.pdv_galeteria.service.ComboService;
 * import lombok.Data;
 * import org.springframework.beans.factory.annotation.Autowired;
 * import org.springframework.http.ResponseEntity;
 * import org.springframework.web.bind.annotation.*;
 * import java.util.List;
 * 
 * @Data
 * @RestController
 *                 @RequestMapping("/api/combos")
 *                 public class ComboController {
 * 
 * @Autowired
 *            private ComboService comboService;
 * 
 * @GetMapping
 *             public ResponseEntity<List<Combo>> listarTodos() {
 *             return ResponseEntity.ok(comboService.listarTodos());
 *             }
 * 
 *             @GetMapping("/{id}")
 *             public ResponseEntity<Combo> buscarPorId(@PathVariable Long id) {
 *             return comboService.buscarPorId(id)
 *             .map(ResponseEntity::ok)
 *             .orElse(ResponseEntity.notFound().build());
 *             }
 * 
 * @PostMapping
 *              public ResponseEntity<Combo> salvar(@RequestBody Combo combo) {
 *              Combo novoCombo = comboService.salvar(combo);
 *              return ResponseEntity.ok(novoCombo);
 *              }
 * 
 *              @PutMapping("/{id}")
 *              public ResponseEntity<Combo> atualizar(@PathVariable Long
 *              id, @RequestBody Combo combo) {
 *              return comboService.buscarPorId(id)
 *              .map(existing -> {
 * 
 *              existing.setNome(combo.getNome());
 *              existing.setPreco(combo.getPreco());
 * 
 * 
 *              Combo atualizado = comboService.salvar(existing);
 *              return ResponseEntity.ok(atualizado);
 *              })
 *              .orElse(ResponseEntity.notFound().build());
 *              }
 * 
 *              @DeleteMapping("/{id}")
 *              public ResponseEntity<Void> deletar(@PathVariable Long id) {
 *              comboService.deletar(id);
 *              return ResponseEntity.noContent().build();
 *              }
 *              }
 ***/