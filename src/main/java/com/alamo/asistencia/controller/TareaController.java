package com.alamo.asistencia.controller;

import com.alamo.asistencia.model.Actividad;
import com.alamo.asistencia.model.Tarea;
import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.repository.IActividadRepository;
import com.alamo.asistencia.repository.ITareaRepository;
import com.alamo.asistencia.repository.IUsuarioRepository;
import com.alamo.asistencia.service.TareaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/tareas")
public class TareaController {

    @Autowired private TareaService tareaService;
    @Autowired private IUsuarioRepository usuarioRepo;
    @Autowired private ITareaRepository tareaRepo;
    @Autowired private IActividadRepository actividadRepo;

    // =========================
    // Constantes (alineadas)
    // =========================
    private static final String EST_COMPLETADA  = "COMPLETADA";
    private static final String EST_PENDIENTE   = "PENDIENTE";
    private static final String EST_EN_PROCESO  = "EN PROCESO"; // si en tu BD guardas EN_PROCESO, cambia aquí y en normEstado()

    private static final String AMB_LISTA       = "LISTA";
    private static final String AMB_ESPERA      = "ESPERA";
    private static final String AMB_CALENDARIO  = "CALENDARIO";
    private static final String AMB_COMPLETADA  = "COMPLETADA";

    private static final String ACT_MODO_EXISTENTE = "EXISTENTE";
    private static final String ACT_MODO_NUEVA     = "NUEVA";

    // =========================
    // Utils
    // =========================
    private boolean esAdminOCoord(Usuario u) {
        if (u == null || u.getObjRol() == null) return false;
        int rol = u.getObjRol().getId_rol();
        return rol == 1 || rol == 3;
    }

    private Integer parseIntegerOrNull(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isEmpty()) return null;
        try { return Integer.valueOf(v); } catch (Exception e) { return null; }
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    /** Normaliza estados aceptando EN_PROCESO / EN PROCESO / etc. */
    private String normEstado(String s) {
        if (s == null) return null;
        String v = s.trim().toUpperCase(Locale.ROOT);
        if (v.isEmpty()) return null;

        v = v.replace("_", " ").replaceAll("\\s+", " ").trim();

        if ("EN PROCESO".equals(v)) return EST_EN_PROCESO;
        if ("PENDIENTE".equals(v)) return EST_PENDIENTE;
        if ("COMPLETADA".equals(v)) return EST_COMPLETADA;

        return v;
    }

    private String normAmbito(String s) {
        if (s == null) return null;
        String v = s.trim().toUpperCase(Locale.ROOT);
        if (v.isEmpty()) return null;
        v = v.replace("_", " ").replaceAll("\\s+", " ").trim();
        return v;
    }

    private String normModoActividad(String s) {
        if (s == null) return ACT_MODO_EXISTENTE;
        String v = s.trim().toUpperCase(Locale.ROOT);
        if (v.isEmpty()) return ACT_MODO_EXISTENTE;
        return ACT_MODO_NUEVA.equals(v) ? ACT_MODO_NUEVA : ACT_MODO_EXISTENTE;
    }

    private String normModoProyecto(String s) {
        if (s == null) return "NUEVO";
        String v = s.trim().toUpperCase(Locale.ROOT);
        if (v.isEmpty()) return "NUEVO";
        return "EXISTENTE".equals(v) ? "EXISTENTE" : "NUEVO";
    }

    private Map<String, Object> miniUsuario(Usuario x) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (x == null) return m;
        m.put("idUsuario", x.getIdUsuario());
        m.put("nombres", x.getNombres());
        m.put("apellidoPaterno", x.getApellido_paterno());
        m.put("apellidoMaterno", x.getApellido_materno());
        m.put("dni", x.getDni());
        m.put("correo", x.getCorreo());
        m.put("telefono", x.getTelefono());
        m.put("cargo", x.getCargo());
        return m;
    }

    private Map<String, Object> errMap(String msg) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("error", msg);
        return m;
    }

    private void validarAmbito(String amb) {
        if (amb == null || (!AMB_LISTA.equals(amb) && !AMB_ESPERA.equals(amb) && !AMB_CALENDARIO.equals(amb) && !AMB_COMPLETADA.equals(amb))) {
            throw new RuntimeException("Ámbito inválido: " + amb);
        }
    }

    private LocalDateTime parseLdtOrNull(String raw) {
        if (raw == null) return null;
        String v = raw.trim();
        if (v.isEmpty()) return null;

        try {
            return LocalDateTime.parse(v);
        } catch (DateTimeParseException ex) {
            try {
                return LocalDateTime.parse(v.replace(" ", "T"));
            } catch (Exception ex2) {
                throw new RuntimeException("Formato fecha inválido: " + raw);
            }
        }
    }

    /** ✅ Unifica la validación de permisos para tareas/proyectos */
    private boolean tienePermiso(Usuario u, Tarea t) {
        if (u == null || t == null) return false;
        if (esAdminOCoord(u)) return true;

        Integer uid = u.getIdUsuario();
        Integer idResp = (t.getResponsable() != null ? t.getResponsable().getIdUsuario() : null);
        Integer idCreador = (t.getCreador() != null ? t.getCreador().getIdUsuario() : null);

        return uid != null && (uid.equals(idResp) || uid.equals(idCreador));
    }

    // =========================
    // ✅ BADGE: CONTEO PENDIENTES PARA SIDEBAR
    // =========================
    private void cargarConteoPendientes(Usuario u, Model model) {
        if (u == null || u.getIdUsuario() == null) {
            model.addAttribute("conteoPendientes", 0);
            return;
        }
        long conteo = tareaRepo.countByResponsable_IdUsuarioAndActivoTrueAndEsProyectoFalseAndEstadoNot(
                u.getIdUsuario(), EST_COMPLETADA
        );
        model.addAttribute("conteoPendientes", conteo);
    }

    // =========================
    // VISTA ADMIN
    // =========================
    @GetMapping("/gestion")
    public String verGestionTareas(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/usuarios/cargarLogin";
        if (!esAdminOCoord(u)) return "redirect:/cargarmenu";

        model.addAttribute("u", u);
        cargarConteoPendientes(u, model); // ✅

        model.addAttribute("usuarios", Optional.ofNullable(usuarioRepo.findAll()).orElse(Collections.emptyList()));

        List<Tarea> historialGlobal = Optional.ofNullable(tareaService.obtenerTodasLasTareas())
                .orElse(Collections.emptyList());

        List<Map<String, Object>> mini = historialGlobal.stream()
                .filter(Objects::nonNull)
                .filter(t -> Boolean.TRUE.equals(t.getActivo()))
                .map(this::toMiniTareaAdmin)
                .collect(Collectors.toList());

        model.addAttribute("todasLasTareas", mini);
        return "asignartareas";
    }

    @GetMapping("/gestion-data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> gestionData(HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errMap("No autenticado"));
        if (!esAdminOCoord(u)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errMap("Sin permiso"));

        List<Usuario> usuarios = Optional.ofNullable(usuarioRepo.findAll()).orElse(Collections.emptyList());
        List<Tarea> historialGlobal = Optional.ofNullable(tareaService.obtenerTodasLasTareas()).orElse(Collections.emptyList());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("usuarios", usuarios.stream().map(this::miniUsuario).collect(Collectors.toList()));
        out.put("todasLasTareas", historialGlobal.stream()
                .filter(Objects::nonNull)
                .filter(t -> Boolean.TRUE.equals(t.getActivo()))
                .map(this::toMiniTareaAdmin)
                .collect(Collectors.toList()));
        return ResponseEntity.ok(out);
    }

    // =========================
    // WORKSPACE
    // =========================
    @GetMapping("/workspace")
    public String verWorkspace(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/usuarios/cargarLogin";

        Integer idUsuario = u.getIdUsuario();

        List<Tarea> recados = Optional.ofNullable(tareaService.obtenerTareasActivasPorAmbito(idUsuario, AMB_LISTA))
                .orElse(Collections.emptyList());
        List<Tarea> espera = Optional.ofNullable(tareaService.obtenerTareasActivasPorAmbito(idUsuario, AMB_ESPERA))
                .orElse(Collections.emptyList());
        List<Tarea> completadas = Optional.ofNullable(tareaService.obtenerTareasCompletadas(idUsuario))
                .orElse(Collections.emptyList());

        List<Tarea> todas = Optional.ofNullable(tareaService.obtenerMisTareas(idUsuario))
                .orElse(Collections.emptyList());

        List<Tarea> proyectos = extraerProyectosDesdeTareas(todas);

        recados = recados.stream().filter(Objects::nonNull).filter(t -> Boolean.TRUE.equals(t.getActivo())).collect(Collectors.toList());
        espera = espera.stream().filter(Objects::nonNull).filter(t -> Boolean.TRUE.equals(t.getActivo())).collect(Collectors.toList());
        proyectos = proyectos.stream().filter(Objects::nonNull).filter(p -> Boolean.TRUE.equals(p.getActivo())).collect(Collectors.toList());
        completadas = completadas.stream().filter(Objects::nonNull).collect(Collectors.toList());

        model.addAttribute("u", u);
        cargarConteoPendientes(u, model); // ✅

        model.addAttribute("usuarios", Optional.ofNullable(usuarioRepo.findAll()).orElse(Collections.emptyList()));
        model.addAttribute("recados", recados);
        model.addAttribute("espera", espera);
        model.addAttribute("proyectos", proyectos);
        model.addAttribute("completadas", completadas);

        return "mistareas";
    }

    @GetMapping("/workspace-data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> workspaceData(HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errMap("No autenticado"));

        Integer idUsuario = u.getIdUsuario();

        List<Tarea> recados = Optional.ofNullable(tareaService.obtenerTareasActivasPorAmbito(idUsuario, AMB_LISTA))
                .orElse(Collections.emptyList());
        List<Tarea> espera = Optional.ofNullable(tareaService.obtenerTareasActivasPorAmbito(idUsuario, AMB_ESPERA))
                .orElse(Collections.emptyList());
        List<Tarea> completadas = Optional.ofNullable(tareaService.obtenerTareasCompletadas(idUsuario))
                .orElse(Collections.emptyList());
        List<Tarea> todas = Optional.ofNullable(tareaService.obtenerMisTareas(idUsuario))
                .orElse(Collections.emptyList());

        List<Tarea> proyectosPropios = Optional.ofNullable(tareaService.obtenerMisProyectos(idUsuario))
                .orElse(Collections.emptyList());

        Map<Integer, Tarea> proyectosUniq = new LinkedHashMap<>();
        for (Tarea p : proyectosPropios) {
            if (p == null) continue;
            if (!Boolean.TRUE.equals(p.getActivo())) continue;
            proyectosUniq.putIfAbsent(p.getIdTarea(), p);
        }
        for (Tarea p : extraerProyectosDesdeTareas(todas)) {
            if (p == null) continue;
            if (!Boolean.TRUE.equals(p.getActivo())) continue;
            proyectosUniq.putIfAbsent(p.getIdTarea(), p);
        }
        List<Tarea> proyectosActivos = new ArrayList<>(proyectosUniq.values());

        Map<Integer, List<Map<String, Object>>> tareasPorProyecto = new LinkedHashMap<>();

        Map<Integer, List<Tarea>> hijasPorPadre = todas.stream()
                .filter(Objects::nonNull)
                .filter(t -> Boolean.TRUE.equals(t.getActivo()))
                .filter(t -> !Boolean.TRUE.equals(t.getEsProyecto()))
                .filter(t -> t.getProyectoPadre() != null && t.getProyectoPadre().getIdTarea() != null)
                .collect(Collectors.groupingBy(t -> t.getProyectoPadre().getIdTarea(), LinkedHashMap::new, Collectors.toList()));

        for (Tarea p : proyectosActivos) {
            Integer pid = p.getIdTarea();
            List<Tarea> hijas = hijasPorPadre.getOrDefault(pid, Collections.emptyList());
            List<Map<String, Object>> hijasMini = hijas.stream().map(this::toMiniTarea).collect(Collectors.toList());
            tareasPorProyecto.put(pid, hijasMini);
        }

        List<Map<String, Object>> independientes = todas.stream()
                .filter(Objects::nonNull)
                .filter(t -> Boolean.TRUE.equals(t.getActivo()))
                .filter(t -> !Boolean.TRUE.equals(t.getEsProyecto()))
                .filter(t -> t.getProyectoPadre() == null)
                .map(this::toMiniTarea)
                .collect(Collectors.toList());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("recados", recados.stream().filter(Objects::nonNull).map(this::toMiniTarea).collect(Collectors.toList()));
        out.put("espera", espera.stream().filter(Objects::nonNull).map(this::toMiniTarea).collect(Collectors.toList()));
        out.put("completadas", completadas.stream().filter(Objects::nonNull).map(this::toMiniTarea).collect(Collectors.toList()));
        out.put("proyectos", proyectosActivos.stream().map(this::toMiniProyecto).collect(Collectors.toList()));
        out.put("tareasPorProyecto", tareasPorProyecto);
        out.put("independientes", independientes);

        return ResponseEntity.ok(out);
    }

    // =========================
    // FULLCALENDAR
    // =========================
    @GetMapping(value = "/listar-json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> listarJsonCalendario(HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());

        Integer idUsuario = u.getIdUsuario();
        List<Tarea> todas = Optional.ofNullable(tareaService.obtenerMisTareas(idUsuario))
                .orElse(Collections.emptyList());

        List<Map<String, Object>> eventos = new ArrayList<>();

        for (Tarea t : todas) {
            if (t == null) continue;
            if (!Boolean.TRUE.equals(t.getActivo())) continue;
            if (Boolean.TRUE.equals(t.getEsProyecto())) continue;

            LocalDateTime fi = t.getFechaInicio();
            LocalDateTime fl = t.getFechaLimite();
            if (fi == null && fl == null) continue;

            String start = (fi != null) ? fi.toString() : fl.toString();
            String end = (fl != null) ? fl.toString() : null;

            Map<String, Object> ev = new LinkedHashMap<>();
            ev.put("id", String.valueOf(t.getIdTarea()));
            ev.put("title", (hasText(t.getTitulo()) ? t.getTitulo().trim() : "Tarea #" + t.getIdTarea()));
            ev.put("start", start);
            if (end != null) ev.put("end", end);
            if (hasText(t.getColor())) ev.put("color", t.getColor().trim());

            eventos.add(ev);
        }

        return ResponseEntity.ok(eventos);
    }

    // =========================
    // ✅ ENDPOINTS CLAVE PARA UI: ACTIVIDADES -> TAREAS
    // =========================
    @GetMapping("/tareas-por-actividad")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> tareasPorActividad(
            @RequestParam("idActividad") Integer idActividad,
            HttpSession session
    ) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());

        try {
            List<Tarea> tareas = tareaService.listarTareasPorActividad(idActividad);

            List<Map<String, Object>> out = tareas.stream()
                    .filter(Objects::nonNull)
                    .filter(t -> Boolean.TRUE.equals(t.getActivo()))
                    .filter(t -> tienePermiso(u, t))
                    .map(this::toMiniTarea)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(out);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/tareas-por-proyecto-actividad")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> tareasPorProyectoActividad(
            @RequestParam("idProyecto") Integer idProyecto,
            @RequestParam("idActividad") Integer idActividad,
            HttpSession session
    ) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());

        try {
            List<Tarea> tareas = tareaService.listarTareasPorProyectoYActividad(idProyecto, idActividad);

            List<Map<String, Object>> out = tareas.stream()
                    .filter(Objects::nonNull)
                    .filter(t -> Boolean.TRUE.equals(t.getActivo()))
                    .filter(t -> tienePermiso(u, t))
                    .map(this::toMiniTarea)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(out);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // =========================
    // Mini
    // =========================
    private Map<String, Object> toMiniProyecto(Tarea p) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (p == null) return m;

        m.put("idTarea", p.getIdTarea());
        m.put("titulo", p.getTitulo());
        m.put("descripcion", p.getDescripcion());
        m.put("fase", p.getFase());
        m.put("prioridad", p.getPrioridad());
        m.put("estado", p.getEstado());
        m.put("ambito", p.getAmbito());
        m.put("color", p.getColor());

        m.put("fechaAsignacion", p.getFechaAsignacion() != null ? p.getFechaAsignacion().toString() : null);
        m.put("fechaDia", p.getFechaDia() != null ? p.getFechaDia().toString() : null);

        m.put("fechaInicio", p.getFechaInicio() != null ? p.getFechaInicio().toString() : null);
        m.put("fechaLimite", p.getFechaLimite() != null ? p.getFechaLimite().toString() : null);

        m.put("idGrupoMasivo", p.getIdGrupoMasivo());
        m.put("esProyecto", p.getEsProyecto());
        m.put("entregableNombre", p.getEntregableNombre());
        m.put("entregableRuta", p.getEntregableRuta());

        try {
            m.put("progreso", tareaService.calcularProgresoProyecto(p.getIdTarea()));
        } catch (Exception ex) {
            m.put("progreso", 0);
        }

        m.put("subActividad", p.getSubActividad());
        m.put("tipoActividad", p.getTipoActividad());
        return m;
    }

    private Map<String, Object> toMiniTarea(Tarea t) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (t == null) return m;

        m.put("idTarea", t.getIdTarea());
        m.put("titulo", t.getTitulo());
        m.put("descripcion", t.getDescripcion());
        m.put("observaciones", t.getObservaciones());
        m.put("prioridad", t.getPrioridad());
        m.put("estado", t.getEstado());
        m.put("ambito", t.getAmbito());
        m.put("color", t.getColor());

        m.put("fechaAsignacion", t.getFechaAsignacion() != null ? t.getFechaAsignacion().toString() : null);
        m.put("fechaDia", t.getFechaDia() != null ? t.getFechaDia().toString() : null);

        m.put("fechaInicio", t.getFechaInicio() != null ? t.getFechaInicio().toString() : null);
        m.put("fechaLimite", t.getFechaLimite() != null ? t.getFechaLimite().toString() : null);

        m.put("proyectoPadreId", (t.getProyectoPadre() != null ? t.getProyectoPadre().getIdTarea() : null));
        m.put("idGrupoMasivo", t.getIdGrupoMasivo());
        m.put("esProyecto", t.getEsProyecto());
        m.put("entregableNombre", t.getEntregableNombre());
        m.put("entregableRuta", t.getEntregableRuta());

        if (t.getActividad() != null) {
            m.put("actividadId", t.getActividad().getIdActividad());
            m.put("actividadNombre", t.getActividad().getNombre());
        } else {
            m.put("actividadId", null);
            m.put("actividadNombre", null);
        }

        m.put("subActividad", t.getSubActividad());
        m.put("tipoActividad", t.getTipoActividad());
        m.put("fase", t.getFase());

        return m;
    }

    private Map<String, Object> toMiniTareaAdmin(Tarea t) {
        Map<String, Object> m = toMiniTarea(t);
        m.put("responsableId", t != null && t.getResponsable() != null ? t.getResponsable().getIdUsuario() : null);
        m.put("creadorId", t != null && t.getCreador() != null ? t.getCreador().getIdUsuario() : null);
        return m;
    }

    // =========================
    // ✅ ASIGNAR
    // =========================
    @PostMapping("/asignar")
    public String asignarMasivo(
            @RequestParam(value = "usuariosIds", required = false) List<Integer> usuariosIds,
            @RequestParam(value = "usuariosIdsProyecto", required = false) List<Integer> usuariosIdsProyecto,
            @RequestParam(value = "idProyectoPadre", required = false) Integer idProyectoPadre,

            @RequestParam(value = "modoProyecto", required = false) String modoProyecto,
            @RequestParam(value = "actividadModo", required = false) String actividadModo,
            @RequestParam(value = "idActividad", required = false) Integer idActividad,
            @RequestParam(value = "actividadNombre", required = false) String actividadNombre,

            @ModelAttribute Tarea tarea,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes ra
    ) {
        Usuario admin = (Usuario) session.getAttribute("usuarioLogueado");
        if (admin == null) return "redirect:/usuarios/cargarLogin";
        if (!esAdminOCoord(admin)) return "redirect:/cargarmenu";

        try {
            boolean esProyecto = Boolean.TRUE.equals(tarea.getEsProyecto());

            // =========================
            // PROYECTO
            // =========================
            if (esProyecto) {
                String mp = normModoProyecto(modoProyecto); // NUEVO | EXISTENTE

                if (usuariosIdsProyecto == null || usuariosIdsProyecto.isEmpty()) {
                    ra.addFlashAttribute("error", "Seleccione al menos un responsable para el proyecto.");
                    return "redirect:/tareas/gestion";
                }

                String am = normModoActividad(actividadModo);
                Integer actId = idActividad;
                String actNombre = (actividadNombre != null ? actividadNombre.trim() : "");

                if ("EXISTENTE".equals(mp)) {
                    if (idProyectoPadre == null) {
                        ra.addFlashAttribute("error", "Seleccione un proyecto existente.");
                        return "redirect:/tareas/gestion";
                    }

                    if (ACT_MODO_EXISTENTE.equals(am)) {
                        if (actId == null) {
                            ra.addFlashAttribute("error", "Seleccione una actividad existente del proyecto.");
                            return "redirect:/tareas/gestion";
                        }
                        actNombre = null;
                    } else {
                        if (!hasText(actNombre)) {
                            ra.addFlashAttribute("error", "Escriba el nombre de la nueva actividad.");
                            return "redirect:/tareas/gestion";
                        }
                        actId = null;
                    }

                    tareaService.asignarTareasAProyectoExistente(
                            usuariosIdsProyecto,
                            idProyectoPadre,
                            actId,
                            am,
                            actNombre,
                            tarea,
                            request,
                            admin.getIdUsuario()
                    );

                    ra.addFlashAttribute("success", "Tareas agregadas al proyecto correctamente.");
                    return "redirect:/tareas/gestion";
                }

                if (!hasText(tarea.getTitulo())) {
                    ra.addFlashAttribute("error", "El Título General es obligatorio al crear un proyecto nuevo.");
                    return "redirect:/tareas/gestion";
                }

                if (!hasText(actNombre) && hasText(tarea.getSubActividad())) {
                    actNombre = tarea.getSubActividad().trim();
                }
                if (!hasText(actNombre)) {
                    ra.addFlashAttribute("error", "La Actividad es obligatoria al crear un proyecto nuevo.");
                    return "redirect:/tareas/gestion";
                }

                Integer idProyectoCreado = tareaService.crearProyectoYAsignarTareas(
                        usuariosIdsProyecto,
                        tarea,
                        actNombre,
                        request,
                        admin.getIdUsuario()
                );

                ra.addFlashAttribute("success", "Proyecto creado correctamente (ID: " + idProyectoCreado + ").");
                return "redirect:/tareas/gestion";
            }

            // =========================
            // INDIVIDUAL
            // =========================
            if (usuariosIds == null || usuariosIds.isEmpty()) {
                ra.addFlashAttribute("error", "Seleccione al menos un responsable.");
                return "redirect:/tareas/gestion";
            }

            int creadas = tareaService.asignarTareasIndividuales(usuariosIds, tarea, request, admin.getIdUsuario());
            if (creadas > 0) ra.addFlashAttribute("success", "Tareas individuales asignadas correctamente. (" + creadas + ")");
            else ra.addFlashAttribute("error", "No se crearon tareas. Verifica tareas específicas por usuario.");

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/tareas/gestion";
    }

    // =========================
    // CREAR PERSONAL
    // =========================
    @PostMapping("/crear-personal")
    public String crearPersonal(@ModelAttribute Tarea tarea, HttpSession session, RedirectAttributes ra) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/usuarios/cargarLogin";

        try {
            tareaService.crearTareaPersonal(tarea, u.getIdUsuario());
            ra.addFlashAttribute("success", "Tarea personal agregada.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al crear: " + e.getMessage());
        }

        return "redirect:/tareas/workspace";
    }

    // =========================
    // ACTUALIZAR ESTADO
    // =========================
    @PostMapping("/actualizar-estado")
    @ResponseBody
    public ResponseEntity<String> actualizarEstado(@RequestParam("idTarea") Integer idTarea,
                                                   @RequestParam("estado") String estado,
                                                   HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");

        try {
            Tarea t = tareaRepo.findById(idTarea).orElse(null);
            if (t == null || !Boolean.TRUE.equals(t.getActivo()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No encontrada");

            if (!tienePermiso(u, t))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Sin permiso");

            String est = normEstado(estado);
            if (est == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Estado inválido");

            t.setEstado(est);

            if (EST_COMPLETADA.equals(est)) {
                t.setFechaFinalizacion(LocalDateTime.now());
                t.setAmbito(AMB_COMPLETADA);
            } else {
                t.setFechaFinalizacion(null);
                if (t.getAmbito() == null || AMB_COMPLETADA.equalsIgnoreCase(t.getAmbito())) {
                    t.setAmbito(AMB_LISTA);
                }
            }

            tareaRepo.save(t);

            if (t.getProyectoPadre() != null && t.getProyectoPadre().getIdTarea() != null) {
                tareaService.refrescarEstadoProyectoPadre(t.getProyectoPadre().getIdTarea());
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // =========================
    // MOVER ÁMBITO
    // =========================
    @PostMapping("/mover-ambito")
    @ResponseBody
    public ResponseEntity<String> moverAmbito(@RequestParam("idTarea") Integer idTarea,
                                              @RequestParam("nuevoAmbito") String nuevoAmbito,
                                              HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");

        try {
            Tarea t = tareaRepo.findById(idTarea).orElse(null);
            if (t == null || !Boolean.TRUE.equals(t.getActivo()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No encontrada");

            if (!tienePermiso(u, t))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Sin permiso");

            String amb = normAmbito(nuevoAmbito);
            validarAmbito(amb);

            String est = normEstado(t.getEstado());
            if (EST_COMPLETADA.equals(est)) t.setAmbito(AMB_COMPLETADA);
            else t.setAmbito(amb);

            tareaRepo.save(t);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // =========================
    // ACTUALIZAR NOTAS
    // =========================
    @PostMapping("/actualizar-notas")
    @ResponseBody
    public ResponseEntity<String> actualizarNotas(@RequestParam("idTarea") Integer idTarea,
                                                  @RequestParam(value = "observaciones", required = false) String observaciones,
                                                  HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");

        try {
            Tarea t = tareaRepo.findById(idTarea).orElse(null);
            if (t == null || !Boolean.TRUE.equals(t.getActivo()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No encontrada");

            if (!tienePermiso(u, t))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Sin permiso");

            t.setObservaciones(observaciones != null ? observaciones : "");
            tareaRepo.save(t);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // =========================
    // OBTENER JSON
    // =========================
    @GetMapping("/{id}/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerTareaJson(@PathVariable("id") Integer id,
                                                                HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errMap("No autenticado"));

        Tarea t = tareaService.obtenerTareaPorId(id);
        if (t == null || !Boolean.TRUE.equals(t.getActivo())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errMap("No encontrada"));
        }

        if (!tienePermiso(u, t)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errMap("Sin permiso"));
        }

        return ResponseEntity.ok(toMiniTareaAdmin(t));
    }

    // =========================
    // NOTAS + ARCHIVO + FECHAS (CORREGIDO: valida permiso)
    // =========================
    @PostMapping(value = "/actualizar-notas-archivo", consumes = {"multipart/form-data"})
    @ResponseBody
    public ResponseEntity<String> actualizarNotasArchivo(@RequestParam("idTarea") Integer idTarea,
                                                         @RequestParam(value = "observaciones", required = false) String observaciones,
                                                         @RequestParam(value = "fechaInicio", required = false) String fechaInicio,
                                                         @RequestParam(value = "fechaLimite", required = false) String fechaLimite,
                                                         @RequestParam(value = "archivo", required = false) MultipartFile archivo,
                                                         HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");

        try {
            Tarea t = tareaRepo.findById(idTarea).orElse(null);
            if (t == null || !Boolean.TRUE.equals(t.getActivo()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No encontrada");

            if (!tienePermiso(u, t))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Sin permiso");

            tareaService.actualizarObservacionesConEntregable(
                    idTarea,
                    (observaciones != null ? observaciones : ""),
                    archivo,
                    fechaInicio,
                    fechaLimite
            );
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // =========================
    // LISTAR PROYECTOS POR USUARIO
    // =========================
    @GetMapping("/proyectos-por-usuario")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> proyectosPorUsuario(@RequestParam("idUsuario") Integer idUsuario,
                                                                         HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        if (!esAdminOCoord(u)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList());

        try {
            List<Tarea> proyectos = Optional.ofNullable(tareaService.obtenerMisProyectos(idUsuario))
                    .orElse(Collections.emptyList());

            List<Tarea> tareasUser = Optional.ofNullable(tareaService.obtenerMisTareas(idUsuario))
                    .orElse(Collections.emptyList());
            List<Tarea> proyectosDesdeHijas = extraerProyectosDesdeTareas(tareasUser);

            Map<Integer, Tarea> uniq = new LinkedHashMap<>();
            for (Tarea p : proyectos) {
                if (p == null || !Boolean.TRUE.equals(p.getActivo())) continue;
                uniq.putIfAbsent(p.getIdTarea(), p);
            }
            for (Tarea p : proyectosDesdeHijas) {
                if (p == null || !Boolean.TRUE.equals(p.getActivo())) continue;
                uniq.putIfAbsent(p.getIdTarea(), p);
            }

            List<Map<String, Object>> out = new ArrayList<>();
            for (Tarea p : uniq.values()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("idTarea", p.getIdTarea());
                m.put("titulo", p.getTitulo());
                out.add(m);
            }
            return ResponseEntity.ok(out);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // =========================
    // LISTAR ACTIVIDADES POR PROYECTO
    // =========================
    @GetMapping("/actividades-por-proyecto")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> actividadesPorProyecto(@RequestParam("idProyecto") Integer idProyecto,
                                                                            HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        if (!esAdminOCoord(u)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList());

        try {
            List<Actividad> acts = Optional.ofNullable(tareaService.listarActividadesActivasPorProyecto(idProyecto))
                    .orElse(Collections.emptyList());

            List<Map<String, Object>> out = new ArrayList<>();
            for (Actividad a : acts) {
                if (a == null) continue;
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("idActividad", a.getIdActividad());
                m.put("nombre", a.getNombre());
                out.add(m);
            }
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // =========================
    // MOVER TAREA A PROYECTO / INDEPENDIENTE
    // =========================
    @PostMapping("/mover-a-proyecto")
    @ResponseBody
    public ResponseEntity<String> moverTareaAProyecto(@RequestParam("idTarea") Integer idTarea,
                                                      @RequestParam(value = "idProyectoNuevo", required = false) String idProyectoNuevoRaw,
                                                      HttpSession session) {

        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");

        try {
            Tarea t = tareaRepo.findById(idTarea).orElse(null);
            if (t == null || !Boolean.TRUE.equals(t.getActivo()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No encontrada");

            if (!tienePermiso(u, t))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Sin permiso");

            Integer idProyectoNuevo = parseIntegerOrNull(idProyectoNuevoRaw);
            Integer idPadreAnterior = (t.getProyectoPadre() != null ? t.getProyectoPadre().getIdTarea() : null);

            if (idProyectoNuevo == null) {
                t.setProyectoPadre(null);
                t.setActividad(null);
                tareaRepo.save(t);

                if (idPadreAnterior != null) tareaService.refrescarEstadoProyectoPadre(idPadreAnterior);
                return ResponseEntity.ok("OK");
            }

            Tarea nuevoPadre = tareaRepo.findById(idProyectoNuevo).orElse(null);
            if (nuevoPadre == null || !Boolean.TRUE.equals(nuevoPadre.getActivo()))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Proyecto destino inválido");

            if (!Boolean.TRUE.equals(nuevoPadre.getEsProyecto()))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El destino no es un proyecto");

            t.setProyectoPadre(nuevoPadre);
            t.setActividad(null);
            tareaRepo.save(t);

            if (idPadreAnterior != null) tareaService.refrescarEstadoProyectoPadre(idPadreAnterior);
            tareaService.refrescarEstadoProyectoPadre(idProyectoNuevo);

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // =========================
    // ELIMINAR (soft delete)
    // =========================
    @PostMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<String> eliminarTarea(@PathVariable("id") Integer id,
                                                HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");

        try {
            Tarea t = tareaRepo.findById(id).orElse(null);
            if (t == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No encontrada");

            Integer uid = u.getIdUsuario();
            Integer idCreador = (t.getCreador() != null ? t.getCreador().getIdUsuario() : null);

            boolean permitido = esAdminOCoord(u) || (uid != null && uid.equals(idCreador));
            if (!permitido) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Sin permiso");

            Integer idPadre = (t.getProyectoPadre() != null ? t.getProyectoPadre().getIdTarea() : null);

            t.setActivo(false);
            tareaRepo.save(t);

            if (idPadre != null) tareaService.refrescarEstadoProyectoPadre(idPadre);

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // =========================
    // Extraer proyectos desde tareas
    // =========================
    private List<Tarea> extraerProyectosDesdeTareas(List<Tarea> todas) {
        if (todas == null || todas.isEmpty()) return Collections.emptyList();

        Map<Integer, Tarea> uniq = new LinkedHashMap<>();

        for (Tarea t : todas) {
            if (t == null) continue;
            if (!Boolean.TRUE.equals(t.getActivo())) continue;

            Tarea padre = t.getProyectoPadre();
            if (padre == null) continue;

            Integer pid = padre.getIdTarea();
            if (pid == null) continue;

            uniq.putIfAbsent(pid, padre);
        }

        return new ArrayList<>(uniq.values());
    }

    // =========================
    // ✅ ACTUALIZAR TAREA (titulo/descripcion/prioridad/color/fechas/observaciones)
    // =========================
    @PostMapping("/actualizar-tarea")
    @ResponseBody
    public ResponseEntity<String> actualizarTarea(
            @RequestParam("idTarea") Integer idTarea,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            @RequestParam(value = "prioridad", required = false) String prioridad,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "fechaInicio", required = false) String fechaInicio,
            @RequestParam(value = "fechaLimite", required = false) String fechaLimite,
            HttpSession session
    ) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");

        try {
            Tarea t = tareaRepo.findById(idTarea).orElse(null);
            if (t == null || !Boolean.TRUE.equals(t.getActivo()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No encontrada");

            if (!tienePermiso(u, t))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Sin permiso");

            if (hasText(titulo)) t.setTitulo(titulo.trim());
            if (descripcion != null) t.setDescripcion(descripcion);
            if (observaciones != null) t.setObservaciones(observaciones);

            if (hasText(prioridad)) t.setPrioridad(prioridad.trim().toUpperCase(Locale.ROOT));
            if (hasText(color)) t.setColor(color.trim());

            LocalDateTime fi = parseLdtOrNull(fechaInicio);
            LocalDateTime fl = parseLdtOrNull(fechaLimite);
            if (fi != null) t.setFechaInicio(fi);
            if (fl != null) t.setFechaLimite(fl);

            tareaRepo.save(t);

            if (t.getProyectoPadre() != null && t.getProyectoPadre().getIdTarea() != null) {
                tareaService.refrescarEstadoProyectoPadre(t.getProyectoPadre().getIdTarea());
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
