package com.alamo.asistencia.service;

import com.alamo.asistencia.model.Actividad;
import com.alamo.asistencia.model.Tarea;
import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.repository.IActividadRepository;
import com.alamo.asistencia.repository.ITareaRepository;
import com.alamo.asistencia.repository.IUsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class TareaService {

    private final ITareaRepository tareaRepo;
    private final IUsuarioRepository usuarioRepo;
    private final IActividadRepository actividadRepo;

    @Value("${file.base-dir:uploads}")
    private String fileBaseDir;

    private static final String ENTREGABLES_SUBDIR = "entregables";

    private static final Set<String> EXT_PERMITIDAS = Set.of(
            "pdf", "doc", "docx",
            "xls", "xlsx",
            "ppt", "pptx",
            "png", "jpg", "jpeg", "webp"
    );

    private static final String EST_PENDIENTE  = "PENDIENTE";
    private static final String EST_EN_PROCESO = "EN PROCESO";
    private static final String EST_COMPLETADA = "COMPLETADA";

    private static final String AMB_LISTA      = "LISTA";
    private static final String AMB_ESPERA     = "ESPERA";
    private static final String AMB_CALENDARIO = "CALENDARIO";
    private static final String AMB_COMPLETADA = "COMPLETADA";

    private static final String ORIG_ASIGNADA  = "ASIGNADA";
    private static final String ORIG_PERSONAL  = "PERSONAL";

    // ✅ modos de actividad
    private static final String ACT_MODO_EXISTENTE = "EXISTENTE";
    private static final String ACT_MODO_NUEVA      = "NUEVA";

    public TareaService(
            ITareaRepository tareaRepo,
            IUsuarioRepository usuarioRepo,
            IActividadRepository actividadRepo
    ) {
        this.tareaRepo = tareaRepo;
        this.usuarioRepo = usuarioRepo;
        this.actividadRepo = actividadRepo;
    }

    // =========================
    // Utils generales
    // =========================
    private static String nvl(String v, String def) {
        return (v == null || v.trim().isEmpty()) ? def : v.trim();
    }

    private static boolean hasText(String v) {
        return v != null && !v.trim().isEmpty();
    }

    private static String canonEstado(String raw) {
        if (raw == null) return null;
        String e = raw.trim().toUpperCase(Locale.ROOT);
        if (e.isEmpty()) return null;

        e = e.replace("_", " ").replaceAll("\\s+", " ").trim();

        if (EST_PENDIENTE.equals(e)) return EST_PENDIENTE;
        if ("EN PROCESO".equals(e)) return EST_EN_PROCESO;
        if (EST_COMPLETADA.equals(e)) return EST_COMPLETADA;

        return e;
    }

    private static String canonAmbito(String raw) {
        if (raw == null) return null;
        String a = raw.trim().toUpperCase(Locale.ROOT);
        a = a.replace("_", " ").replaceAll("\\s+", " ").trim();
        return a.isEmpty() ? null : a;
    }

    private static String canonPrioridad(String raw) {
        if (raw == null) return null;
        String p = raw.trim().toUpperCase(Locale.ROOT);
        p = p.replace("_", " ").replaceAll("\\s+", " ").trim();
        return p.isEmpty() ? null : p;
    }

    private static String canonModoActividad(String raw) {
        if (raw == null) return ACT_MODO_EXISTENTE;
        String v = raw.trim().toUpperCase(Locale.ROOT);
        if (v.isEmpty()) return ACT_MODO_EXISTENTE;
        if (ACT_MODO_NUEVA.equals(v)) return ACT_MODO_NUEVA;
        return ACT_MODO_EXISTENTE;
    }

    private static void validarAmbitoPermitido(String amb) {
        if (amb == null) throw new RuntimeException("Ámbito inválido");
        if (!AMB_LISTA.equals(amb) && !AMB_ESPERA.equals(amb) && !AMB_CALENDARIO.equals(amb) && !AMB_COMPLETADA.equals(amb)) {
            throw new RuntimeException("Ámbito inválido: " + amb);
        }
    }

    private LocalDateTime parseDateTimeLocal(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isEmpty()) return null;

        try {
            return LocalDateTime.parse(v);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(v.replace(" ", "T"));
            } catch (Exception ex) {
                throw new RuntimeException("Formato fecha inválido: " + s);
            }
        }
    }

    // =========================
    // ✅ Fechas consistentes (fechaAsignacion + fechaDia)
    // =========================
    private static void ensureFechaAsignacionYDia(Tarea t) {
        if (t == null) return;

        if (t.getFechaAsignacion() == null) {
            t.setFechaAsignacion(LocalDateTime.now());
        }

        // ✅ clave para reportes por mes/año con DATE
        if (t.getFechaDia() == null && t.getFechaAsignacion() != null) {
            t.setFechaDia(t.getFechaAsignacion().toLocalDate());
        }
        if (t.getFechaDia() == null) {
            t.setFechaDia(LocalDate.now());
        }
    }

    // =========================
    // Utils archivos
    // =========================
    private Path resolveUploadsBaseDir() {
        return Paths.get(fileBaseDir).toAbsolutePath().normalize();
    }

    private static void ensureDirExists(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear directorio: " + dir + " | " + e.getMessage());
        }
    }

    private static String sanitizeFilename(String original) {
        String n = (original == null || original.trim().isEmpty()) ? "archivo" : original.trim();
        n = n.replace("\\", "/");
        n = n.substring(n.lastIndexOf('/') + 1);
        n = n.replaceAll("[^a-zA-Z0-9._-]", "_");
        while (n.contains("..")) n = n.replace("..", ".");
        if (n.length() > 140) n = n.substring(0, 140);
        if (n.isBlank()) n = "archivo";
        return n;
    }

    private static String getExtensionLower(String fileName) {
        if (fileName == null) return "";
        int i = fileName.lastIndexOf('.');
        if (i < 0 || i == fileName.length() - 1) return "";
        return fileName.substring(i + 1).toLowerCase(Locale.ROOT);
    }

    // =========================
    // ✅ ACTIVIDAD helpers
    // =========================

    /** Crea o reactiva actividad por nombre dentro del proyecto. */
    private Actividad ensureActividadParaProyecto(Integer idProyecto, String nombre, String tipoActividad, String fase) {
        if (idProyecto == null) return null;

        String nom = (nombre == null) ? "" : nombre.trim();
        if (nom.isEmpty()) return null;

        Optional<Actividad> opt = actividadRepo.findByProyecto_IdTareaAndNombreIgnoreCase(idProyecto, nom);
        if (opt.isPresent()) {
            Actividad a = opt.get();
            if (!Boolean.TRUE.equals(a.getActivo())) a.setActivo(true);

            if (hasText(tipoActividad)) a.setTipoActividad(tipoActividad.trim());
            if (hasText(fase)) a.setFase(fase.trim());

            return actividadRepo.save(a);
        }

        Tarea proyecto = tareaRepo.findById(idProyecto)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        if (!Boolean.TRUE.equals(proyecto.getEsProyecto())) {
            throw new RuntimeException("El idProyecto no corresponde a un proyecto contenedor.");
        }

        Actividad a = new Actividad();
        a.setProyecto(proyecto);
        a.setNombre(nom);
        a.setTipoActividad(hasText(tipoActividad) ? tipoActividad.trim() : null);
        a.setFase(hasText(fase) ? fase.trim() : null);
        a.setActivo(true);

        return actividadRepo.save(a);
    }

    // ✅ EXISTENTE nunca exige nombre, y NUEVA sí exige nombre.
    private Actividad resolveActividadPorModo(Integer idProyecto,
                                              String actividadModoRaw,
                                              Integer idActividad,
                                              String actividadNombre,
                                              String tipoActividad,
                                              String fase) {

        if (idProyecto == null) return null;

        String modo = canonModoActividad(actividadModoRaw);

        // ✅ NUEVA: exige nombre
        if (ACT_MODO_NUEVA.equals(modo)) {
            if (!hasText(actividadNombre)) {
                throw new RuntimeException("Debe escribir el nombre de la nueva actividad.");
            }
            return ensureActividadParaProyecto(idProyecto, actividadNombre.trim(), tipoActividad, fase);
        }

        // ✅ EXISTENTE: exige idActividad (IGNORA actividadNombre)
        if (idActividad == null) {
            throw new RuntimeException("Debe seleccionar una actividad existente.");
        }

        Actividad a = actividadRepo.findById(idActividad)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada."));

        Integer pid = (a.getProyecto() != null) ? a.getProyecto().getIdTarea() : null;
        if (pid == null || !pid.equals(idProyecto)) {
            throw new RuntimeException("La actividad seleccionada no pertenece al proyecto.");
        }

        boolean changed = false;

        if (!Boolean.TRUE.equals(a.getActivo())) {
            a.setActivo(true);
            changed = true;
        }

        if (hasText(tipoActividad)) {
            String v = tipoActividad.trim();
            if (!Objects.equals(a.getTipoActividad(), v)) {
                a.setTipoActividad(v);
                changed = true;
            }
        }

        if (hasText(fase)) {
            String v = fase.trim();
            if (!Objects.equals(a.getFase(), v)) {
                a.setFase(v);
                changed = true;
            }
        }

        return changed ? actividadRepo.save(a) : a;
    }

    private static void setActividadEnTarea(Tarea t, Actividad a) {
        t.setActividad(a);
    }

    private static void syncStringsDesdeActividad(Tarea t, Actividad a) {
        if (t == null || a == null) return;
        t.setSubActividad(a.getNombre());
        t.setTipoActividad(a.getTipoActividad());
        t.setFase(a.getFase());
    }

    // =========================
    // ✅ PROGRESO DE PROYECTO
    // =========================
    public int calcularProgresoProyecto(Integer idProyectoPadre) {
        if (idProyectoPadre == null) return 0;

        long total = tareaRepo.countByProyectoPadre_IdTareaAndActivoTrueAndEsProyectoFalse(idProyectoPadre);
        if (total <= 0) return 0;

        long done = tareaRepo.countByProyectoPadre_IdTareaAndActivoTrueAndEsProyectoFalseAndEstado(
                idProyectoPadre, EST_COMPLETADA
        );

        int out = (int) Math.round((done * 100.0) / total);
        if (out < 0) out = 0;
        if (out > 100) out = 100;
        return out;
    }

    @Transactional
    public void refrescarEstadoProyectoPadre(Integer idProyectoPadre) {
        if (idProyectoPadre == null) return;

        Tarea padre = tareaRepo.findById(idProyectoPadre).orElse(null);
        if (padre == null) return;

        if (!Boolean.TRUE.equals(padre.getActivo())) return;
        if (!Boolean.TRUE.equals(padre.getEsProyecto())) return;

        int pct = calcularProgresoProyecto(idProyectoPadre);

        String nuevoEstado = (pct >= 100) ? EST_COMPLETADA : EST_PENDIENTE;

        String estadoActual = canonEstado(padre.getEstado());
        if (!nuevoEstado.equals(estadoActual)) {
            padre.setEstado(nuevoEstado);

            if (EST_COMPLETADA.equals(nuevoEstado)) {
                padre.setFechaFinalizacion(LocalDateTime.now());
                padre.setAmbito(AMB_COMPLETADA);
            } else {
                padre.setFechaFinalizacion(null);
                if (padre.getAmbito() == null || AMB_COMPLETADA.equalsIgnoreCase(padre.getAmbito())) {
                    padre.setAmbito(AMB_LISTA);
                }
            }
            tareaRepo.save(padre);
        }
    }

    // =========================
    // ✅ CRUD / ACCIONES
    // =========================

    @Transactional
    public void actualizarEstado(Integer idTarea, String estadoRaw) {
        if (idTarea == null) throw new RuntimeException("idTarea requerido");

        Tarea t = tareaRepo.findById(idTarea)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        if (!Boolean.TRUE.equals(t.getActivo())) throw new RuntimeException("Tarea inactiva");

        String estado = canonEstado(estadoRaw);
        if (!hasText(estado)) throw new RuntimeException("Estado inválido");

        t.setEstado(estado);

        if (EST_COMPLETADA.equals(estado)) {
            t.setFechaFinalizacion(LocalDateTime.now());
            t.setAmbito(AMB_COMPLETADA);
        } else {
            t.setFechaFinalizacion(null);
            String amb = canonAmbito(t.getAmbito());
            if (amb == null || AMB_COMPLETADA.equals(amb)) t.setAmbito(AMB_LISTA);
        }

        tareaRepo.save(t);

        if (!Boolean.TRUE.equals(t.getEsProyecto())
                && t.getProyectoPadre() != null
                && t.getProyectoPadre().getIdTarea() != null) {
            refrescarEstadoProyectoPadre(t.getProyectoPadre().getIdTarea());
        }
    }

    @Transactional
    public void moverAmbito(Integer idTarea, String nuevoAmbitoRaw) {
        if (idTarea == null) throw new RuntimeException("idTarea requerido");

        Tarea t = tareaRepo.findById(idTarea)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        if (!Boolean.TRUE.equals(t.getActivo())) throw new RuntimeException("Tarea inactiva");
        if (Boolean.TRUE.equals(t.getEsProyecto())) throw new RuntimeException("No se mueve el ámbito de un proyecto contenedor.");

        String amb = canonAmbito(nuevoAmbitoRaw);
        validarAmbitoPermitido(amb);

        String est = canonEstado(t.getEstado());
        if (EST_COMPLETADA.equals(est)) {
            t.setAmbito(AMB_COMPLETADA);
        } else {
            t.setAmbito(amb);
        }

        tareaRepo.save(t);
    }

    @Transactional
    public void actualizarObservaciones(Integer idTarea, String observaciones) {
        if (idTarea == null) throw new RuntimeException("idTarea requerido");

        Tarea t = tareaRepo.findById(idTarea)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        if (!Boolean.TRUE.equals(t.getActivo())) throw new RuntimeException("Tarea inactiva");

        t.setObservaciones(observaciones != null ? observaciones : "");
        tareaRepo.save(t);
    }

    /**
     * Actualiza proyecto (contenedor). Si propagarHijas=true, aplica prioridad/color/fechas/fase/tipo/subActividad a hijas.
     */
    @Transactional
    public void actualizarProyecto(Integer idProyecto,
                                   String titulo,
                                   String descripcion,
                                   String fase,
                                   String subActividad,
                                   String tipoActividad,
                                   String prioridad,
                                   String color,
                                   String fechaInicio,
                                   String fechaLimite,
                                   Boolean propagarHijas,
                                   Integer idSolicitante) {

        if (idProyecto == null) throw new RuntimeException("idProyecto requerido");

        Tarea p = tareaRepo.findById(idProyecto)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        if (!Boolean.TRUE.equals(p.getActivo())) throw new RuntimeException("Proyecto inactivo");
        if (!Boolean.TRUE.equals(p.getEsProyecto())) throw new RuntimeException("El id no corresponde a un proyecto contenedor.");

        if (hasText(titulo)) p.setTitulo(titulo.trim());
        if (descripcion != null) p.setDescripcion(descripcion);

        if (hasText(fase)) p.setFase(fase.trim());
        if (hasText(subActividad)) p.setSubActividad(subActividad.trim());
        if (hasText(tipoActividad)) p.setTipoActividad(tipoActividad.trim());

        String pr = canonPrioridad(prioridad);
        if (hasText(pr)) p.setPrioridad(pr);

        if (hasText(color)) p.setColor(color.trim());

        LocalDateTime fi = parseDateTimeLocal(fechaInicio);
        LocalDateTime fl = parseDateTimeLocal(fechaLimite);

        if (fi != null) p.setFechaInicio(fi);
        if (fl != null) p.setFechaLimite(fl);

        tareaRepo.save(p);

        if (Boolean.TRUE.equals(propagarHijas)) {
            List<Tarea> hijas = tareaRepo.findByProyectoPadre_IdTareaAndEsProyectoFalseAndActivoTrue(idProyecto);

            for (Tarea h : hijas) {
                if (h == null || !Boolean.TRUE.equals(h.getActivo())) continue;

                if (hasText(pr)) h.setPrioridad(pr);
                if (hasText(color)) h.setColor(color.trim());

                if (fi != null) h.setFechaInicio(fi);
                if (fl != null) h.setFechaLimite(fl);

                if (hasText(fase)) h.setFase(fase.trim());
                if (hasText(subActividad)) h.setSubActividad(subActividad.trim());
                if (hasText(tipoActividad)) h.setTipoActividad(tipoActividad.trim());

                tareaRepo.save(h);
            }

            refrescarEstadoProyectoPadre(idProyecto);
        }
    }

    @Transactional
    public void moverTareaAProyecto(Integer idTarea, Integer idProyectoNuevo, Integer idSolicitante) {
        if (idTarea == null) throw new RuntimeException("idTarea requerido");

        Tarea t = tareaRepo.findById(idTarea)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        if (!Boolean.TRUE.equals(t.getActivo())) throw new RuntimeException("Tarea inactiva");
        if (Boolean.TRUE.equals(t.getEsProyecto())) throw new RuntimeException("No se puede mover un proyecto contenedor.");

        Integer viejoPadreId = (t.getProyectoPadre() != null ? t.getProyectoPadre().getIdTarea() : null);

        if (idProyectoNuevo == null) {
            t.setProyectoPadre(null);
            t.setActividad(null);
            tareaRepo.save(t);

            if (viejoPadreId != null) refrescarEstadoProyectoPadre(viejoPadreId);
            return;
        }

        Tarea nuevoPadre = tareaRepo.findById(idProyectoNuevo)
                .orElseThrow(() -> new RuntimeException("Proyecto destino no encontrado"));

        if (!Boolean.TRUE.equals(nuevoPadre.getActivo())) throw new RuntimeException("Proyecto destino inactivo");
        if (!Boolean.TRUE.equals(nuevoPadre.getEsProyecto())) throw new RuntimeException("El destino no es un proyecto contenedor");

        t.setProyectoPadre(nuevoPadre);
        t.setActividad(null);

        if (t.getFechaInicio() == null) t.setFechaInicio(nuevoPadre.getFechaInicio());
        if (t.getFechaLimite() == null) t.setFechaLimite(nuevoPadre.getFechaLimite());

        tareaRepo.save(t);

        if (viejoPadreId != null) refrescarEstadoProyectoPadre(viejoPadreId);
        refrescarEstadoProyectoPadre(idProyectoNuevo);
    }

    @Transactional
    public void eliminarTarea(Integer idTarea) {
        if (idTarea == null) throw new RuntimeException("idTarea requerido");

        Tarea t = tareaRepo.findById(idTarea)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        if (!Boolean.TRUE.equals(t.getActivo())) return;

        Integer padreId = (t.getProyectoPadre() != null ? t.getProyectoPadre().getIdTarea() : null);

        if (Boolean.TRUE.equals(t.getEsProyecto())) {
            List<Tarea> hijas = tareaRepo.findByProyectoPadre_IdTareaAndActivoTrue(idTarea);
            for (Tarea h : hijas) {
                if (h == null) continue;
                h.setActivo(false);
                tareaRepo.save(h);
            }
        }

        t.setActivo(false);
        tareaRepo.save(t);

        if (padreId != null) refrescarEstadoProyectoPadre(padreId);
    }

    // =========================
    // CONSULTAS
    // =========================
    public List<Tarea> obtenerTodasLasTareas() { return tareaRepo.findByActivoTrue(); }

    public List<Tarea> obtenerMisTareas(Integer idUsuario) {
        if (idUsuario == null) return new ArrayList<>();
        return tareaRepo.findByResponsable_IdUsuarioAndActivoTrue(idUsuario);
    }

    public List<Tarea> obtenerTareasActivasPorAmbito(Integer idUsuario, String ambito) {
        if (idUsuario == null) return new ArrayList<>();
        String amb = canonAmbito(ambito);
        return tareaRepo.findByResponsable_IdUsuarioAndAmbitoAndActivoTrueAndEstadoNotAndEsProyectoFalse(
                idUsuario, amb, EST_COMPLETADA
        );
    }

    public List<Tarea> obtenerTareasCompletadas(Integer idUsuario) {
        if (idUsuario == null) return new ArrayList<>();
        return tareaRepo.findByResponsable_IdUsuarioAndEstadoAndActivoTrue(idUsuario, EST_COMPLETADA);
    }

    public List<Tarea> obtenerMisProyectos(Integer idUsuario) {
        if (idUsuario == null) return new ArrayList<>();
        return tareaRepo.findByResponsable_IdUsuarioAndEsProyectoTrueAndActivoTrue(idUsuario);
    }

    public List<Tarea> listarProyectosExistentesParaUsuario(Integer idUsuario) {
        return obtenerMisProyectos(idUsuario);
    }

    public Tarea obtenerTareaPorId(Integer idTarea) {
        if (idTarea == null) return null;
        return tareaRepo.findById(idTarea).orElse(null);
    }

    public List<Actividad> listarActividadesActivasPorProyecto(Integer idProyecto) {
        if (idProyecto == null) return Collections.emptyList();
        return actividadRepo.findByProyecto_IdTareaAndActivoTrueOrderByNombreAsc(idProyecto);
    }

    // =========================
    // ✅ PROYECTOS / ASIGNACIÓN
    // =========================

    @Transactional
    public Integer crearProyectoYAsignarTareas(List<Integer> ids,
                                               Tarea proyectoBase,
                                               String actividadNombre,
                                               HttpServletRequest request,
                                               Integer adminId) {

        if (proyectoBase == null) {
            throw new RuntimeException("proyectoBase requerido");
        }

        Usuario admin = usuarioRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

        Tarea proyecto = new Tarea();
        proyecto.setTitulo(nvl(proyectoBase.getTitulo(), "Proyecto"));
        proyecto.setDescripcion(proyectoBase.getDescripcion());
        proyecto.setObservaciones(proyectoBase.getObservaciones());

        proyecto.setFase(hasText(proyectoBase.getFase()) ? proyectoBase.getFase().trim() : null);
        proyecto.setEsProyecto(true);
        proyecto.setAmbito(AMB_LISTA);

        proyecto.setCreador(admin);
        proyecto.setResponsable(admin);

        proyecto.setFechaAsignacion(LocalDateTime.now());
        ensureFechaAsignacionYDia(proyecto);

        proyecto.setFechaInicio(proyectoBase.getFechaInicio());
        proyecto.setFechaLimite(proyectoBase.getFechaLimite());

        proyecto.setPrioridad(nvl(canonPrioridad(proyectoBase.getPrioridad()), "MEDIA"));
        proyecto.setColor(nvl(proyectoBase.getColor(), "#0d52f2"));
        proyecto.setEstado(EST_PENDIENTE);

        proyecto.setTipoOrigen(ORIG_ASIGNADA);
        String grupoId = UUID.randomUUID().toString();
        proyecto.setIdGrupoMasivo(grupoId);
        proyecto.setActivo(true);

        proyecto = tareaRepo.save(proyecto);

        Actividad actividad = null;
        if (hasText(actividadNombre)) {
            actividad = ensureActividadParaProyecto(
                    proyecto.getIdTarea(),
                    actividadNombre.trim(),
                    proyectoBase.getTipoActividad(),
                    proyectoBase.getFase()
            );
        }

        // ✅ guardar resumen de actividad en el PROYECTO
        if (actividad != null) {
            proyecto.setSubActividad(actividad.getNombre());
            proyecto.setTipoActividad(actividad.getTipoActividad());
            proyecto.setFase(actividad.getFase());
            tareaRepo.save(proyecto);
        }

        if (ids == null) ids = new ArrayList<>();
        if (admin.getIdUsuario() != null && !ids.contains(admin.getIdUsuario())) ids.add(admin.getIdUsuario());

        int totalCreadas = 0;

        for (Integer idRes : ids) {
            if (idRes == null) continue;

            Usuario responsable = usuarioRepo.findById(idRes).orElse(null);
            if (responsable == null) continue;

            List<String> titulos = readTitulosEspecificos(request, idRes);
            if (titulos.isEmpty()) continue;

            for (String titulo : titulos) {
                Tarea sub = crearTareaObjeto(titulo, proyectoBase, responsable, admin, grupoId);
                sub.setProyectoPadre(proyecto);

                if (actividad != null) {
                    setActividadEnTarea(sub, actividad);
                    syncStringsDesdeActividad(sub, actividad);
                }

                if (sub.getFechaInicio() == null) sub.setFechaInicio(proyecto.getFechaInicio());
                if (sub.getFechaLimite() == null) sub.setFechaLimite(proyecto.getFechaLimite());

                ensureFechaAsignacionYDia(sub);

                tareaRepo.save(sub);
                totalCreadas++;
            }
        }

        if (totalCreadas > 0) refrescarEstadoProyectoPadre(proyecto.getIdTarea());
        return proyecto.getIdTarea();
    }

    @Transactional
    public Integer crearProyectoYAsignarTareas(List<Integer> ids,
                                              Tarea proyectoBase,
                                              HttpServletRequest request,
                                              Integer adminId) {
        return crearProyectoYAsignarTareas(ids, proyectoBase, null, request, adminId);
    }

    @Transactional
    public void asignarTareasAProyectoExistente(List<Integer> ids,
                                                Integer idProyectoPadre,
                                                Integer idActividad,
                                                String actividadModo,
                                                String actividadNombre,
                                                Tarea base,
                                                HttpServletRequest request,
                                                Integer adminId) {

        if (idProyectoPadre == null) throw new RuntimeException("Debe seleccionar un proyecto.");

        Usuario admin = usuarioRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

        Tarea contenedor = tareaRepo.findById(idProyectoPadre)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado."));

        if (!Boolean.TRUE.equals(contenedor.getActivo()))
            throw new RuntimeException("Proyecto inactivo.");

        if (!Boolean.TRUE.equals(contenedor.getEsProyecto()))
            throw new RuntimeException("El destino no es un proyecto contenedor.");

        Integer creadorId = (contenedor.getCreador() != null) ? contenedor.getCreador().getIdUsuario() : null;
        Integer respId = (contenedor.getResponsable() != null) ? contenedor.getResponsable().getIdUsuario() : null;

        if (admin.getIdUsuario() == null || !(admin.getIdUsuario().equals(creadorId) || admin.getIdUsuario().equals(respId))) {
            throw new RuntimeException("Sin permiso para asignar tareas en este proyecto.");
        }

        if (ids == null || ids.isEmpty()) return;

        String grupoId = (hasText(contenedor.getIdGrupoMasivo()))
                ? contenedor.getIdGrupoMasivo()
                : UUID.randomUUID().toString();

        if (!hasText(contenedor.getIdGrupoMasivo())) {
            contenedor.setIdGrupoMasivo(grupoId);
            tareaRepo.save(contenedor);
        }

        Actividad actividad = resolveActividadPorModo(
                contenedor.getIdTarea(),
                actividadModo,
                idActividad,
                (actividadNombre != null ? actividadNombre.trim() : null),
                base != null ? base.getTipoActividad() : null,
                base != null ? base.getFase() : null
        );

        // ✅ mantener resumen en el contenedor
        if (actividad != null) {
            contenedor.setSubActividad(actividad.getNombre());
            contenedor.setTipoActividad(actividad.getTipoActividad());
            contenedor.setFase(actividad.getFase());
            tareaRepo.save(contenedor);
        }

        int totalCreadas = 0;

        for (Integer idRes : ids) {
            if (idRes == null) continue;

            Usuario responsable = usuarioRepo.findById(idRes).orElse(null);
            if (responsable == null) continue;

            List<String> titulos = readTitulosEspecificos(request, idRes);
            if (titulos.isEmpty()) continue;

            for (String titulo : titulos) {
                Tarea sub = crearTareaObjeto(titulo, base, responsable, admin, grupoId);
                sub.setProyectoPadre(contenedor);

                setActividadEnTarea(sub, actividad);
                syncStringsDesdeActividad(sub, actividad);

                if (sub.getFechaInicio() == null) sub.setFechaInicio(contenedor.getFechaInicio());
                if (sub.getFechaLimite() == null) sub.setFechaLimite(contenedor.getFechaLimite());

                ensureFechaAsignacionYDia(sub);

                tareaRepo.save(sub);
                totalCreadas++;
            }
        }

        if (totalCreadas > 0) refrescarEstadoProyectoPadre(contenedor.getIdTarea());
    }

    @Transactional
    public void asignarTareasAProyectoExistente(List<Integer> ids,
                                                Integer idProyectoPadre,
                                                Integer idActividad,
                                                Tarea base,
                                                HttpServletRequest request,
                                                Integer adminId) {
        String modo = ACT_MODO_EXISTENTE;
        asignarTareasAProyectoExistente(ids, idProyectoPadre, idActividad, modo, null, base, request, adminId);
    }

    // =========================
    // Individuales / personal
    // =========================

    @Transactional
    public int asignarTareasIndividuales(List<Integer> ids, Tarea base, HttpServletRequest request, Integer adminId) {
        if (ids == null || ids.isEmpty()) return 0;

        Usuario admin = usuarioRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

        String grupoId = UUID.randomUUID().toString();
        int creadas = 0;

        for (Integer idRes : ids) {
            if (idRes == null) continue;

            Usuario responsable = usuarioRepo.findById(idRes).orElse(null);
            if (responsable == null) continue;

            List<String> titulos = readTitulosEspecificos(request, idRes);
            if (titulos.isEmpty()) continue;

            for (String titulo : titulos) {
                Tarea t = crearTareaObjeto(titulo, base, responsable, admin, grupoId);
                t.setProyectoPadre(null);
                t.setActividad(null);

                ensureFechaAsignacionYDia(t);

                tareaRepo.save(t);
                creadas++;
            }
        }
        return creadas;
    }

    @Transactional
    public void crearTareaPersonal(Tarea tarea, Integer idUsuario) {
        Usuario u = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        tarea.setCreador(u);
        tarea.setResponsable(u);

        tarea.setFechaAsignacion(LocalDateTime.now());
        ensureFechaAsignacionYDia(tarea);

        tarea.setTipoOrigen(ORIG_PERSONAL);

        tarea.setEsPrivada(true);
        tarea.setEsProyecto(false);

        tarea.setEstado(canonEstado(nvl(tarea.getEstado(), EST_PENDIENTE)));
        tarea.setPrioridad(nvl(canonPrioridad(tarea.getPrioridad()), "MEDIA"));
        tarea.setAmbito(nvl(canonAmbito(tarea.getAmbito()), AMB_LISTA));
        tarea.setColor(nvl(tarea.getColor(), "#0d52f2"));

        tarea.setActividad(null);

        tarea.setActivo(true);
        tareaRepo.save(tarea);
    }

    // =========================
    // PRIVADOS
    // =========================
    private Tarea crearTareaObjeto(String titulo, Tarea base, Usuario res, Usuario admin, String grupo) {
        Tarea t = new Tarea();
        t.setTitulo(titulo);

        t.setDescripcion(base != null ? base.getDescripcion() : null);
        t.setObservaciones(base != null ? base.getObservaciones() : null);

        t.setFase(base != null && hasText(base.getFase()) ? base.getFase().trim() : null);

        t.setPrioridad(nvl(canonPrioridad(base != null ? base.getPrioridad() : null), "MEDIA"));
        t.setColor(nvl(base != null ? base.getColor() : null, "#0d52f2"));

        t.setFechaInicio(base != null ? base.getFechaInicio() : null);
        t.setFechaLimite(base != null ? base.getFechaLimite() : null);

        t.setCreador(admin);
        t.setResponsable(res);

        t.setFechaAsignacion(LocalDateTime.now());
        ensureFechaAsignacionYDia(t);

        t.setEstado(EST_PENDIENTE);
        t.setAmbito(AMB_LISTA);

        t.setTipoOrigen(ORIG_ASIGNADA);
        t.setIdGrupoMasivo(grupo);

        t.setEsProyecto(false);
        t.setEsPrivada(false);
        t.setActivo(true);

        return t;
    }

    private List<String> readTitulosEspecificos(HttpServletRequest request, Integer idRes) {
        String key = "tareaEspecifica_" + idRes;
        String[] values = request.getParameterValues(key);

        if (values == null || values.length == 0) return Collections.emptyList();

        List<String> out = new ArrayList<>();

        if (values.length > 1) {
            for (String v : values) addIfValid(out, v);
            return out;
        }

        String one = values[0];
        if (one == null) return Collections.emptyList();

        if (one.contains("\n") || one.contains("\r")) {
            String[] lines = one.split("\\r?\\n");
            for (String line : lines) addIfValid(out, line);
            return out;
        }

        addIfValid(out, one);
        return out;
    }

    private void addIfValid(List<String> out, String v) {
        if (v == null) return;
        String t = v.trim();
        if (!t.isEmpty()) out.add(t);
    }

    // =========================
    // ENTREGABLE
    // =========================
    @Transactional
    public void actualizarObservacionesConEntregable(Integer idTarea,
                                                     String observaciones,
                                                     MultipartFile archivo,
                                                     String fechaInicioStr,
                                                     String fechaLimiteStr) {

        Tarea tarea = tareaRepo.findById(idTarea)
                .orElseThrow(() -> new RuntimeException("No encontrada"));

        tarea.setObservaciones(observaciones != null ? observaciones : "");

        if (fechaInicioStr != null && !fechaInicioStr.isBlank())
            tarea.setFechaInicio(parseDateTimeLocal(fechaInicioStr));

        if (fechaLimiteStr != null && !fechaLimiteStr.isBlank())
            tarea.setFechaLimite(parseDateTimeLocal(fechaLimiteStr));

        if (archivo != null && !archivo.isEmpty()) {

            Path baseDir = resolveUploadsBaseDir();
            ensureDirExists(baseDir);

            String original = archivo.getOriginalFilename();
            String safe = sanitizeFilename(original);

            String ext = getExtensionLower(safe);
            if (ext.isEmpty()) throw new RuntimeException("Archivo sin extensión");
            if (!EXT_PERMITIDAS.contains(ext)) {
                throw new RuntimeException("Extensión no permitida: " + ext);
            }

            String folder = "tarea_" + idTarea;

            Path dir = baseDir.resolve(ENTREGABLES_SUBDIR).resolve(folder).toAbsolutePath().normalize();
            ensureDirExists(dir);

            String nombreFinal = UUID.randomUUID() + "_" + safe;
            Path destino = dir.resolve(nombreFinal).toAbsolutePath().normalize();

            if (!destino.startsWith(dir)) {
                throw new RuntimeException("Nombre de archivo inválido");
            }

            try {
                Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar archivo: " + e.getMessage());
            }

            tarea.setEntregableNombre(safe);

            String rutaRel = ENTREGABLES_SUBDIR + "/" + folder + "/" + nombreFinal;
            tarea.setEntregableRuta(rutaRel);
        }

        tareaRepo.save(tarea);
    }
    
 // =========================
 // ✅ TAREAS POR ACTIVIDAD (CLAVE PARA TU UI)
 // =========================

 /**
  * Devuelve tareas ejecutables (no proyectos) de una actividad.
  * Ideal: Proyecto -> Actividades -> Tareas
  */
 @Transactional(readOnly = true)
 public List<Tarea> listarTareasPorActividad(Integer idActividad) {
     if (idActividad == null) return Collections.emptyList();
     return tareaRepo.findByActividad_IdActividadAndActivoTrueAndEsProyectoFalse(idActividad);
 }

 /**
  * (Más seguro) Devuelve tareas de una actividad validando que pertenezca al proyecto.
  * Útil si tu endpoint recibe idProyecto + idActividad.
  */
 @Transactional(readOnly = true)
 public List<Tarea> listarTareasPorProyectoYActividad(Integer idProyecto, Integer idActividad) {
     if (idProyecto == null || idActividad == null) return Collections.emptyList();
     return tareaRepo.findByProyectoAndActividad(idProyecto, idActividad);
 }

    
}
