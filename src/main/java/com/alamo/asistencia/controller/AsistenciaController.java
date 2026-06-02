package com.alamo.asistencia.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alamo.asistencia.model.Asistencia;
import com.alamo.asistencia.model.Horario;
import com.alamo.asistencia.model.Rol;
import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.repository.IHorarioRepository;
import com.alamo.asistencia.repository.IUsuarioRepository;
import com.alamo.asistencia.service.AsistenciaService;
import com.alamo.asistencia.service.SaludoService;

@Controller
public class AsistenciaController {

    @Autowired private AsistenciaService asistenciaService;
    @Autowired private IHorarioRepository horarioRepo;
    @Autowired private IUsuarioRepository usuarioRepo;
    @Autowired private SaludoService saludoService;

    private final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    private BigDecimal calcularHorasTrabajadas(LocalTime entrada, LocalTime salida) {
        if (entrada == null || salida == null) return BigDecimal.ZERO;

        long minutosTotal = ChronoUnit.MINUTES.between(entrada, salida);
        if (minutosTotal <= 0) return BigDecimal.ZERO;

        return BigDecimal.valueOf(minutosTotal)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private boolean esAdminOJefe(Usuario usuario) {
        if (usuario == null) return false;
        Rol rolObj = usuario.getObjRol();
        int rolId = (rolObj != null) ? rolObj.getId_rol() : 0;
        return (rolId == 1 || rolId == 3);
    }

    @GetMapping("/registrar")
    public String cargarRegistro(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/usuarios/cargarLogin";
        return refrescarVistaRegistro(usuario, model, null);
    }

    @PostMapping("/entrada")
    public String registrarEntrada(
            HttpSession session,
            Model model,
            @RequestParam(name = "ubicacion", required = false) String ubicacion
    ) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/usuarios/cargarLogin";

        String resultado = asistenciaService.registrarEntrada(usuario, ubicacion);
        return refrescarVistaRegistro(usuario, model, resultado);
    }

    @PostMapping("/salida")
    public String registrarSalida(
            HttpSession session,
            Model model,
            @RequestParam(name = "ubicacion", required = false) String ubicacion
    ) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/usuarios/cargarLogin";

        String resultado = asistenciaService.registrarSalida(usuario, ubicacion);
        return refrescarVistaRegistro(usuario, model, resultado);
    }

    @PostMapping("/asistencia/actualizar-manual")
    @ResponseBody
    public String actualizarManual(
            @RequestParam("id") Integer id,
            @RequestParam("entrada") String entrada,
            @RequestParam(name = "salida", required = false) String salida,
            HttpSession session,
            HttpServletRequest request
    ) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "Error: Sesión expirada";
        if (!esAdminOJefe(usuario)) return "Error: No tiene permisos";

        try {
            if (id == null) return "Error: ID requerido";
            if (entrada == null || entrada.isBlank()) return "Error: Hora de entrada requerida";

            LocalTime hEntrada = LocalTime.parse(entrada.trim());

            LocalTime hSalida = null;
            if (salida != null) {
                String s = salida.trim();
                if (!s.isEmpty() && !s.equals("-")) {
                    hSalida = LocalTime.parse(s);
                }
            }

            String ip = (request != null) ? request.getRemoteAddr() : null;
            asistenciaService.actualizarManual(id, hEntrada, hSalida, usuario, ip);

            return "OK";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/asistencia/registrar-manual")
    @ResponseBody
    public String registrarManual(
            @RequestParam("idUsuario") Integer idUsuario,
            @RequestParam("fecha") String fecha,
            @RequestParam("entrada") String entrada,
            @RequestParam(name = "salida", required = false) String salida,
            @RequestParam(name = "ubicacion", required = false) String ubicacion,
            HttpSession session,
            HttpServletRequest request
    ) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "Error: Sesión expirada";
        if (!esAdminOJefe(usuario)) return "Error: No tiene permisos";

        try {
            if (idUsuario == null) return "Error: Usuario requerido";
            if (fecha == null || fecha.isBlank()) return "Error: Fecha requerida";
            if (entrada == null || entrada.isBlank()) return "Error: Hora de entrada requerida";

            LocalDate f = LocalDate.parse(fecha.trim());
            LocalTime hEntrada = LocalTime.parse(entrada.trim());

            LocalTime hSalida = null;
            if (salida != null && !salida.trim().isEmpty() && !salida.trim().equals("-")) {
                hSalida = LocalTime.parse(salida.trim());
            }

            String ip = (request != null) ? request.getRemoteAddr() : null;
            asistenciaService.registrarManual(idUsuario, f, hEntrada, hSalida, ubicacion, usuario, ip);

            return "OK";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/asistencia/eliminar-manual")
    @ResponseBody
    public String eliminarManual(
            @RequestParam("id") Integer id,
            HttpSession session,
            HttpServletRequest request
    ) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "Error: Sesión expirada";
        if (!esAdminOJefe(usuario)) return "Error: No tiene permisos";

        try {
            if (id == null) return "Error: ID requerido";

            String ip = (request != null) ? request.getRemoteAddr() : null;
            asistenciaService.eliminarManual(id, usuario, ip);

            return "OK";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/cargarHistorialGeneral")
    public String cargarHistorialGeneral(
            @RequestParam(name = "mes", required = false) Integer mes,
            @RequestParam(name = "anio", required = false) Integer anio,
            HttpSession session,
            Model model
    ) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/usuarios/cargarLogin";
        if (!esAdminOJefe(usuario)) return "redirect:/cargarmenu";

        LocalDate ahora = LocalDate.now(ZONA_LIMA);
        int m = (mes != null && mes >= 1 && mes <= 12) ? mes : ahora.getMonthValue();
        int a = (anio != null && anio >= 2000 && anio <= 2100) ? anio : ahora.getYear();

        LocalDate inicio = LocalDate.of(a, m, 1);
        LocalDate fin = inicio.withDayOfMonth(inicio.lengthOfMonth());

        List<Asistencia> lista = asistenciaService.obtenerHistorialPorRango(inicio, fin);

        Map<Usuario, List<Asistencia>> historialAgrupado = lista.stream()
                .filter(asis -> asis.getUsuario() != null)
                .collect(Collectors.groupingBy(
                        Asistencia::getUsuario,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<Usuario> usuarios = usuarioRepo.findAll().stream()
                .filter(u -> u != null && u.getIdUsuario() != null)
                .sorted((u1, u2) -> {
                    String n1 = ((u1.getNombres() != null ? u1.getNombres() : "") + " "
                            + (u1.getApellido_paterno() != null ? u1.getApellido_paterno() : "") + " "
                            + (u1.getApellido_materno() != null ? u1.getApellido_materno() : "")).trim();

                    String n2 = ((u2.getNombres() != null ? u2.getNombres() : "") + " "
                            + (u2.getApellido_paterno() != null ? u2.getApellido_paterno() : "") + " "
                            + (u2.getApellido_materno() != null ? u2.getApellido_materno() : "")).trim();

                    return n1.compareToIgnoreCase(n2);
                })
                .collect(Collectors.toList());

        model.addAttribute("u", usuario);
        model.addAttribute("historialAgrupado", historialAgrupado);
        model.addAttribute("mes", m);
        model.addAttribute("anio", a);
        model.addAttribute("usuarios", usuarios);

        return "historialgeneral";
    }

    private String refrescarVistaRegistro(Usuario usuario, Model model, String mensaje) {
        LocalDate hoy = LocalDate.now(ZONA_LIMA);
        int diaSemana = hoy.getDayOfWeek().getValue();

        List<Horario> horariosHoy = horarioRepo.findByUsuario_IdUsuarioAndDia(usuario.getIdUsuario(), diaSemana);
        List<Asistencia> asistenciasDia = asistenciaService.obtenerHistorialDelDia(usuario, hoy);

        List<Usuario> cumpleaneros = saludoService.obtenerCumpleanerosParaSaludar(usuario.getIdUsuario());
        if (cumpleaneros == null) cumpleaneros = new ArrayList<>();

        boolean esMiCumple = saludoService.esMesDeCumpleaniosDeUsuario(usuario);

        model.addAttribute("cumpleaneros", cumpleaneros);
        model.addAttribute("esMiCumple", esMiCumple);
        model.addAttribute("u", usuario);
        model.addAttribute("asistenciasDia", asistenciasDia);
        model.addAttribute("msg", mensaje);
        model.addAttribute("horariosHoy", horariosHoy);

        return "registrar";
    }

    @GetMapping("/historialdia")
    public String historialDia(
            @RequestParam(name = "mes", required = false) Integer mes,
            @RequestParam(name = "anio", required = false) Integer anio,
            HttpSession session,
            Model model
    ) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/usuarios/cargarLogin";

        LocalDate ahora = LocalDate.now(ZONA_LIMA);

        int mesSel = (mes != null && mes >= 1 && mes <= 12) ? mes : ahora.getMonthValue();
        int anioSel = (anio != null && anio >= 2000 && anio <= 2100) ? anio : ahora.getYear();

        LocalDate inicio = LocalDate.of(anioSel, mesSel, 1);
        LocalDate fin = inicio.withDayOfMonth(inicio.lengthOfMonth());

        List<Asistencia> historial = asistenciaService.obtenerHistorialPorRangoUsuario(usuario, inicio, fin);

        for (Asistencia a : historial) {
            if (a.getHoraEntrada() != null && a.getHoraSalida() != null) {
                a.setHorasTrabajadas(calcularHorasTrabajadas(a.getHoraEntrada(), a.getHoraSalida()));
            }
            if (a.getHorasTrabajadas() == null) a.setHorasTrabajadas(BigDecimal.ZERO);
            if (a.getMinutosExtra() == null) a.setMinutosExtra(0);
            if (a.getMinutosTardanza() == null) a.setMinutosTardanza(0);
        }

        model.addAttribute("u", usuario);
        model.addAttribute("historial", historial);
        model.addAttribute("mesActual", mesSel);
        model.addAttribute("anioActual", anioSel);

        return "historialdia";
    }
}