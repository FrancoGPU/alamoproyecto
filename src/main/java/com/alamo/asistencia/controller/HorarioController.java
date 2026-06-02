package com.alamo.asistencia.controller;

import com.alamo.asistencia.model.Horario;
import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.model.PermisoExtra;
import com.alamo.asistencia.repository.ITurnoRepository;
import com.alamo.asistencia.repository.IUsuarioRepository;
import com.alamo.asistencia.repository.IPermisoExtraRepository;
import com.alamo.asistencia.service.HorarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cargarHorarios")
public class HorarioController {

    @Autowired
    private HorarioService service;
    @Autowired
    private IUsuarioRepository usuarioRepo;
    @Autowired
    private ITurnoRepository turnoRepo;
    @Autowired
    private IPermisoExtraRepository permisoExtraRepo;

    private final ZoneId zonaLima = ZoneId.of("America/Lima");

    /**
     * ABRE LA VISTA PRINCIPAL
     * Centraliza la carga de datos para Thymeleaf
     */
    @GetMapping
    public String abrirVista(
            @RequestParam(name = "filtroUsuarioId", required = false) Integer filtroUsuarioId,
            @RequestParam(name = "filtroMes", required = false) Integer filtroMes,
            @RequestParam(name = "filtroAnio", required = false) Integer filtroAnio,
            Model model, HttpSession session) {
        
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null) return "redirect:/usuarios/cargarLogin"; 

        int rolId = (usuarioLogueado.getObjRol() != null) ? usuarioLogueado.getObjRol().getId_rol() : 0;
        // Solo Admin (1) o SuperAdmin (3) pueden gestionar horarios
        if (rolId != 1 && rolId != 3) return "redirect:/cargarmenu";

        LocalDate hoy = LocalDate.now(zonaLima);
        int mesBusqueda = (filtroMes != null) ? filtroMes : hoy.getMonthValue();
        int anioBusqueda = (filtroAnio != null) ? filtroAnio : hoy.getYear();

        model.addAttribute("u", usuarioLogueado);
        model.addAttribute("usuarios", usuarioRepo.findAll());
        model.addAttribute("turnos", turnoRepo.findAll());
        
        // Datos para el Badge y el Modal de aprobación
        model.addAttribute("conteoPendientes", permisoExtraRepo.countByEstado("PENDIENTE"));
        model.addAttribute("solicitudesPendientes", permisoExtraRepo.findByEstadoOrderByFechaSolicitudDesc("PENDIENTE"));
        
        model.addAttribute("filtroUsuarioId", filtroUsuarioId);
        model.addAttribute("filtroMes", mesBusqueda);
        model.addAttribute("filtroAnio", anioBusqueda);

        return "Horarios"; 
    }

    /**
     * API: Obtener horarios de un usuario para el modal
     * URL: /cargarHorarios/api/usuario/{id}
     */
    @GetMapping("/api/usuario/{id}")
    @ResponseBody 
    public ResponseEntity<?> obtener(@PathVariable(name = "id") Integer id) { 
        try {
            List<Horario> horarios = service.listarPorUsuario(id);
            return ResponseEntity.ok(horarios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * API: Guardar o Eliminar Horario Manual (T1 o T2)
     * URL: /cargarHorarios/api/guardarManual
     * Se ajustó para que si entrada/salida son vacíos, se proceda a eliminar.
     */
    @PostMapping("/api/guardarManual")
    @ResponseBody
    public ResponseEntity<?> guardarManual(
            @RequestParam(name = "idUsuario") Integer idUsuario, 
            @RequestParam(name = "entrada", required = false) String entrada, 
            @RequestParam(name = "salida", required = false) String salida, 
            @RequestParam(name = "dia") Integer dia,
            @RequestParam(name = "idTurno") Integer idTurno) { // idTurno representa el bloque 1 o 2
        try {
            // Lógica de eliminación: si los parámetros llegan vacíos o nulos (acción del botón borrar)
            if (entrada == null || entrada.trim().isEmpty() || salida == null || salida.trim().isEmpty()) {
                service.eliminarHorarioManual(idUsuario, dia, idTurno);
                return ResponseEntity.ok(Map.of("status", "deleted", "message", "Horario eliminado correctamente"));
            }

            // Lógica de guardado o actualización normal
            service.guardarHorarioManual(idUsuario, entrada, salida, dia, idTurno);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Formulario: Solicitar Horas Extra (Desde vista Marcación)
     */
    @PostMapping("/solicitarExtra")
    public String solicitarExtra(
            @RequestParam(name = "motivo") String motivo, 
            HttpSession session, 
            RedirectAttributes ads) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/usuarios/cargarLogin";

        PermisoExtra solicitud = new PermisoExtra();
        solicitud.setUsuario(u);
        solicitud.setFechaSolicitud(LocalDate.now(zonaLima));
        solicitud.setMotivo(motivo);
        solicitud.setEstado("PENDIENTE");
        solicitud.setUsado(false); 
        
        permisoExtraRepo.save(solicitud);
        ads.addFlashAttribute("msg", "Solicitud enviada correctamente.");
        return "redirect:/cargarRegistro";
    }
}