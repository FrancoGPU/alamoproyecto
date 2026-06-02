package com.alamo.asistencia.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.alamo.asistencia.model.Asistencia;
import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.service.AsistenciaService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

@Controller
public class DescargaReporteController {

    private final AsistenciaService asistenciaService;

    public DescargaReporteController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @GetMapping("/informes/descargar-pdf")
    public void descargarPDF(HttpSession session, HttpServletResponse response) throws IOException {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            response.sendRedirect("/usuarios/cargarLogin");
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Reporte_Asistencia.pdf");

        List<Asistencia> asistencias = asistenciaService.obtenerHistorialPorRangoUsuario(
                usuario,
                java.time.LocalDate.of(1970, 1, 1),
                java.time.LocalDate.now()
        );

        try (OutputStream salida = response.getOutputStream()) {
            PdfWriter writer = new PdfWriter(salida);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Concatenación completa del nombre
            String nombres = usuario.getNombres() != null ? usuario.getNombres() : "";
            String apellido_paterno = usuario.getApellido_paterno() != null ? usuario.getApellido_paterno() : "";
            String apellido_materno = usuario.getApellido_materno() != null ? usuario.getApellido_materno() : "";
            String nombreCompleto = nombres + " " + apellido_paterno + " " + apellido_materno;

            document.add(new Paragraph("Reporte de Asistencia de: " + nombreCompleto));
            document.add(new Paragraph("------------------------------------------------------------"));

            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Asistencia a : asistencias) {
                String fecha = a.getFecha() != null ? a.getFecha().format(df) : "-";
                String entrada = a.getHoraEntrada() != null ? a.getHoraEntrada().toString() : "-";
                String salidaHora = a.getHoraSalida() != null ? a.getHoraSalida().toString() : "-";
                String ubicacion = a.getUbicacion() != null ? a.getUbicacion() : "-";

                String linea = String.format("Fecha: %s | Entrada: %s | Salida: %s | Ubicación: %s",
                        fecha, entrada, salidaHora, ubicacion);

                document.add(new Paragraph(linea));
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}