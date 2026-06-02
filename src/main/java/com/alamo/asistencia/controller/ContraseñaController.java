package com.alamo.asistencia.controller;

import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.repository.IUsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/perfil")
public class ContraseñaController {

    @Autowired
    private IUsuarioRepository usuarioRepository;

    // Mostrar página de cambiar contraseña
    @GetMapping("/cambiar-contrasenia")
    public String mostrarCambiarContrasenia(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) {
            return "redirect:/usuarios/cargarLogin"; // Redirige al login si no hay sesión
        }

        model.addAttribute("u", u);
        return "contraseña";
    }

    // Procesar cambio de contraseña
    @PostMapping("/cambiar-contrasenia")
    public String cambiarContrasenia(@RequestParam("contraseniaActual") String contraseniaActual,
                                     @RequestParam("nuevaContrasenia") String nuevaContrasenia,
                                     @RequestParam("confirmarContrasenia") String confirmarContrasenia,
                                     HttpSession session,
                                     Model model) {

        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) {
            return "redirect:/usuarios/cargarLogin"; // Redirige al login si no hay sesión
        }

        // Validar contraseña actual
        if (!u.getContrasenia().equals(contraseniaActual)) {
            model.addAttribute("error", "La contraseña actual es incorrecta.");
            model.addAttribute("u", u);
            return "contraseña";
        }

        // Validar que la nueva contraseña no esté vacía y coincida
        if (nuevaContrasenia == null || nuevaContrasenia.isEmpty()) {
            model.addAttribute("error", "La nueva contraseña no puede estar vacía.");
            model.addAttribute("u", u);
            return "contraseña";
        }

        if (!nuevaContrasenia.equals(confirmarContrasenia)) {
            model.addAttribute("error", "La nueva contraseña y la confirmación no coinciden.");
            model.addAttribute("u", u);
            return "contraseña";
        }

        // Guardar la nueva contraseña
        u.setContrasenia(nuevaContrasenia);
        usuarioRepository.save(u);

        // Actualizar el usuario en sesión
        session.setAttribute("usuarioLogueado", u);

        model.addAttribute("success", "Contraseña actualizada correctamente.");
        model.addAttribute("u", u);
        return "contraseña";
    }
}
