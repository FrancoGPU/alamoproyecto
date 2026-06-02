package com.alamo.asistencia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class AgendaController {

    @GetMapping("/agenda")
    public String agenda(Model model, HttpSession session) {

        // ✅ MISMO atributo que usa TODO tu sistema
        Object usuario = session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/usuarios/cargarLogin"; // ✅ ruta real de tu login
        }

        // ✅ El sidebar espera "u"
        model.addAttribute("u", usuario);
        model.addAttribute("moduloActivo", "agenda");

        return "agenda"; // agenda.html en templates
    }
}
