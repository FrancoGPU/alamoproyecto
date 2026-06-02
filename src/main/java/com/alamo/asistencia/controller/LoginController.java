package com.alamo.asistencia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alamo.asistencia.repository.IUsuarioRepository;

import jakarta.servlet.http.HttpSession;

import com.alamo.asistencia.model.Usuario;

@Controller
public class LoginController {

    @Autowired
    private IUsuarioRepository repo;

    // Mostrar el Login
    @GetMapping("/usuarios/cargarLogin")
    public String cargarLogin() {
        return "login"; // login.html
    }

    // Validar Login
    @PostMapping("/usuarios/validarLogin")
    public String validarLogin(@RequestParam("dni") String dni,
                               @RequestParam("contrasenia") String contrasenia,
                               HttpSession session, Model model) {

        Usuario user = repo.findByDniAndContrasenia(dni, contrasenia).orElse(null);

        if (user != null) {
            // Lógica de verificación de estado
            if ("Inactivo".equalsIgnoreCase(user.getEstado())) {
                // Si está inactivo, no permitir el acceso y redirigir con error.
                return "redirect:/usuarios/cargarLogin?error=cuenta_inactiva"; 
            }
            
            // Login Correcto
        	session.setAttribute("usuarioLogueado", user);
            return "redirect:/cargarmenu"; 
        } else {
            // Login Incorrecto
            return "redirect:/usuarios/cargarLogin?error=credenciales_invalidas"; 
        }
    }
    
    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate(); // Elimina todo
        return "redirect:/usuarios/cargarLogin";
    }
}