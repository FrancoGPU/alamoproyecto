package com.alamo.asistencia.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.alamo.asistencia.model.Etiqueta;
import com.alamo.asistencia.service.EtiquetaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/etiquetas")
@RequiredArgsConstructor
public class EtiquetaRestController {

    private final EtiquetaService etiquetaService;

    // =========================
    // LISTAR ETIQUETAS ACTIVAS
    // GET /api/etiquetas
    // =========================
    @GetMapping
    public ResponseEntity<List<Etiqueta>> listar() {
        return ResponseEntity.ok(etiquetaService.listarActivas());
    }

    // =========================
    // OBTENER 1 ETIQUETA
    // GET /api/etiquetas/{id}
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<Etiqueta> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(etiquetaService.obtenerPorId(id));
    }

    // =========================
    // CREAR ETIQUETA
    // POST /api/etiquetas
    // =========================
    @PostMapping
    public ResponseEntity<Etiqueta> crear(@RequestBody Etiqueta e) {
        return ResponseEntity.ok(etiquetaService.crear(e));
    }

    // =========================
    // ACTUALIZAR ETIQUETA
    // PUT /api/etiquetas/{id}
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<Etiqueta> actualizar(@PathVariable Integer id, @RequestBody Etiqueta e) {
        return ResponseEntity.ok(etiquetaService.actualizar(id, e));
    }

    // =========================
    // DESACTIVAR ETIQUETA
    // DELETE /api/etiquetas/{id}
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        etiquetaService.desactivar(id);
        return ResponseEntity.ok().build();
    }

    // =========================
    // ACTIVAR ETIQUETA
    // PATCH /api/etiquetas/{id}/activar
    // =========================
    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Integer id) {
        etiquetaService.activar(id);
        return ResponseEntity.ok().build();
    }
}
