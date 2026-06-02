package com.alamo.asistencia.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.alamo.asistencia.dto.DiaControlDTO;                 // ✅ NUEVO
import com.alamo.asistencia.model.Asistencia;
import com.alamo.asistencia.model.Horario;
import com.alamo.asistencia.model.Informe;
import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.service.AsistenciaService;
import com.alamo.asistencia.service.HorarioService;
import com.alamo.asistencia.service.InformeService;
import com.alamo.asistencia.service.InformesExcelService;
import com.alamo.asistencia.service.UsuarioService;
import com.alamo.asistencia.service.InformeCalendarioService; // ✅ NUEVO

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Controller
@RequestMapping("/informes")
public class InformeController {

    private final InformeService informeService;
    private final UsuarioService usuarioService;
    private final AsistenciaService asistenciaService;
    private final HorarioService horarioService;

    // ✅ NUEVO: calendario (Descanso / No asistió)
    private final InformeCalendarioService informeCalendarioService;

    // ✅ Excel
    private final InformesExcelService informesExcelService;
    private final ObjectMapper objectMapper;

    // ✅ consistencia con tu sistema (Lima)
    private final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    public InformeController(InformeService informeService,
                             UsuarioService usuarioService,
                             AsistenciaService asistenciaService,
                             HorarioService horarioService,
                             InformesExcelService informesExcelService,
                             ObjectMapper objectMapper,
                             InformeCalendarioService informeCalendarioService) { // ✅ NUEVO PARAM
        this.informeService = informeService;
        this.usuarioService = usuarioService;
        this.asistenciaService = asistenciaService;
        this.horarioService = horarioService;

        this.informesExcelService = informesExcelService;
        this.objectMapper = objectMapper;

        this.informeCalendarioService = informeCalendarioService; // ✅ NUEVO
    }

    // =========================================================
    // Utils
    // =========================================================
    private int horasBigDecimalAMinutos(BigDecimal horas) {
        if (horas == null) return 0;
        // 2 decimales -> minutos con redondeo a minuto
        return horas.multiply(BigDecimal.valueOf(60))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    // =========================================================
    // MOSTRAR INFORMES Y HORAS (CONSOLIDADO)
    // =========================================================
    @GetMapping
    public String mostrarInformes(
            @RequestParam(name = "usuarioId", required = false) Integer usuarioId,
            @RequestParam(name = "mes", required = false) Integer mes,
            @RequestParam(name = "anio", required = false) Integer anio,
            @RequestParam(name = "semana", required = false) Integer semana,
            HttpSession session,
            Model model) {

        Usuario usuarioLog = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLog == null) return "redirect:/usuarios/cargarLogin";

        int rol = (usuarioLog.getObjRol() != null) ? usuarioLog.getObjRol().getId_rol() : 0;
        if (rol != 1 && rol != 3 && rol != 4) {
            model.addAttribute("error", "No tienes permiso para acceder a los informes");
            return "informes";
        }

        // --- 1) RANGO DE FECHAS ---
        LocalDate hoy = LocalDate.now(ZONA_LIMA);
        LocalDate inicio;
        LocalDate fin;

        Integer mesActualModel;
        Integer anioActualModel;

        if (semana != null && semana > 0) {
            inicio = hoy.minusDays(semana * 7L);
            fin = hoy;
            mesActualModel = inicio.getMonthValue();
            anioActualModel = inicio.getYear();
        } else {
            anioActualModel = (anio == null || anio == 0) ? hoy.getYear() : anio;
            mesActualModel = (mes == null || mes == 0) ? hoy.getMonthValue() : mes;

            inicio = LocalDate.of(anioActualModel, mesActualModel, 1);
            fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
        }

        // --- 2) DATOS AL MODELO ---
        List<Usuario> listaUsuarios = usuarioService.listarUsuarios();

        model.addAttribute("u", usuarioLog);
        model.addAttribute("usuarios", listaUsuarios);
        model.addAttribute("mesActual", mesActualModel);
        model.addAttribute("anioActual", anioActualModel);
        model.addAttribute("semanaActual", semana);
        model.addAttribute("usuarioId", usuarioId);

        // --- MAPA DE HORARIOS PARA MODAL ---
        Map<Integer, List<Horario>> mapaHorarios = listaUsuarios.stream()
                .collect(Collectors.toMap(
                        Usuario::getIdUsuario,
                        u -> horarioService.listarPorUsuario(u.getIdUsuario()),
                        (v1, v2) -> v1
                ));
        model.addAttribute("mapaHorarios", mapaHorarios);

        // =========================================================
        // ✅ NUEVO: CALENDARIO COMPLETO (Descanso / No asistió / Asistió)
        // =========================================================
        List<Integer> userIds;
        if (usuarioId != null) {
            userIds = List.of(usuarioId);
        } else {
            userIds = listaUsuarios.stream()
                    .map(Usuario::getIdUsuario)
                    .collect(Collectors.toList());
        }

        Map<Integer, List<DiaControlDTO>> detalleCalendarioPorUsuario =
                informeCalendarioService.buildCalendario(userIds, inicio, fin);

        model.addAttribute("detalleCalendarioPorUsuario", detalleCalendarioPorUsuario);

        // --- 3) INFORMES EN RANGO ---
        List<Informe> informes = informeService.listarPorRangoFechas(inicio, fin);

        if (usuarioId != null) {
            final Integer uid = usuarioId;
            informes = informes.stream()
                    .filter(inf -> inf.getAsistencia() != null
                            && inf.getAsistencia().getUsuario() != null
                            && Objects.equals(inf.getAsistencia().getUsuario().getIdUsuario(), uid))
                    .collect(Collectors.toList());
        }

        Map<Usuario, List<Informe>> informesAgrupados = informes.stream()
                .filter(inf -> inf.getAsistencia() != null && inf.getAsistencia().getUsuario() != null)
                .collect(Collectors.groupingBy(
                        inf -> inf.getAsistencia().getUsuario(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        model.addAttribute("informesAgrupados", informesAgrupados);

        // --- 4) ASISTENCIAS EN RANGO ---
        List<Asistencia> asistencias;
        if (usuarioId != null) {
            Usuario usrFiltro = usuarioService.obtenerUsuario(usuarioId).orElse(null);
            asistencias = (usrFiltro != null)
                    ? asistenciaService.obtenerHistorialPorRangoUsuario(usrFiltro, inicio, fin)
                    : new ArrayList<>();
        } else {
            asistencias = asistenciaService.obtenerHistorialPorRango(inicio, fin);
        }

        // =========================================================
        // ✅ NO recalcular horasTrabajadas aquí
        // =========================================================
        for (Asistencia asis : asistencias) {
            if (asis.getHorasTrabajadas() == null) asis.setHorasTrabajadas(BigDecimal.ZERO);
            if (asis.getMinutosExtra() == null) asis.setMinutosExtra(0);
            if (asis.getMinutosTardanza() == null) asis.setMinutosTardanza(0);
        }

        // --- 5) MAPAS PARA GRID ---
        Map<Integer, Map<LocalDate, List<Asistencia>>> asistenciasPorDia = asistencias.stream()
                .filter(asis -> asis.getUsuario() != null)
                .collect(Collectors.groupingBy(
                        asis -> asis.getUsuario().getIdUsuario(),
                        Collectors.groupingBy(Asistencia::getFecha, TreeMap::new, Collectors.toList())
                ));

        Map<Usuario, List<Asistencia>> asistenciasAgrupadas = asistencias.stream()
                .filter(asis -> asis.getUsuario() != null)
                .collect(Collectors.groupingBy(Asistencia::getUsuario, LinkedHashMap::new, Collectors.toList()));

        // ✅ EXTRA por usuario (min)
        Map<Integer, Integer> totalMinExtraPorUsuario = asistencias.stream()
                .filter(asis -> asis.getUsuario() != null)
                .collect(Collectors.groupingBy(
                        asis -> asis.getUsuario().getIdUsuario(),
                        Collectors.summingInt(a -> a.getMinutosExtra() != null ? a.getMinutosExtra() : 0)
                ));

        // ✅ BASE por usuario (min)
        Map<Integer, Integer> totalMinBasePorUsuario = asistencias.stream()
                .filter(asis -> asis.getUsuario() != null)
                .collect(Collectors.groupingBy(
                        asis -> asis.getUsuario().getIdUsuario(),
                        Collectors.summingInt(a -> horasBigDecimalAMinutos(a.getHorasTrabajadas()))
                ));

        // ✅ TOTAL por usuario (min) = BASE + EXTRA
        Map<Integer, Integer> totalMinTotalesPorUsuario = new HashMap<>();
        Set<Integer> allKeys = new HashSet<>();
        allKeys.addAll(totalMinBasePorUsuario.keySet());
        allKeys.addAll(totalMinExtraPorUsuario.keySet());

        for (Integer uid : allKeys) {
            int baseMin = totalMinBasePorUsuario.getOrDefault(uid, 0);
            int extraMin = totalMinExtraPorUsuario.getOrDefault(uid, 0);
            totalMinTotalesPorUsuario.put(uid, baseMin + extraMin);
        }

        // ✅ TARDANZA por usuario (min)
        Map<Integer, Integer> totalMinTardanzaPorUsuario = asistencias.stream()
                .filter(asis -> asis.getUsuario() != null)
                .collect(Collectors.groupingBy(
                        asis -> asis.getUsuario().getIdUsuario(),
                        Collectors.summingInt(a -> a.getMinutosTardanza() != null ? a.getMinutosTardanza() : 0)
                ));

        // ✅ DÍAS CON TARDANZA
        Map<Integer, Integer> diasConTardanzaPorUsuario = new HashMap<>();
        for (Map.Entry<Integer, Map<LocalDate, List<Asistencia>>> userEntry : asistenciasPorDia.entrySet()) {

            Integer uid = userEntry.getKey();
            Map<LocalDate, List<Asistencia>> asistenciasPorFecha = userEntry.getValue();

            int diasTarde = 0;

            for (Map.Entry<LocalDate, List<Asistencia>> fechaEntry : asistenciasPorFecha.entrySet()) {

                int tardanzaDelDia = fechaEntry.getValue().stream()
                        .map(Asistencia::getMinutosTardanza)
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue)
                        .sum();

                if (tardanzaDelDia > 0) diasTarde++;
            }

            diasConTardanzaPorUsuario.put(uid, diasTarde);
        }

        // ✅ TOTAL horas basado en TOTAL minutos
        Map<Integer, BigDecimal> totalHorasPorUsuario = new HashMap<>();
        for (Integer uid : allKeys) {
            int totalMin = totalMinTotalesPorUsuario.getOrDefault(uid, 0);
            BigDecimal totalHoras = BigDecimal.valueOf(totalMin)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            totalHorasPorUsuario.put(uid, totalHoras);
        }

        // --- 6) MODEL ATTRS ---
        model.addAttribute("asistenciasPorDia", asistenciasPorDia);
        model.addAttribute("asistenciasAgrupadas", asistenciasAgrupadas);

        model.addAttribute("totalHorasPorUsuario", totalHorasPorUsuario);
        model.addAttribute("totalMinExtraPorUsuario", totalMinExtraPorUsuario);
        model.addAttribute("totalMinBasePorUsuario", totalMinBasePorUsuario);
        model.addAttribute("totalMinTotalesPorUsuario", totalMinTotalesPorUsuario);

        model.addAttribute("totalMinTardanzaPorUsuario", totalMinTardanzaPorUsuario);
        model.addAttribute("diasConTardanzaPorUsuario", diasConTardanzaPorUsuario);

        return "informes";
    }

    // =========================================================
    // ✅ EXPORT EXCEL (incluye checks)
    // =========================================================
    @PostMapping("/export/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(name = "usuarioId", required = false) Integer usuarioId,
            @RequestParam(name = "mes", required = false) Integer mes,
            @RequestParam(name = "anio", required = false) Integer anio,
            @RequestParam(name = "semana", required = false) Integer semana,
            @RequestParam(name = "revisadosJson", required = false) String revisadosJson,
            HttpSession session
    ) throws Exception {

        Usuario usuarioLog = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLog == null) {
            return ResponseEntity.status(401).build();
        }

        int rol = (usuarioLog.getObjRol() != null) ? usuarioLog.getObjRol().getId_rol() : 0;
        if (rol != 1 && rol != 3 && rol != 4) {
            return ResponseEntity.status(403).build();
        }

        // --- rango igual que tu GET ---
        LocalDate hoy = LocalDate.now(ZONA_LIMA);
        LocalDate inicio;
        LocalDate fin;

        Integer mesModel;
        Integer anioModel;

        if (semana != null && semana > 0) {
            inicio = hoy.minusDays(semana * 7L);
            fin = hoy;
            mesModel = inicio.getMonthValue();
            anioModel = inicio.getYear();
        } else {
            anioModel = (anio == null || anio == 0) ? hoy.getYear() : anio;
            mesModel = (mes == null || mes == 0) ? hoy.getMonthValue() : mes;

            inicio = LocalDate.of(anioModel, mesModel, 1);
            fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
        }

        // --- parse revisados ---
        Map<Integer, Boolean> revisados = new HashMap<>();
        if (revisadosJson != null && !revisadosJson.isBlank()) {
            try {
                revisados = objectMapper.readValue(
                        revisadosJson,
                        new TypeReference<Map<Integer, Boolean>>() {}
                );
            } catch (Exception ex) {
                // si mandan mal el json, no rompemos: solo exportamos sin revisados
                revisados = new HashMap<>();
            }
        }

        byte[] file = informesExcelService.buildExcel(inicio, fin, usuarioId, revisados);

        String filename = String.format("informes_%04d-%02d.xlsx", anioModel, mesModel);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(file);
    }

    // =========================================================
    // DESCARGAR/VER INFORMES
    // =========================================================
    @GetMapping("/descargar/{id}")
    public void descargarInforme(
            @PathVariable("id") Integer id,
            @RequestParam(name = "download", required = false) Boolean download,
            HttpServletResponse response) throws IOException {

        Informe inf = informeService.obtenerPorId(id);
        if (inf == null || inf.getRutaArchivo() == null) {
            response.sendError(404, "Informe no encontrado");
            return;
        }

        File archivo = new File(inf.getRutaArchivo());
        if (!archivo.exists()) {
            response.sendError(404, "Archivo físico no encontrado");
            return;
        }

        response.setContentType("application/pdf");
        String disposition = (download != null && download) ? "attachment" : "inline";
        response.setHeader("Content-Disposition", disposition + "; filename=\"" + inf.getNombreArchivo() + "\"");

        Files.copy(archivo.toPath(), response.getOutputStream());
        response.getOutputStream().flush();
    }
}
