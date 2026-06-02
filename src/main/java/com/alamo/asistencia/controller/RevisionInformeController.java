package com.alamo.asistencia.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.alamo.asistencia.service.RevisionInformeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/revision-informes")
@RequiredArgsConstructor
public class RevisionInformeController {

    private final RevisionInformeService service;

    @GetMapping
    public ResponseEntity<?> get(@RequestParam Integer anio, @RequestParam Integer mes) {
        return ResponseEntity.ok(Map.of(
                "anio", anio,
                "mes", mes,
                "revisados", service.obtenerMapRevisados(anio, mes)
        ));
    }

    @PostMapping
    public ResponseEntity<?> post(@RequestBody Map<String, Object> req) {

        Integer idUsuario = (req.get("idUsuario") instanceof Number) ? ((Number) req.get("idUsuario")).intValue() : null;
        Integer anio = (req.get("anio") instanceof Number) ? ((Number) req.get("anio")).intValue() : null;
        Integer mes = (req.get("mes") instanceof Number) ? ((Number) req.get("mes")).intValue() : null;
        Boolean revisado = (req.get("revisado") instanceof Boolean) ? (Boolean) req.get("revisado") : null;

        if (idUsuario == null || anio == null || mes == null || revisado == null) {
            return ResponseEntity.badRequest().body("Faltan campos obligatorios");
        }

        boolean val = service.upsert(idUsuario, anio, mes, revisado);
        return ResponseEntity.ok(Map.of("ok", true, "revisado", val));
    }
}