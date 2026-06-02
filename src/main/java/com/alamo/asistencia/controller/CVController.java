package com.alamo.asistencia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.repository.IUsuarioRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/cv")
public class CVController {

    @Autowired
    private IUsuarioRepository usuarioRepository;

    // Directorios de carga
    private static final String BASE_UPLOAD_DIR = "/root/Asistencia/uploads/";
    private static final String CV_UPLOAD_DIR = BASE_UPLOAD_DIR + "CV/";
    private static final String ID_UPLOAD_DIR = BASE_UPLOAD_DIR + "ID/";

    // ==============================================
    // 1. LÓGICA DE SUBIDA CV
    // ==============================================

    @PostMapping("/subir")
    @Transactional
    public String subirCV(@RequestParam("archivo") MultipartFile archivo, 
                         HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/usuarios/cargarLogin";
        
        Usuario usuarioActualizado = usuarioRepository.findById(usuario.getIdUsuario()).orElse(usuario);
        model.addAttribute("u", usuarioActualizado); 

        if (!validarArchivoPDF(archivo)) {
            model.addAttribute("error", "Solo se permiten archivos PDF");
            return "perfil";
        }

        try {
            Path dirPath = Paths.get(CV_UPLOAD_DIR).toAbsolutePath().normalize();
            Files.createDirectories(dirPath);

            String nombreArchivo = "CV_" + usuario.getDni() + "_" + usuario.getIdUsuario() + ".pdf";
            Path rutaArchivoFinal = dirPath.resolve(nombreArchivo).normalize();

            Files.copy(archivo.getInputStream(), rutaArchivoFinal, StandardCopyOption.REPLACE_EXISTING);

            usuarioRepository.actualizarRutaCV(usuario.getIdUsuario(), nombreArchivo); 
            
            usuarioActualizado.setRutaCV(nombreArchivo);
            session.setAttribute("usuarioLogueado", usuarioActualizado);

            return "redirect:/cargarPerfil?success=CV actualizado correctamente.";

        } catch (IOException e) {
            model.addAttribute("error", "Error al subir el archivo: " + e.getMessage());
            return "perfil";
        }
    }

    // --- ENDPOINTS DE DESCARGA/VISTA CON PARÁMETRO 'download' ---

    @GetMapping("/descargar/{idUsuario}")
    public void descargarCV(@PathVariable("idUsuario") Integer idUsuario, 
                            @RequestParam(value = "download", required = false) Boolean download,
                            HttpSession session, HttpServletResponse response) throws IOException {
        manejarDescargaDocumento(idUsuario, session, response, CV_UPLOAD_DIR, "rutaCV", "application/pdf", download);
    }

    // ==============================================
    // 2. FOTO ID FRONTAL
    // ==============================================

    @PostMapping("/foto-id-frontal/subir")
    @Transactional
    public String subirFotoIDFrontal(@RequestParam("archivo") MultipartFile archivo, 
                                     HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/usuarios/cargarLogin";
        
        Usuario usuarioActualizado = usuarioRepository.findById(usuario.getIdUsuario()).orElse(usuario);
        model.addAttribute("u", usuarioActualizado); 

        if (!validarArchivoImagen(archivo)) {
            model.addAttribute("error", "Solo se permiten imágenes JPG o PNG para el DNI Frontal.");
            return "perfil";
        }

        try {
            Path dirPath = Paths.get(ID_UPLOAD_DIR).toAbsolutePath().normalize();
            Files.createDirectories(dirPath);

            String extension = obtenerExtension(archivo);
            String nombreArchivo = "ID_FRONTAL_" + usuario.getDni() + "_" + usuario.getIdUsuario() + "." + extension;
            Path rutaArchivoFinal = dirPath.resolve(nombreArchivo).normalize();

            Files.copy(archivo.getInputStream(), rutaArchivoFinal, StandardCopyOption.REPLACE_EXISTING);

            usuarioRepository.actualizarRutaFotoID(usuario.getIdUsuario(), nombreArchivo, archivo.getOriginalFilename());
            
            usuarioActualizado.setRuta_foto_id(nombreArchivo);
            usuarioActualizado.setFoto_id_nombre(archivo.getOriginalFilename());
            session.setAttribute("usuarioLogueado", usuarioActualizado);

            return "redirect:/cargarPerfil?success=Foto ID Frontal actualizada correctamente.";
        } catch (IOException e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            return "perfil";
        }
    }
    
    @GetMapping("/foto-id-frontal/descargar/{idUsuario}")
    public void descargarFotoIDFrontal(@PathVariable("idUsuario") Integer idUsuario, 
                                       @RequestParam(value = "download", required = false) Boolean download,
                                       HttpSession session, HttpServletResponse response) throws IOException {
        manejarDescargaDocumento(idUsuario, session, response, ID_UPLOAD_DIR, "ruta_foto_id", "image/jpeg", download);
    }

    // ==============================================
    // 3. FOTO ID TRASERA
    // ==============================================

    @PostMapping("/foto-id-trasera/subir")
    @Transactional
    public String subirFotoIDTrasera(@RequestParam("archivo") MultipartFile archivo, 
                                     HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/usuarios/cargarLogin";
        
        Usuario usuarioActualizado = usuarioRepository.findById(usuario.getIdUsuario()).orElse(usuario);
        model.addAttribute("u", usuarioActualizado); 

        if (!validarArchivoImagen(archivo)) {
            model.addAttribute("error", "Solo se permiten imágenes JPG o PNG para el DNI Trasero.");
            return "perfil";
        }

        try {
            Path dirPath = Paths.get(ID_UPLOAD_DIR).toAbsolutePath().normalize();
            Files.createDirectories(dirPath);

            String extension = obtenerExtension(archivo);
            String nombreArchivo = "ID_TRASERA_" + usuario.getDni() + "_" + usuario.getIdUsuario() + "." + extension;
            Path rutaArchivoFinal = dirPath.resolve(nombreArchivo).normalize();

            Files.copy(archivo.getInputStream(), rutaArchivoFinal, StandardCopyOption.REPLACE_EXISTING);

            usuarioRepository.actualizarRutaFotoIDTrasera(usuario.getIdUsuario(), nombreArchivo, archivo.getOriginalFilename());
            
            usuarioActualizado.setRuta_foto_id_trasera(nombreArchivo);
            usuarioActualizado.setFoto_id_nombre_trasera(archivo.getOriginalFilename());
            session.setAttribute("usuarioLogueado", usuarioActualizado);

            return "redirect:/cargarPerfil?success=Foto ID Trasera actualizada correctamente.";
        } catch (IOException e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            return "perfil";
        }
    }
    
    @GetMapping("/foto-id-trasera/descargar/{idUsuario}")
    public void descargarFotoIDTrasera(@PathVariable("idUsuario") Integer idUsuario, 
                                       @RequestParam(value = "download", required = false) Boolean download,
                                       HttpSession session, HttpServletResponse response) throws IOException {
        manejarDescargaDocumento(idUsuario, session, response, ID_UPLOAD_DIR, "ruta_foto_id_trasera", "image/jpeg", download);
    }

    // ==============================================
    // LÓGICA COMÚN: MANEJA "INLINE" Y "ATTACHMENT"
    // ==============================================
    
    private void manejarDescargaDocumento(Integer idUsuario, HttpSession session, HttpServletResponse response,
                                          String uploadDir, String rutaCampo, String contentTypePorDefecto,
                                          Boolean download) throws IOException {

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null) return;

        // 1. Lógica de Seguridad
        int rolUsuario = usuarioLogueado.getObjRol() != null ? usuarioLogueado.getObjRol().getId_rol() : 0;
        boolean autorizado = (rolUsuario == 1 || rolUsuario == 3 || usuarioLogueado.getIdUsuario().equals(idUsuario));
        
        if (!autorizado) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Usuario usuarioDoc = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuarioDoc == null) return;

        String nombreArchivoEnDb = null;
        String tipoDoc = "";
        if ("rutaCV".equals(rutaCampo)) { nombreArchivoEnDb = usuarioDoc.getRutaCV(); tipoDoc = "CV"; }
        else if ("ruta_foto_id".equals(rutaCampo)) { nombreArchivoEnDb = usuarioDoc.getRuta_foto_id(); tipoDoc = "DNI_FRONTAL"; }
        else if ("ruta_foto_id_trasera".equals(rutaCampo)) { nombreArchivoEnDb = usuarioDoc.getRuta_foto_id_trasera(); tipoDoc = "DNI_TRASERA"; }
        
        if (nombreArchivoEnDb == null || nombreArchivoEnDb.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Path rutaArchivo = Paths.get(uploadDir, nombreArchivoEnDb).toAbsolutePath().normalize();

        if (Files.exists(rutaArchivo)) {
            byte[] contenido = Files.readAllBytes(rutaArchivo);
            String extension = obtenerExtensionDesdeNombre(nombreArchivoEnDb);
            String finalContentType = determinarContentType(extension, contentTypePorDefecto);
            
            response.setContentType(finalContentType);
            
            // ✅ LÓGICA CORREGIDA:
            if (download != null && download) {
                // Si el parámetro download=true viene en la URL, se descarga el archivo
                String nombreDescarga = tipoDoc + "_" + usuarioDoc.getDni() + "." + extension;
                response.setHeader("Content-Disposition", "attachment; filename=\"" + nombreDescarga + "\"");
            } else {
                // Si no hay parámetro, se muestra en el navegador (para el Modal)
                response.setHeader("Content-Disposition", "inline; filename=\"" + nombreArchivoEnDb + "\"");
            }
            
            response.setContentLength(contenido.length);
            response.getOutputStream().write(contenido);
            response.getOutputStream().flush();
        } else {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // ==============================================
    // UTILS
    // ==============================================

    private boolean validarArchivoPDF(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) return false;
        String contentType = archivo.getContentType();
        return contentType != null && contentType.equals("application/pdf");
    }
    
    private boolean validarArchivoImagen(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) return false;
        String contentType = archivo.getContentType();
        return contentType != null && (contentType.startsWith("image/"));
    }
    
    private String obtenerExtension(MultipartFile archivo) {
        String nombre = archivo.getOriginalFilename();
        if (nombre == null) return "";
        int puntoIndex = nombre.lastIndexOf('.');
        return (puntoIndex > 0) ? nombre.substring(puntoIndex + 1).toLowerCase() : "";
    }
    
    private String obtenerExtensionDesdeNombre(String nombreArchivo) {
        int puntoIndex = nombreArchivo.lastIndexOf('.');
        return (puntoIndex > 0) ? nombreArchivo.substring(puntoIndex + 1).toLowerCase() : "";
    }
    
    private String determinarContentType(String extension, String contentTypePorDefecto) {
        if ("pdf".equals(extension)) return "application/pdf";
        if ("png".equals(extension)) return "image/png";
        if ("jpg".equals(extension) || "jpeg".equals(extension)) return "image/jpeg";
        return contentTypePorDefecto;
    }
}