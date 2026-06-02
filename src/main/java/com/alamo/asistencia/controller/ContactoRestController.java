package com.alamo.asistencia.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.alamo.asistencia.model.Contacto;
import com.alamo.asistencia.service.ContactoEtiquetaService;
import com.alamo.asistencia.service.ContactoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contactos")
@RequiredArgsConstructor
public class ContactoRestController {

    private final ContactoService contactoService;
    private final ContactoEtiquetaService contactoEtiquetaService;

    // =========================
    // LISTAR / BUSCAR (ACTIVOS)
    // GET /api/contactos?q=
    // =========================
    @GetMapping
    public ResponseEntity<List<Contacto>> listar(
            @RequestParam(required = false, defaultValue = "") String q
    ) {
        return ResponseEntity.ok(contactoService.listarActivos(q));
    }

    // =========================
    // LISTAR / BUSCAR (ARCHIVADOS)
    // GET /api/contactos/archivados?q=
    // =========================
    @GetMapping("/archivados")
    public ResponseEntity<List<Contacto>> listarArchivados(
            @RequestParam(required = false, defaultValue = "") String q
    ) {
        return ResponseEntity.ok(contactoService.listarArchivados(q));
    }

    // =========================
    // LISTAR POR ETIQUETA (ACTIVOS + búsqueda opcional)
    // GET /api/contactos/etiqueta/{idEtiqueta}?q=
    // =========================
    @GetMapping("/etiqueta/{idEtiqueta}")
    public ResponseEntity<List<Contacto>> listarPorEtiqueta(
            @PathVariable Integer idEtiqueta,
            @RequestParam(required = false, defaultValue = "") String q
    ) {
        return ResponseEntity.ok(contactoService.listarActivosPorEtiqueta(idEtiqueta, q));
    }

    // =========================
    // OBTENER 1 CONTACTO (activo o archivado)
    // GET /api/contactos/{id}
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<Contacto> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(contactoService.obtenerPorId(id));
    }

    // =========================
    // CREAR CONTACTO
    // POST /api/contactos
    // =========================
    @PostMapping
    public ResponseEntity<Contacto> crear(@RequestBody Contacto c) {
        return ResponseEntity.ok(contactoService.crear(c));
    }

    // =========================
    // ACTUALIZAR CONTACTO
    // PUT /api/contactos/{id}
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<Contacto> actualizar(
            @PathVariable Integer id,
            @RequestBody Contacto c
    ) {
        return ResponseEntity.ok(contactoService.actualizar(id, c));
    }

    // =========================
    // SUBIR / ACTUALIZAR FOTO
    // POST /api/contactos/{id}/foto
    // multipart/form-data: file
    // =========================
    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Contacto> subirFoto(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Debes enviar un archivo en el campo 'file'.");
        }
        return ResponseEntity.ok(contactoService.guardarFoto(id, file));
    }

    // =========================
    // ARCHIVAR (Soft delete)
    // DELETE /api/contactos/{id}
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archivar(@PathVariable Integer id) {
        contactoService.archivar(id);
        return ResponseEntity.noContent().build(); // 204
    }

    // =========================
    // DES-ARCHIVAR / RESTAURAR
    // PATCH /api/contactos/{id}/activar
    // PATCH /api/contactos/{id}/restaurar
    // =========================
    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Integer id) {
        contactoService.activar(id);
        return ResponseEntity.noContent().build(); // 204
    }

    @PatchMapping("/{id}/restaurar")
    public ResponseEntity<Void> restaurar(@PathVariable Integer id) {
        contactoService.activar(id);
        return ResponseEntity.noContent().build(); // 204
    }

    // =========================
    // LISTAR ETIQUETAS DE UN CONTACTO (IDs)
    // GET /api/contactos/{idContacto}/etiquetas
    // =========================
    @GetMapping("/{idContacto}/etiquetas")
    public ResponseEntity<List<Integer>> listarEtiquetasDeContacto(@PathVariable Integer idContacto) {
        return ResponseEntity.ok(contactoEtiquetaService.listarIdsEtiquetasDeContacto(idContacto));
    }

    // =========================
    // ASIGNAR ETIQUETA A CONTACTO
    // POST /api/contactos/{idContacto}/etiquetas/{idEtiqueta}
    // =========================
    @PostMapping("/{idContacto}/etiquetas/{idEtiqueta}")
    public ResponseEntity<Void> asignarEtiqueta(
            @PathVariable Integer idContacto,
            @PathVariable Integer idEtiqueta
    ) {
        contactoEtiquetaService.asignarEtiqueta(idContacto, idEtiqueta);
        return ResponseEntity.noContent().build(); // 204
    }

    // =========================
    // QUITAR ETIQUETA A CONTACTO
    // DELETE /api/contactos/{idContacto}/etiquetas/{idEtiqueta}
    // =========================
    @DeleteMapping("/{idContacto}/etiquetas/{idEtiqueta}")
    public ResponseEntity<Void> quitarEtiqueta(
            @PathVariable Integer idContacto,
            @PathVariable Integer idEtiqueta
    ) {
        contactoEtiquetaService.quitarEtiqueta(idContacto, idEtiqueta);
        return ResponseEntity.noContent().build(); // 204
    }
}
