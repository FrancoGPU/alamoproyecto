package com.alamo.asistencia.controller;

import com.alamo.asistencia.model.Rol;
import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.repository.IRolRepository;
import com.alamo.asistencia.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final IRolRepository rolRepository;

    public UsuarioController(UsuarioService usuarioService, IRolRepository rolRepository) {
        this.usuarioService = usuarioService;
        this.rolRepository = rolRepository;
    }

    // ===================== Utils =====================
    private boolean esAdminORRHH(Usuario u) {
        return u != null && u.getObjRol() != null
                && (u.getObjRol().getId_rol() == 1 || u.getObjRol().getId_rol() == 3);
    }

    private String limpiar(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private boolean dniValido(String dni) {
        if (dni == null) return false;
        return dni.trim().matches("\\d{8}");
    }

    private String normalizarEstado(String estado) {
        if (estado == null) return null;
        String e = estado.trim();
        if (e.equalsIgnoreCase("activo")) return "Activo";
        if (e.equalsIgnoreCase("inactivo")) return "Inactivo";
        return e;
    }

    // ===================== Cargar lista de usuarios =====================
    @GetMapping("/cargarUsuarios")
    public String cargarUsuarios(Model model, HttpSession session) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");

        if (!esAdminORRHH(usuarioLogueado)) {
            return "redirect:/login";
        }

        List<Usuario> usuarios = usuarioService.listarUsuarios();
        List<Rol> roles = rolRepository.findAll();

        model.addAttribute("u", usuarioLogueado);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("roles", roles);

        if (!model.containsAttribute("usuarioForm")) {
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setObjRol(new Rol());
            nuevoUsuario.setEstado("Activo"); // default visible
            model.addAttribute("usuarioForm", nuevoUsuario);
        } else {
            // por si viene por flashAttribute, normaliza estado
            Usuario form = (Usuario) model.getAttribute("usuarioForm");
            if (form != null) form.setEstado(normalizarEstado(form.getEstado()));
        }

        return "usuarios";
    }

    // ===================== Guardar o actualizar usuario (MERGE SEGURO) =====================
    @PostMapping("/guardarUsuario")
    public String guardarUsuario(@ModelAttribute("usuarioForm") Usuario form,
                                 HttpSession session,
                                 RedirectAttributes ra) {

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (!esAdminORRHH(usuarioLogueado)) return "redirect:/login";

        try {
            // Limpieza básica (convierte "" en null para no romper unique y saneamiento)
            form.setNombres(limpiar(form.getNombres()));
            form.setApellido_paterno(limpiar(form.getApellido_paterno()));
            form.setApellido_materno(limpiar(form.getApellido_materno()));
            form.setDni(limpiar(form.getDni()));
            form.setCorreo(limpiar(form.getCorreo()));
            form.setTelefono(limpiar(form.getTelefono()));
            form.setDireccion(limpiar(form.getDireccion()));
            form.setCargo(limpiar(form.getCargo()));
            form.setBanco(limpiar(form.getBanco()));
            form.setCuenta_bancaria(limpiar(form.getCuenta_bancaria()));
            form.setCuenta_interbancaria(limpiar(form.getCuenta_interbancaria()));
            form.setEstado(normalizarEstado(form.getEstado()));

            // Validaciones mínimas
            if (form.getNombres() == null
                    || form.getApellido_paterno() == null
                    || form.getApellido_materno() == null
                    || !dniValido(form.getDni())) {

                ra.addFlashAttribute("error",
                        "Los campos Nombre(s), Apellidos (Paterno y Materno) y DNI (8 dígitos) son obligatorios.");
                ra.addFlashAttribute("usuarioForm", form);
                return "redirect:/cargarUsuarios";
            }

            Integer idRol = (form.getObjRol() != null) ? form.getObjRol().getId_rol() : null;
            if (idRol == null || idRol == 0) {
                ra.addFlashAttribute("error", "Debe seleccionar un Rol.");
                ra.addFlashAttribute("usuarioForm", form);
                return "redirect:/cargarUsuarios";
            }

            Rol rol = rolRepository.findById(idRol)
                    .orElseThrow(() -> new IllegalArgumentException("Rol inválido."));

            // ====== NUEVO ======
            if (form.getIdUsuario() == null) {
                form.setObjRol(rol);

                // estado por defecto
                if (form.getEstado() == null) form.setEstado("Activo");

                // contraseña por defecto = DNI
                if (form.getContrasenia() == null || form.getContrasenia().isBlank()) {
                    form.setContrasenia(form.getDni());
                }

                usuarioService.guardarUsuario(form);
                ra.addFlashAttribute("success", "Usuario creado con éxito. Contraseña por defecto: DNI.");
                return "redirect:/cargarUsuarios";
            }

            // ====== EDICIÓN (MERGE REAL) ======
            Usuario db = usuarioService.obtenerUsuario(form.getIdUsuario())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

            // Actualiza SOLO lo que el form edita
            db.setNombres(form.getNombres());
            db.setApellido_paterno(form.getApellido_paterno());
            db.setApellido_materno(form.getApellido_materno());
            db.setDni(form.getDni());

            db.setCorreo(form.getCorreo());
            db.setTelefono(form.getTelefono());
            db.setDireccion(form.getDireccion());
            db.setCargo(form.getCargo());

            db.setBanco(form.getBanco());
            db.setCuenta_bancaria(form.getCuenta_bancaria());
            db.setCuenta_interbancaria(form.getCuenta_interbancaria());

            db.setObjRol(rol);

            // ✅ CLAVE: si el usuario eligió estado, lo seteas sí o sí
            // (y si viene null por cualquier razón, mantienes el de db)
            if (form.getEstado() != null) {
                db.setEstado(form.getEstado());
            }

            // NO tocar contraseña en edición
            usuarioService.guardarUsuario(db);

            ra.addFlashAttribute("success", "Usuario actualizado correctamente.");
            return "redirect:/cargarUsuarios";

        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            ra.addFlashAttribute("usuarioForm", form);
            return "redirect:/cargarUsuarios";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al guardar: Ya existe un DNI o correo registrado (o datos inválidos).");
            ra.addFlashAttribute("usuarioForm", form);
            return "redirect:/cargarUsuarios";
        }
    }

    // ===================== Editar usuario =====================
    @GetMapping("/editarUsuario/{id}")
    public String editarUsuario(@PathVariable("id") Integer id,
                                HttpSession session,
                                RedirectAttributes ra) {

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (!esAdminORRHH(usuarioLogueado)) return "redirect:/login";

        Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuario(id);

        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            u.setEstado(normalizarEstado(u.getEstado()));
            ra.addFlashAttribute("usuarioForm", u);
        } else {
            ra.addFlashAttribute("error", "Usuario no encontrado.");
        }
        return "redirect:/cargarUsuarios";
    }

    // =====================
    // “Eliminar” => Desactivar (NO BORRAR)
    // =====================
    @GetMapping("/eliminarUsuario/{id}")
    public String desactivarUsuario(@PathVariable("id") Integer id,
                                    HttpSession session,
                                    RedirectAttributes ra) {

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (!esAdminORRHH(usuarioLogueado)) return "redirect:/login";

        try {
            // ✅ ahora sí desactiva (set estado = Inactivo)
            usuarioService.desactivarUsuario(id);

            ra.addFlashAttribute("success", "Usuario desactivado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error",
                    "No se pudo desactivar: El usuario está asociado a asistencias/asignaciones (o hubo un error).");
        }
        return "redirect:/cargarUsuarios";
    }
}
