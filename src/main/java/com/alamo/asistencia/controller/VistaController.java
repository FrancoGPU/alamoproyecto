package com.alamo.asistencia.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alamo.asistencia.model.Asistencia;
import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.model.Rol;
import com.alamo.asistencia.model.Horario;
import com.alamo.asistencia.model.PermisoExtra;
import com.alamo.asistencia.model.Saludo;
import com.alamo.asistencia.service.AsistenciaService;
import com.alamo.asistencia.service.SaludoService;
import com.alamo.asistencia.repository.IUsuarioRepository;
import com.alamo.asistencia.repository.IHorarioRepository;
import com.alamo.asistencia.repository.IPermisoExtraRepository;
import com.alamo.asistencia.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
public class VistaController {

    @Autowired
    private AsistenciaService asistenciaService;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private IHorarioRepository horarioRepo;

    @Autowired
    private IPermisoExtraRepository permisoExtraRepo;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SaludoService saludoService;

    private final ZoneId zonaLima = ZoneId.of("America/Lima");

    // ===================== Utils =====================
    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    // ✅ IMPORTANTE: este conteo SOLO ES PARA HORARIOS/PERMISOS
    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("conteoPendientesHorarios", permisoExtraRepo.countByEstado("PENDIENTE"));
    }

    // ===================== MENÚ =====================
    @GetMapping("/cargarmenu")
    public String cargarMenu(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/usuarios/cargarLogin";

        // 1) Cumpleañeros para saludar
        List<Usuario> cumpleaneros = saludoService.obtenerCumpleanerosParaSaludar(usuario.getIdUsuario());
        model.addAttribute("cumpleaneros", cumpleaneros != null ? cumpleaneros : new ArrayList<>());
        model.addAttribute("esMiCumple", saludoService.esMesDeCumpleaniosDeUsuario(usuario));

        // 2) Solo SUS saludos recibidos
        List<Saludo> saludosMuro = saludoService.obtenerSaludosDelMuro(usuario.getIdUsuario());
        model.addAttribute("saludosMuro", saludosMuro != null ? saludosMuro : new ArrayList<>());

        model.addAttribute("u", usuario);
        return "menu";
    }

    // ===================== PERFIL (GET) =====================
    // ✅ FIX: ya NO renderiza "perfil" directamente, porque aquí NO cargas formaciones/certificaciones
    // ✅ Ahora redirige a /perfil (PerfilController) que sí carga esas listas desde BD
    @GetMapping("/cargarPerfil")
    public String cargarPerfil(
            @RequestParam(name = "success", required = false) String successMessage,
            @RequestParam(name = "error", required = false) String errorMessage,
            RedirectAttributes ra,
            HttpSession session
    ) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/usuarios/cargarLogin";

        // refresca el usuario de sesión (datos base)
        Usuario usuarioActualizado = usuarioRepository.findById(usuario.getIdUsuario()).orElse(usuario);
        session.setAttribute("usuarioLogueado", usuarioActualizado);

        // pasa mensajes como flash para que /perfil los muestre
        if (successMessage != null) ra.addFlashAttribute("success", successMessage);
        if (errorMessage != null) ra.addFlashAttribute("error", errorMessage);

        return "redirect:/perfil";
    }

    // ===================== REGISTRO =====================
    @GetMapping("/cargarRegistro")
    public String cargarRegistro(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/usuarios/cargarLogin";

        LocalDate hoy = LocalDate.now(zonaLima);

        boolean tienePermisoExtra = permisoExtraRepo
                .findByUsuario_IdUsuarioAndFechaSolicitudAndEstadoAndUsadoFalse(
                        usuario.getIdUsuario(), hoy, "APROBADO"
                ).isPresent();

        List<Usuario> cumpleaneros = saludoService.obtenerCumpleanerosParaSaludar(usuario.getIdUsuario());
        boolean esMiCumple = saludoService.esMesDeCumpleaniosDeUsuario(usuario);

        model.addAttribute("u", usuario);
        model.addAttribute("tienePermisoExtra", tienePermisoExtra);
        model.addAttribute("cumpleaneros", cumpleaneros != null ? cumpleaneros : new ArrayList<>());
        model.addAttribute("esMiCumple", esMiCumple);

        List<Asistencia> asistenciasDia = asistenciaService.obtenerHistorialDelDia(usuario, hoy);
        model.addAttribute("asistenciasDia", asistenciasDia != null ? asistenciasDia : new ArrayList<>());

        return "registrar";
    }

    // ===================== SOLICITAR EXTRA =====================
    @PostMapping("/solicitarExtra")
    public String solicitarExtra(
            @RequestParam(name = "motivo") String motivo,
            HttpSession session,
            RedirectAttributes ra
    ) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/usuarios/cargarLogin";

        PermisoExtra p = new PermisoExtra();
        p.setUsuario(u);
        p.setFechaSolicitud(LocalDate.now(zonaLima));
        p.setMotivo(trimToNull(motivo));
        p.setEstado("PENDIENTE");
        p.setUsado(false);
        permisoExtraRepo.save(p);

        ra.addFlashAttribute("msg", "✅ Solicitud enviada. Pide al admin que la apruebe para marcar entrada.");
        return "redirect:/cargarRegistro";
    }

    // ===================== HISTORIAL =====================
    @GetMapping("/cargarMiHistorial")
    public String cargarMiHistorial(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/usuarios/cargarLogin";

        model.addAttribute("u", usuario);

        YearMonth ym = YearMonth.now(zonaLima);
        List<Asistencia> historialMes = asistenciaService.obtenerHistorialPorRangoUsuario(
                usuario, ym.atDay(1), ym.atEndOfMonth()
        );

        model.addAttribute("historial", historialMes != null ? historialMes : new ArrayList<>());
        return "historialdia";
    }

    // ===================== REVISION USUARIOS =====================
    @GetMapping("/cargarRevisionUsuarios")
    public String cargarRevisionUsuarios(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/usuarios/cargarLogin";

        Rol rolObj = usuario.getObjRol();
        int rolId = (rolObj != null) ? rolObj.getId_rol() : 0;
        if (rolId != 1 && rolId != 3 && rolId != 4) return "redirect:/cargarmenu";

        List<Usuario> usuarios = usuarioRepository.findAll();
        Map<Integer, List<Horario>> mapaHorarios = new HashMap<>();

        for (Usuario uIter : usuarios) {
            List<Horario> listaH = horarioRepo.findByUsuario_IdUsuarioOrderByDiaAsc(uIter.getIdUsuario());
            mapaHorarios.put(uIter.getIdUsuario(), listaH != null ? listaH : new ArrayList<>());
        }

        model.addAttribute("u", usuario);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("mapaHorarios", mapaHorarios);
        model.addAttribute("estadosAsistencia", usuarioService.obtenerEstadoAsistenciaParaUsuarios(usuarios));

        return "revisionusuarios";
    }

    // ===================== REPORTE =====================
    @GetMapping("/cargarReporte")
    public String cargarReporte() {
        return "redirect:/reporte/subir";
    }

    // ===================== MOBILE =====================
    @GetMapping("/mobile")
    public String mobile() {
        return "mobile";
    }
}