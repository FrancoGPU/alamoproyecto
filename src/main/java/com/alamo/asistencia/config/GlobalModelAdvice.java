package com.alamo.asistencia.config;

import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.repository.IPermisoExtraRepository;
import com.alamo.asistencia.repository.ITareaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private static final String EST_COMPLETADA = "COMPLETADA";

    @Autowired
    private ITareaRepository tareaRepo;

    @Autowired
    private IPermisoExtraRepository permisoExtraRepo;

    @ModelAttribute
    public void agregarConteosGlobales(HttpSession session, Model model) {

        // =========================
        // 1) HORARIOS (pendientes)
        // =========================
        long conteoPendientesHorarios = 0;
        try {
            conteoPendientesHorarios = permisoExtraRepo.countByEstado("PENDIENTE");
        } catch (Exception ignored) {
            conteoPendientesHorarios = 0;
        }
        model.addAttribute("conteoPendientesHorarios", conteoPendientesHorarios);

        // =========================
        // 2) TAREAS (pendientes usuario)
        // =========================
        long conteoTareasPendientes = 0;
        try {
            Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
            if (u != null && u.getIdUsuario() != null) {
                conteoTareasPendientes =
                        tareaRepo.countByResponsable_IdUsuarioAndActivoTrueAndEsProyectoFalseAndEstadoNot(
                                u.getIdUsuario(), EST_COMPLETADA
                        );
            }
        } catch (Exception ignored) {
            conteoTareasPendientes = 0;
        }

        // ✅ compatibilidad: si en algún lado usas "conteoPendientes" para tareas, lo dejamos
        model.addAttribute("conteoPendientes", conteoTareasPendientes);

        // ✅ el correcto para el badge del workspace
        model.addAttribute("conteoTareasPendientes", conteoTareasPendientes);
    }
}
