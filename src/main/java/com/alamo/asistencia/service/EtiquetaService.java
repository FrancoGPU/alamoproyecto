package com.alamo.asistencia.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alamo.asistencia.model.Etiqueta;
import com.alamo.asistencia.repository.IEtiquetaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EtiquetaService {

    private final IEtiquetaRepository etiquetaRepo;

    @Transactional(readOnly = true)
    public List<Etiqueta> listarActivas() {
        return etiquetaRepo.findByEstadoTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Etiqueta obtenerPorId(Integer idEtiqueta) {
        return etiquetaRepo.findById(idEtiqueta)
                .orElseThrow(() -> new RuntimeException("Etiqueta no encontrada: " + idEtiqueta));
    }

    @Transactional
    public Etiqueta crear(Etiqueta e) {
        if (e == null) throw new RuntimeException("Etiqueta inválida.");

        String nombre = trimOrNull(e.getNombre());
        if (nombre == null) throw new RuntimeException("El nombre de la etiqueta es obligatorio.");

        etiquetaRepo.findByNombre(nombre).ifPresent(x -> {
            throw new RuntimeException("Ya existe una etiqueta con nombre: " + nombre);
        });

        e.setNombre(nombre);
        e.setColor(trimOrNull(e.getColor()));
        e.setDescripcion(trimOrNull(e.getDescripcion()));
        e.setEstado(true);

        return etiquetaRepo.save(e);
    }

    @Transactional
    public Etiqueta actualizar(Integer idEtiqueta, Etiqueta nuevo) {
        if (nuevo == null) throw new RuntimeException("Etiqueta inválida.");

        Etiqueta actual = obtenerPorId(idEtiqueta);

        String nombre = trimOrNull(nuevo.getNombre());
        if (nombre == null) throw new RuntimeException("El nombre de la etiqueta es obligatorio.");

        if (!nombre.equals(actual.getNombre())) {
            etiquetaRepo.findByNombre(nombre).ifPresent(x -> {
                throw new RuntimeException("Ya existe una etiqueta con nombre: " + nombre);
            });
            actual.setNombre(nombre);
        }

        actual.setColor(trimOrNull(nuevo.getColor()));
        actual.setDescripcion(trimOrNull(nuevo.getDescripcion()));

        return etiquetaRepo.save(actual);
    }

    @Transactional
    public void desactivar(Integer idEtiqueta) {
        Etiqueta e = obtenerPorId(idEtiqueta);
        e.setEstado(false);
        etiquetaRepo.save(e);
    }

    @Transactional
    public void activar(Integer idEtiqueta) {
        Etiqueta e = obtenerPorId(idEtiqueta);
        e.setEstado(true);
        etiquetaRepo.save(e);
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
