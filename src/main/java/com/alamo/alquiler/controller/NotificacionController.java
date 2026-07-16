package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.Notificacion;
import com.alamo.alquiler.repository.NotificacionRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController extends AbstractCrudController<Notificacion, Integer> {

    private final NotificacionRepository repo;

    public NotificacionController(NotificacionRepository repo) {
        this.repo = repo;
    }

    @Override
    protected JpaRepository<Notificacion, Integer> repo() {
        return repo;
    }

    @GetMapping("/recientes")
    public List<Notificacion> obtenerRecientes() {
        return repo.findFirst10ByOrderByFechaCreacionDesc();
    }

    @PutMapping("/{id}/leer")
    public ResponseEntity<Notificacion> marcarComoLeida(@PathVariable Integer id) {
        return repo.findById(id)
                .map(n -> {
                    n.setLeido(true);
                    return ResponseEntity.ok(repo.save(n));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/leer-todas")
    public ResponseEntity<Void> marcarTodasComoLeidas() {
        List<Notificacion> lista = repo.findAll();
        for (Notificacion n : lista) {
            if (!n.isLeido()) {
                n.setLeido(true);
            }
        }
        repo.saveAll(lista);
        return ResponseEntity.ok().build();
    }
}
