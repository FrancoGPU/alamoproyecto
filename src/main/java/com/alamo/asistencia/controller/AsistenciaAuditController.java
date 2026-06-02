package com.alamo.asistencia.controller;

import com.alamo.asistencia.model.Asistencia;
import com.alamo.asistencia.model.AsistenciaAudit;
import com.alamo.asistencia.model.Rol;
import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.repository.AsistenciaAuditRepository;
import com.alamo.asistencia.repository.IAsistenciaRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/asistencia")
public class AsistenciaAuditController {

    private final AsistenciaAuditRepository auditRepo;
    private final IAsistenciaRepository asistenciaRepo;

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==========================================================
    // 1) Auditoría por asistencia específica (TOP 20)
    // GET /asistencia/{idAsistencia}/auditoria
    // ==========================================================
    @GetMapping("/{idAsistencia}/auditoria")
    public List<AuditDTO> auditoriaPorAsistencia(
            @PathVariable Integer idAsistencia,
            HttpSession session
    ) {
        Usuario usuario = requireUsuario(session);
        requireAdminOJefe(usuario);

        List<AsistenciaAudit> lista =
                auditRepo.findTop20ByIdAsistenciaOrderByFechaAccionDesc(idAsistencia);

        return lista.stream().map(AsistenciaAuditController::toDto).toList();
    }

    // ==========================================================
    // 2) Auditoría GENERAL por mes/año (PANEL LATERAL)
    // GET /asistencia/auditoria?mes=2&anio=2026
    // ==========================================================
    @GetMapping("/auditoria")
    public List<AuditDTOGeneral> auditoriaGeneral(
            @RequestParam int mes,
            @RequestParam int anio,
            @RequestParam(defaultValue = "200") int limit,
            HttpSession session
    ) {
        Usuario usuario = requireUsuario(session);
        requireAdminOJefe(usuario);

        if (mes < 1 || mes > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mes inválido");
        }
        if (anio < 2000 || anio > 2100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Año inválido");
        }

        if (limit < 1) limit = 50;
        if (limit > 1000) limit = 1000;

        LocalDateTime desde = LocalDate.of(anio, mes, 1).atStartOfDay();
        LocalDateTime hasta = LocalDate.of(anio, mes, 1).plusMonths(1).atStartOfDay();

        List<AsistenciaAudit> lista = auditRepo
                .findByFechaAccionGreaterThanEqualAndFechaAccionLessThanOrderByFechaAccionDesc(
                        desde, hasta, PageRequest.of(0, limit)
                );

        return lista.stream()
                .map(this::toDtoGeneral)
                .toList();
    }

    // ==========================================================
    // DTOs
    // ==========================================================
    public record AuditDTO(
            String accion,
            String actor,
            String fecha,
            String entradaAntes,
            String salidaAntes,
            String entradaDespues,
            String salidaDespues,
            String ip
    ) {}

    public record AuditDTOGeneral(
            String accion,
            String actor,
            String afectado,
            String fecha,
            String entradaAntes,
            String salidaAntes,
            String entradaDespues,
            String salidaDespues,
            String ip,
            Integer idAsistencia
    ) {}

    // ==========================================================
    // Mappers
    // ==========================================================
    private static AuditDTO toDto(AsistenciaAudit a) {
        return new AuditDTO(
                t(a.getAccion()),
                nombreCompleto(a.getUsuarioActor()),
                fmtFecha(a.getFechaAccion()),
                t(a.getEntradaAntes()),
                t(a.getSalidaAntes()),
                t(a.getEntradaDespues()),
                t(a.getSalidaDespues()),
                t(a.getIpActor())
        );
    }

    private AuditDTOGeneral toDtoGeneral(AsistenciaAudit a) {
        String afectado = "—";
        Integer idAsis = a.getIdAsistencia();

        if (idAsis != null) {
            Asistencia asis = asistenciaRepo.findById(idAsis).orElse(null);
            if (asis != null && asis.getUsuario() != null) {
                afectado = nombreCompleto(asis.getUsuario());
            }
        }

        return new AuditDTOGeneral(
                t(a.getAccion()),
                nombreCompleto(a.getUsuarioActor()),
                afectado,
                fmtFecha(a.getFechaAccion()),
                t(a.getEntradaAntes()),
                t(a.getSalidaAntes()),
                t(a.getEntradaDespues()),
                t(a.getSalidaDespues()),
                t(a.getIpActor()),
                idAsis
        );
    }

    // ==========================================================
    // Helpers permisos / sesión
    // ==========================================================
    private static Usuario requireUsuario(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión no válida");
        }
        return usuario;
    }

    private static void requireAdminOJefe(Usuario usuario) {
        if (!esAdminOJefe(usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin permisos");
        }
    }

    private static boolean esAdminOJefe(Usuario usuario) {
        if (usuario == null) return false;
        Rol rolObj = usuario.getObjRol();
        int rolId = (rolObj != null) ? rolObj.getId_rol() : 0;
        return (rolId == 1 || rolId == 3);
    }

    private static String fmtFecha(LocalDateTime dt) {
        if (dt == null) return "-";
        return dt.atZone(ZONA_LIMA).format(FMT);
    }

    private static String t(Object x) {
        return (x == null) ? "-" : x.toString();
    }

    private static String nombreCompleto(Usuario u) {
        if (u == null) return "—";
        return (safe(u.getNombres()) + " " + safe(u.getApellido_paterno()) + " " + safe(u.getApellido_materno())).trim();
    }

    private static String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}