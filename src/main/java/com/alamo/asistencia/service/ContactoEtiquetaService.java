package com.alamo.asistencia.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alamo.asistencia.model.ContactoEtiqueta;
import com.alamo.asistencia.repository.IContactoEtiquetaRepository;
import com.alamo.asistencia.repository.IContactoRepository;
import com.alamo.asistencia.repository.IEtiquetaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContactoEtiquetaService {

    private final IContactoEtiquetaRepository ceRepo;
    private final IContactoRepository contactoRepo;
    private final IEtiquetaRepository etiquetaRepo;

    // =========================
    // ASIGNAR ETIQUETA (idempotente)
    // =========================
    @Transactional
    public void asignarEtiqueta(Integer idContacto, Integer idEtiqueta) {

        if (idContacto == null || idEtiqueta == null) {
            throw new RuntimeException("idContacto e idEtiqueta son obligatorios.");
        }

        // Validación existencia (mensajes claros)
        if (!contactoRepo.existsById(idContacto)) {
            throw new RuntimeException("Contacto no encontrado: " + idContacto);
        }
        if (!etiquetaRepo.existsById(idEtiqueta)) {
            throw new RuntimeException("Etiqueta no encontrada: " + idEtiqueta);
        }

        // Evitar duplicados
        if (ceRepo.existsRelacion(idContacto, idEtiqueta)) return;

        ContactoEtiqueta ce = new ContactoEtiqueta();
        ce.setId_contacto(idContacto);
        ce.setId_etiqueta(idEtiqueta);

        // OJO: NO dependas de setContacto/setEtiqueta
        // a menos que tu Entity realmente los tenga y estén bien mapeados.
        ceRepo.save(ce);
    }

    // =========================
    // QUITAR ETIQUETA (idempotente)
    // =========================
    @Transactional
    public void quitarEtiqueta(Integer idContacto, Integer idEtiqueta) {

        if (idContacto == null || idEtiqueta == null) {
            throw new RuntimeException("idContacto e idEtiqueta son obligatorios.");
        }

        // delete idempotente: si no existe, borra 0 y no pasa nada
        ceRepo.deleteRelacion(idContacto, idEtiqueta);
    }

    // =========================
    // LISTAR IDS DE ETIQUETAS POR CONTACTO
    // =========================
    @Transactional(readOnly = true)
    public List<Integer> listarIdsEtiquetasDeContacto(Integer idContacto) {

        if (idContacto == null) {
            throw new RuntimeException("idContacto es obligatorio.");
        }

        // si quieres error claro:
        if (!contactoRepo.existsById(idContacto)) {
            throw new RuntimeException("Contacto no encontrado: " + idContacto);
        }

        return ceRepo.listarIdsEtiquetasPorContacto(idContacto);
    }
}
