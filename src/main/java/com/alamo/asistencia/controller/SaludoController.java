package com.alamo.asistencia.controller;

import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.service.SaludoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/saludos")
public class SaludoController {

    @Autowired
    private SaludoService saludoService;

    @PostMapping("/enviar")
    public String enviarSaludo(@RequestParam("idReceptor") Integer idReceptor,
                               @RequestParam("mensaje") String mensaje,
                               HttpSession session,
                               RedirectAttributes ra) {
        
        // Obtenemos el usuario de la sesión
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");

        // Verificamos sesión activa
        if (usuarioLogueado == null) {
            return "redirect:/usuarios/cargarLogin";
        }

        try {
            // Guardamos el saludo usando el Service
            saludoService.guardarSaludo(usuarioLogueado, idReceptor, mensaje);
            
            // Usamos FlashAttributes para que el mensaje sobreviva al redirect
            ra.addFlashAttribute("mensajeExito", "¡Tu saludo ha sido enviado correctamente!");
            
        } catch (Exception e) {
            ra.addFlashAttribute("mensajeError", "No se pudo enviar el saludo: " + e.getMessage());
        }

        /**
         * CORRECCIÓN DE RUTA: 
         * En tu VistaController la ruta es "/cargarRegistro". 
         * Si rediriges a "/registrar" podría darte error 404 si esa ruta no existe en el GetMapping.
         */
        return "redirect:/cargarRegistro"; 
    }
}