package com.alamo.asistencia.controller;

import java.time.LocalDate;
import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.alamo.asistencia.model.Asistencia;
import com.alamo.asistencia.model.Informe;
import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.service.AsistenciaService;
import com.alamo.asistencia.service.InformeService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/reporte")
public class ReporteController {

    private final InformeService informeService;
    private final AsistenciaService asistenciaService;

    public ReporteController(InformeService informeService, AsistenciaService asistenciaService) {
        this.informeService = informeService;
        this.asistenciaService = asistenciaService;
    }

    @GetMapping("/subir")
    public String mostrarFormulario(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/usuarios/cargarLogin";

        model.addAttribute("u", usuario);

        // Cambiado: Ahora verificamos si obtenerEntradaHoy devuelve un registro
        Asistencia asistenciaActiva = asistenciaService.obtenerEntradaHoy(usuario);
        model.addAttribute("tieneEntrada", asistenciaActiva != null);
        
        return "reportes";
    }

    @PostMapping("/subir")
    public String subirArchivo(@RequestParam("archivo") MultipartFile archivo,
                                 HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/usuarios/cargarLogin";

        model.addAttribute("u", usuario);

        try {
            // Buscamos la entrada activa (la que no tiene salida aún)
            Asistencia asistenciaHoy = asistenciaService.obtenerEntradaHoy(usuario);

            if (asistenciaHoy == null) {
                model.addAttribute("error", "No puedes subir un reporte. Debes registrar tu entrada y no haber marcado salida aún.");
                model.addAttribute("tieneEntrada", false);
                return "reportes";
            }

            // ⭐ MODIFICACIÓN CLAVE: Se pasa el 'usuario' al servicio
            Informe informe = informeService.guardarArchivo(archivo, usuario); 
            
            informe.setAsistencia(asistenciaHoy);
            informeService.guardarInforme(informe);

            model.addAttribute("success", "Archivo PDF subido correctamente: " + informe.getNombreArchivo());
            model.addAttribute("tieneEntrada", true);

        } catch (IOException e) {
            model.addAttribute("error", "Error de archivo: " + e.getMessage());
            model.addAttribute("tieneEntrada", asistenciaService.obtenerEntradaHoy(usuario) != null);
        } catch (Exception e) {
            model.addAttribute("error", "Error inesperado al subir archivo: " + e.getMessage());
            model.addAttribute("tieneEntrada", asistenciaService.obtenerEntradaHoy(usuario) != null);
        }

        return "reportes";
    }
}