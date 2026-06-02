package com.alamo.asistencia.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alamo.asistencia.model.Contacto;
import com.alamo.asistencia.repository.IContactoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContactoService {

    private final IContactoRepository contactoRepo;

    @Value("${file.base-dir:uploads}")
    private String baseDir;

    // =========================
    // LISTAR / BUSCAR
    // =========================
    @Transactional(readOnly = true)
    public List<Contacto> listarActivos(String q) {
        return contactoRepo.buscarActivos(trimOrNull(q));
    }

    @Transactional(readOnly = true)
    public List<Contacto> listarArchivados(String q) {
        return contactoRepo.buscarArchivados(trimOrNull(q));
    }

    @Transactional(readOnly = true)
    public List<Contacto> listarActivosPorEtiqueta(Integer idEtiqueta, String q) {
        if (idEtiqueta == null) throw new RuntimeException("idEtiqueta es obligatorio.");
        return contactoRepo.listarPorEtiqueta(idEtiqueta, trimOrNull(q));
    }

    // =========================
    // CRUD
    // =========================
    @Transactional(readOnly = true)
    public Contacto obtenerPorId(Integer idContacto) {
        return contactoRepo.findById(idContacto)
                .orElseThrow(() -> new RuntimeException("Contacto no encontrado: " + idContacto));
    }

    @Transactional
    public Contacto crear(Contacto c) {
        if (c == null) throw new RuntimeException("Contacto inválido.");

        // Normalización (trim seguro)
        c.setPrimer_nombre(trimOrNull(c.getPrimer_nombre()));
        c.setSegundo_nombre(trimOrNull(c.getSegundo_nombre()));
        c.setApellido_paterno(trimOrNull(c.getApellido_paterno()));
        c.setApellido_materno(trimOrNull(c.getApellido_materno()));
        c.setAlias(trimOrNull(c.getAlias()));
        c.setCorreo(trimOrNull(c.getCorreo()));

        c.setTelefono_principal(trimOrNull(c.getTelefono_principal()));
        c.setNumero_secundario(trimOrNull(c.getNumero_secundario()));
        c.setCelular_whatsapp(trimOrNull(c.getCelular_whatsapp()));
        c.setTelefono_fijo(trimOrNull(c.getTelefono_fijo()));

        c.setDepartamento(trimOrNull(c.getDepartamento()));
        c.setProvincia(trimOrNull(c.getProvincia()));
        c.setDistrito(trimOrNull(c.getDistrito()));

        // ✅ Dirección
        c.setDireccion(trimOrNull(c.getDireccion()));
        c.setDireccion_exacta(trimOrNull(c.getDireccion_exacta()));

        // ✅ Fecha nacimiento (LocalDate, no trim)
        c.setFecha_nacimiento(c.getFecha_nacimiento());

        c.setTrabajo(trimOrNull(c.getTrabajo()));

        // ✅ Cargo / Institución
        c.setCargo(trimOrNull(c.getCargo()));
        c.setInstitucion_cargo(trimOrNull(c.getInstitucion_cargo()));

        // ✅✅✅ FECHAS DEL CARGO (AHORA SÍ)
        c.setFecha_inicio_cargo(c.getFecha_inicio_cargo());
        c.setFecha_fin_cargo(c.getFecha_fin_cargo());

        c.setCategoria(trimOrNull(c.getCategoria()));
        c.setProfesion(trimOrNull(c.getProfesion()));
        c.setReferencia(trimOrNull(c.getReferencia()));

        c.setParentesco_enrique(trimOrNull(c.getParentesco_enrique()));
        c.setLenguaje_enrique(trimOrNull(c.getLenguaje_enrique()));
        c.setDeno_enrique(trimOrNull(c.getDeno_enrique()));

        c.setDato_importante(trimOrNull(c.getDato_importante()));
        c.setGustos(trimOrNull(c.getGustos()));
        c.setContactabilidad_alterna(trimOrNull(c.getContactabilidad_alterna()));

        // ✅ foto_url NO se setea desde el form
        c.setEventos_detalle(trimOrNull(c.getEventos_detalle()));

        // DNI único
        String dni = trimOrNull(c.getDni());
        if (dni != null) {
            c.setDni(dni);
            contactoRepo.findByDni(dni).ifPresent(x -> {
                throw new RuntimeException("Ya existe un contacto con DNI: " + dni);
            });
        } else {
            c.setDni(null);
        }

        // defaults
        c.setEstado(true);
        if (c.getAsistio_eventos() == null) c.setAsistio_eventos(false);

        if (c.getPrimer_nombre() == null) {
            throw new RuntimeException("El primer nombre es obligatorio.");
        }

        return contactoRepo.save(c);
    }

    @Transactional
    public Contacto actualizar(Integer idContacto, Contacto nuevo) {
        if (nuevo == null) throw new RuntimeException("Contacto inválido.");

        Contacto actual = obtenerPorId(idContacto);

        // DNI
        String dniNuevo = trimOrNull(nuevo.getDni());
        if (dniNuevo != null) {
            if (actual.getDni() == null || !dniNuevo.equals(actual.getDni())) {
                contactoRepo.findByDni(dniNuevo).ifPresent(x -> {
                    throw new RuntimeException("Ya existe un contacto con DNI: " + dniNuevo);
                });
                actual.setDni(dniNuevo);
            }
        } else {
            actual.setDni(null);
        }

        // primer nombre obligatorio
        String primerNombre = trimOrNull(nuevo.getPrimer_nombre());
        if (primerNombre == null) throw new RuntimeException("El primer nombre es obligatorio.");
        actual.setPrimer_nombre(primerNombre);

        // resto
        actual.setSegundo_nombre(trimOrNull(nuevo.getSegundo_nombre()));
        actual.setApellido_paterno(trimOrNull(nuevo.getApellido_paterno()));
        actual.setApellido_materno(trimOrNull(nuevo.getApellido_materno()));
        actual.setAlias(trimOrNull(nuevo.getAlias()));
        actual.setCorreo(trimOrNull(nuevo.getCorreo()));

        actual.setTelefono_principal(trimOrNull(nuevo.getTelefono_principal()));
        actual.setNumero_secundario(trimOrNull(nuevo.getNumero_secundario()));
        actual.setCelular_whatsapp(trimOrNull(nuevo.getCelular_whatsapp()));
        actual.setTelefono_fijo(trimOrNull(nuevo.getTelefono_fijo()));

        actual.setDepartamento(trimOrNull(nuevo.getDepartamento()));
        actual.setProvincia(trimOrNull(nuevo.getProvincia()));
        actual.setDistrito(trimOrNull(nuevo.getDistrito()));

        // ✅ Dirección
        actual.setDireccion(trimOrNull(nuevo.getDireccion()));
        actual.setDireccion_exacta(trimOrNull(nuevo.getDireccion_exacta()));

        // ✅ Fechas
        actual.setFecha_nacimiento(nuevo.getFecha_nacimiento());

        // ✅ Cargo
        actual.setTrabajo(trimOrNull(nuevo.getTrabajo()));
        actual.setCargo(trimOrNull(nuevo.getCargo()));
        actual.setInstitucion_cargo(trimOrNull(nuevo.getInstitucion_cargo()));

        // ✅✅✅ FECHAS DEL CARGO (AHORA SÍ)
        actual.setFecha_inicio_cargo(nuevo.getFecha_inicio_cargo());
        actual.setFecha_fin_cargo(nuevo.getFecha_fin_cargo());

        actual.setCategoria(trimOrNull(nuevo.getCategoria()));
        actual.setProfesion(trimOrNull(nuevo.getProfesion()));
        actual.setReferencia(trimOrNull(nuevo.getReferencia()));

        actual.setParentesco_enrique(trimOrNull(nuevo.getParentesco_enrique()));
        actual.setLenguaje_enrique(trimOrNull(nuevo.getLenguaje_enrique()));
        actual.setDeno_enrique(trimOrNull(nuevo.getDeno_enrique()));

        actual.setDato_importante(trimOrNull(nuevo.getDato_importante()));
        actual.setGustos(trimOrNull(nuevo.getGustos()));
        actual.setContactabilidad_alterna(trimOrNull(nuevo.getContactabilidad_alterna()));

        // foto_url no
        actual.setAsistio_eventos(nuevo.getAsistio_eventos() != null ? nuevo.getAsistio_eventos() : false);
        actual.setEventos_detalle(trimOrNull(nuevo.getEventos_detalle()));

        return contactoRepo.save(actual);
    }

    // =========================
    // FOTO (UPLOAD)
    // =========================
    @Transactional
    public Contacto guardarFoto(Integer idContacto, MultipartFile file) {
        if (idContacto == null) throw new RuntimeException("idContacto es obligatorio.");
        if (file == null || file.isEmpty()) throw new RuntimeException("No se recibió archivo.");

        Contacto c = obtenerPorId(idContacto);

        String contentType = (file.getContentType() == null) ? "" : file.getContentType().toLowerCase();
        if (!contentType.startsWith("image/")) {
            throw new RuntimeException("El archivo debe ser una imagen (JPG/PNG/WEBP).");
        }

        long maxBytes = 2L * 1024 * 1024; // 2MB
        if (file.getSize() > maxBytes) {
            throw new RuntimeException("La imagen excede 2MB.");
        }

        String ext = extensionSegura(contentType, file.getOriginalFilename());

        try {
            Path dir = Paths.get(baseDir, "contactos").toAbsolutePath().normalize();
            Files.createDirectories(dir);

            borrarSiExiste(dir.resolve(idContacto + ".jpg"));
            borrarSiExiste(dir.resolve(idContacto + ".jpeg"));
            borrarSiExiste(dir.resolve(idContacto + ".png"));
            borrarSiExiste(dir.resolve(idContacto + ".webp"));

            Path target = dir.resolve(idContacto + "." + ext);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            String url = "/uploads/contactos/" + idContacto + "." + ext
                    + "?v=" + Instant.now().toEpochMilli();

            c.setFoto_url(url);
            return contactoRepo.save(c);

        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar la imagen: " + e.getMessage());
        }
    }

    private void borrarSiExiste(Path p) throws IOException {
        if (p != null && Files.exists(p)) {
            Files.delete(p);
        }
    }

    private String extensionSegura(String contentType, String originalName) {
        String ct = (contentType == null) ? "" : contentType.toLowerCase();

        if (ct.contains("png")) return "png";
        if (ct.contains("webp")) return "webp";
        if (ct.contains("jpeg") || ct.contains("jpg")) return "jpg";

        String name = (originalName == null) ? "" : originalName.toLowerCase().trim();
        if (name.endsWith(".png")) return "png";
        if (name.endsWith(".webp")) return "webp";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "jpg";

        throw new RuntimeException("Formato no permitido. Usa JPG, PNG o WEBP.");
    }

    // =========================
    // SOFT DELETE
    // =========================
    @Transactional
    public void archivar(Integer idContacto) {
        Contacto c = obtenerPorId(idContacto);
        c.setEstado(false);
        contactoRepo.save(c);
    }

    @Transactional
    public void activar(Integer idContacto) {
        Contacto c = obtenerPorId(idContacto);
        c.setEstado(true);
        contactoRepo.save(c);
    }

    // =========================
    // Utils
    // =========================
    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}