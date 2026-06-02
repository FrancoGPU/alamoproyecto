package com.alamo.asistencia.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.alamo.asistencia.model.PermisoExtra;
import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.repository.IPermisoExtraRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/permisos-extras")
public class PermisoExtraController {

    @Autowired
    private IPermisoExtraRepository permisoRepo;

    private final ZoneId zonaLima = ZoneId.of("America/Lima");

    /**
     * API: Obtener Historial de Permisos Extras del mes actual
     * Resuelve el error 404 al abrir el historial
     */
    @GetMapping("/historial")
    public ResponseEntity<?> obtenerHistorial() {
        try {
            LocalDate hoy = LocalDate.now(zonaLima);
            // Buscamos los registros del mes y año actual
            List<PermisoExtra> historial = permisoRepo.findByMesAnio(hoy.getMonthValue(), hoy.getYear());
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API: Gestionar Aprobación o Rechazo
     */
    @PostMapping("/gestionar/{id}")
    public ResponseEntity<?> gestionarSolicitud(
            @PathVariable(name = "id") Integer id,
            @RequestParam(name = "nuevoEstado") String nuevoEstado,
            HttpSession session) {
        
        Usuario admin = (Usuario) session.getAttribute("usuarioLogueado");
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sesión no válida o expirada.");
        }

        return permisoRepo.findById(id).map(permiso -> {
            // Regla de negocio: No rechazar si ya se usó para re-ingresar
            if (permiso.isUsado() && "RECHAZADO".equals(nuevoEstado)) {
                return ResponseEntity.badRequest().body("No se puede rechazar un permiso ya utilizado.");
            }

            permiso.setEstado(nuevoEstado);
            permiso.setAdminAprobador(admin);
            permiso.setFechaAprobacion(LocalDateTime.now(zonaLima));
            
            permisoRepo.save(permiso);
            
            return ResponseEntity.ok("Solicitud " + nuevoEstado + " correctamente.");
            
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("La solicitud no existe."));
    }
}