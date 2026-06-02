package com.alamo.asistencia.controller;

import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.model.UsuarioCertificacion;
import com.alamo.asistencia.model.UsuarioExperiencia;
import com.alamo.asistencia.model.UsuarioIdioma;
import com.alamo.asistencia.repository.UsuarioCertificacionRepository;
import com.alamo.asistencia.repository.UsuarioExperienciaRepository;
import com.alamo.asistencia.repository.UsuarioFormacionRepository;
import com.alamo.asistencia.repository.UsuarioIdiomaRepository;
import com.alamo.asistencia.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioFormacionRepository formacionRepo;
    @Autowired private UsuarioCertificacionRepository certRepo;
    @Autowired private UsuarioIdiomaRepository idiomaRepo;
    @Autowired private UsuarioExperienciaRepository experienciaRepo;

    private Integer getCurrentUserId(HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        return (user != null) ? user.getIdUsuario() : null;
    }

    @GetMapping
    public String verPerfil(Model model, HttpSession session) {
        Integer userId = getCurrentUserId(session);
        if (userId == null) return "redirect:/usuarios/cargarLogin";

        Usuario usuario = usuarioService.obtenerUsuario(userId).orElse(null);
        if (usuario == null) return "redirect:/usuarios/cargarLogin";

        if (usuario.getFormaciones() != null) {
            usuario.getFormaciones().clear();
            usuario.getFormaciones().addAll(formacionRepo.findByUsuario_IdUsuario(userId));
        }

        if (usuario.getCertificacionesList() != null) {
            usuario.getCertificacionesList().clear();
            usuario.getCertificacionesList().addAll(certRepo.findByUsuario_IdUsuario(userId));
        }

        if (usuario.getIdiomasList() != null) {
            usuario.getIdiomasList().clear();
            usuario.getIdiomasList().addAll(idiomaRepo.findByUsuario_IdUsuario(userId));
        }

        if (usuario.getExperienciasList() != null) {
            usuario.getExperienciasList().clear();
            usuario.getExperienciasList().addAll(experienciaRepo.findByUsuario_IdUsuario(userId));
        }

        model.addAttribute("u", usuario);
        model.addAttribute("usuario", usuario);

        return "perfil";
    }

    @PostMapping("/actualizar-todo")
    public String actualizarTodo(
            @ModelAttribute("u") Usuario uForm,
            @RequestParam(value = "idiomasFiles", required = false) MultipartFile[] idiomasFiles,
            @RequestParam(value = "certificacionesFiles", required = false) MultipartFile[] certificacionesFiles,
            @RequestParam(value = "experienciasFiles", required = false) MultipartFile[] experienciasFiles,
            @RequestParam(value = "certijovenFile", required = false) MultipartFile certijovenFile,
            @RequestParam(value = "certiadultoFile", required = false) MultipartFile certiadultoFile,
            @RequestParam(value = "antecedentesFile", required = false) MultipartFile antecedentesFile,
            RedirectAttributes ra,
            HttpSession session
    ) {
        Integer userId = getCurrentUserId(session);

        if (userId == null) {
            ra.addFlashAttribute("error", "Sesión expirada. Por favor, inicie sesión.");
            return "redirect:/usuarios/cargarLogin";
        }

        if (uForm.getIdUsuario() == null || !uForm.getIdUsuario().equals(userId)) {
            ra.addFlashAttribute("error", "No autorizado para actualizar este perfil.");
            return "redirect:/perfil";
        }

        try {
            System.out.println("== PERFIL POST ==");
            System.out.println("idUsuario=" + uForm.getIdUsuario());
            System.out.println("idiomasFiles=" + (idiomasFiles != null ? idiomasFiles.length : 0));
            System.out.println("certificacionesFiles=" + (certificacionesFiles != null ? certificacionesFiles.length : 0));
            System.out.println("experienciasFiles=" + (experienciasFiles != null ? experienciasFiles.length : 0));
            System.out.println("certijovenFile=" + (certijovenFile != null && !certijovenFile.isEmpty()));
            System.out.println("certiadultoFile=" + (certiadultoFile != null && !certiadultoFile.isEmpty()));
            System.out.println("antecedentesFile=" + (antecedentesFile != null && !antecedentesFile.isEmpty()));
            System.out.println("tieneAntecedentes=" + uForm.getTiene_antecedentes());

            usuarioService.actualizarPerfilTodo(
                    userId,
                    uForm,
                    idiomasFiles,
                    certificacionesFiles,
                    experienciasFiles,
                    certijovenFile,
                    certiadultoFile,
                    antecedentesFile
            );

            usuarioService.obtenerUsuario(userId)
                    .ifPresent(uDb -> session.setAttribute("usuarioLogueado", uDb));

            ra.addFlashAttribute("success", "Perfil actualizado correctamente.");
            return "redirect:/perfil";

        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
            return "redirect:/perfil";
        }
    }

    @PostMapping("/subir-foto")
    public String handleProfilePhotoUpload(
            @RequestParam("file") MultipartFile archivoSubido,
            RedirectAttributes ra,
            HttpSession session
    ) {
        Integer currentUserId = getCurrentUserId(session);

        if (currentUserId == null) {
            ra.addFlashAttribute("error", "Sesión expirada. Por favor, inicie sesión.");
            return "redirect:/usuarios/cargarLogin";
        }

        if (usuarioService.updateProfilePhoto(currentUserId, archivoSubido)) {
            usuarioService.obtenerUsuario(currentUserId)
                    .ifPresent(u -> session.setAttribute("usuarioLogueado", u));
            ra.addFlashAttribute("success", "Foto de perfil actualizada correctamente.");
        } else {
            ra.addFlashAttribute("error", "Error al actualizar la foto. Selecciona JPG/PNG válido.");
        }

        return "redirect:/perfil";
    }

    @GetMapping("/foto/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveProfilePhoto(@PathVariable("filename") String nombreArchivo) {
        try {
            Path filePath = usuarioService.loadProfilePhotoPath(nombreArchivo);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/idioma/archivo/{idIdioma}")
    @ResponseBody
    public ResponseEntity<Resource> verArchivoIdioma(
            @PathVariable("idIdioma") Integer idIdioma,
            HttpSession session
    ) {
        Integer currentUserId = getCurrentUserId(session);
        if (currentUserId == null) return ResponseEntity.status(401).build();

        try {
            Optional<UsuarioIdioma> idiomaOpt = usuarioService.obtenerIdiomaPorId(idIdioma);
            if (idiomaOpt.isEmpty()) return ResponseEntity.notFound().build();

            UsuarioIdioma idioma = idiomaOpt.get();

            if (idioma.getUsuario() == null || idioma.getUsuario().getIdUsuario() == null) {
                return ResponseEntity.notFound().build();
            }

            if (!idioma.getUsuario().getIdUsuario().equals(currentUserId)) {
                return ResponseEntity.status(403).build();
            }

            if (idioma.getArchivoRuta() == null || idioma.getArchivoRuta().isBlank()) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = usuarioService.loadIdiomaFilePath(idioma.getArchivoRuta());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = (idioma.getArchivoTipo() != null && !idioma.getArchivoTipo().isBlank())
                        ? idioma.getArchivoTipo()
                        : "application/octet-stream";
            }

            String nombreDescarga = (idioma.getArchivoNombre() != null && !idioma.getArchivoNombre().isBlank())
                    ? idioma.getArchivoNombre().replace("\"", "")
                    : resource.getFilename();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreDescarga + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/certificacion/archivo/{idCertificacion}")
    @ResponseBody
    public ResponseEntity<Resource> verArchivoCertificacion(
            @PathVariable("idCertificacion") Integer idCertificacion,
            HttpSession session
    ) {
        Integer currentUserId = getCurrentUserId(session);
        if (currentUserId == null) return ResponseEntity.status(401).build();

        try {
            Optional<UsuarioCertificacion> certOpt = usuarioService.obtenerCertificacionPorId(idCertificacion);
            if (certOpt.isEmpty()) return ResponseEntity.notFound().build();

            UsuarioCertificacion cert = certOpt.get();

            if (cert.getUsuario() == null || cert.getUsuario().getIdUsuario() == null) {
                return ResponseEntity.notFound().build();
            }

            if (!cert.getUsuario().getIdUsuario().equals(currentUserId)) {
                return ResponseEntity.status(403).build();
            }

            if (cert.getArchivoRuta() == null || cert.getArchivoRuta().isBlank()) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = usuarioService.loadCertificacionFilePath(cert.getArchivoRuta());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = (cert.getArchivoTipo() != null && !cert.getArchivoTipo().isBlank())
                        ? cert.getArchivoTipo()
                        : "application/octet-stream";
            }

            String nombreDescarga = (cert.getArchivoNombre() != null && !cert.getArchivoNombre().isBlank())
                    ? cert.getArchivoNombre().replace("\"", "")
                    : resource.getFilename();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreDescarga + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/experiencia/archivo/{idExperiencia}")
    @ResponseBody
    public ResponseEntity<Resource> verArchivoExperiencia(
            @PathVariable("idExperiencia") Integer idExperiencia,
            HttpSession session
    ) {
        Integer currentUserId = getCurrentUserId(session);
        if (currentUserId == null) return ResponseEntity.status(401).build();

        try {
            Optional<UsuarioExperiencia> expOpt = usuarioService.obtenerExperienciaPorId(idExperiencia);
            if (expOpt.isEmpty()) return ResponseEntity.notFound().build();

            UsuarioExperiencia exp = expOpt.get();

            if (exp.getUsuario() == null || exp.getUsuario().getIdUsuario() == null) {
                return ResponseEntity.notFound().build();
            }

            if (!exp.getUsuario().getIdUsuario().equals(currentUserId)) {
                return ResponseEntity.status(403).build();
            }

            if (exp.getArchivoRuta() == null || exp.getArchivoRuta().isBlank()) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = usuarioService.loadExperienciaFilePath(exp.getArchivoRuta());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = (exp.getArchivoTipo() != null && !exp.getArchivoTipo().isBlank())
                        ? exp.getArchivoTipo()
                        : "application/octet-stream";
            }

            String nombreDescarga = (exp.getArchivoNombre() != null && !exp.getArchivoNombre().isBlank())
                    ? exp.getArchivoNombre().replace("\"", "")
                    : resource.getFilename();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreDescarga + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/certijoven/archivo")
    @ResponseBody
    public ResponseEntity<Resource> verArchivoCertijoven(HttpSession session) {
        Integer currentUserId = getCurrentUserId(session);
        if (currentUserId == null) return ResponseEntity.status(401).build();

        try {
            Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuario(currentUserId);
            if (usuarioOpt.isEmpty()) return ResponseEntity.notFound().build();

            Usuario usuario = usuarioOpt.get();

            if (usuario.getRuta_certijoven() == null || usuario.getRuta_certijoven().isBlank()) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = usuarioService.loadCertijovenFilePath(usuario.getRuta_certijoven());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";

            String nombreDescarga = (usuario.getCertijoven_nombre() != null && !usuario.getCertijoven_nombre().isBlank())
                    ? usuario.getCertijoven_nombre().replace("\"", "")
                    : resource.getFilename();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreDescarga + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/certiadulto/archivo")
    @ResponseBody
    public ResponseEntity<Resource> verArchivoCertiadulto(HttpSession session) {
        Integer currentUserId = getCurrentUserId(session);
        if (currentUserId == null) return ResponseEntity.status(401).build();

        try {
            Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuario(currentUserId);
            if (usuarioOpt.isEmpty()) return ResponseEntity.notFound().build();

            Usuario usuario = usuarioOpt.get();

            if (usuario.getRuta_certiadulto() == null || usuario.getRuta_certiadulto().isBlank()) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = usuarioService.loadCertiadultoFilePath(usuario.getRuta_certiadulto());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";

            String nombreDescarga = (usuario.getCertiadulto_nombre() != null && !usuario.getCertiadulto_nombre().isBlank())
                    ? usuario.getCertiadulto_nombre().replace("\"", "")
                    : resource.getFilename();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreDescarga + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/antecedentes/archivo")
    @ResponseBody
    public ResponseEntity<Resource> verArchivoAntecedentes(HttpSession session) {
        Integer currentUserId = getCurrentUserId(session);
        if (currentUserId == null) return ResponseEntity.status(401).build();

        try {
            Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuario(currentUserId);
            if (usuarioOpt.isEmpty()) return ResponseEntity.notFound().build();

            Usuario usuario = usuarioOpt.get();

            if (usuario.getRuta_antecedentes() == null || usuario.getRuta_antecedentes().isBlank()) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = usuarioService.loadAntecedentesFilePath(usuario.getRuta_antecedentes());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";

            String nombreDescarga = (usuario.getAntecedentes_nombre() != null && !usuario.getAntecedentes_nombre().isBlank())
                    ? usuario.getAntecedentes_nombre().replace("\"", "")
                    : resource.getFilename();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreDescarga + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}