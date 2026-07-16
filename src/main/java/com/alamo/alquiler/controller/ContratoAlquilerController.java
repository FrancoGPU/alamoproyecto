package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.ContratoAlquiler;
import com.alamo.alquiler.model.Notificacion;
import com.alamo.alquiler.repository.ContratoAlquilerRepository;
import com.alamo.alquiler.repository.NotificacionRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contratos-alquiler")
public class ContratoAlquilerController extends AbstractCrudController<ContratoAlquiler, Integer> {

    private final ContratoAlquilerRepository repo;
    private final NotificacionRepository notificacionRepo;

    public ContratoAlquilerController(ContratoAlquilerRepository repo, NotificacionRepository notificacionRepo) {
        this.repo = repo;
        this.notificacionRepo = notificacionRepo;
    }

    @Override
    protected JpaRepository<ContratoAlquiler, Integer> repo() {
        return repo;
    }

    @Override
    public ContratoAlquiler crear(ContratoAlquiler entidad) {
        ContratoAlquiler guardado = super.crear(entidad);
        try {
            Notificacion notif = Notificacion.builder()
                    .titulo("Nuevo Alquiler")
                    .mensaje("Contrato " + guardado.getCodigo() + " registrado con éxito.")
                    .tipo("CONTRATO")
                    .build();
            notificacionRepo.save(notif);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return guardado;
    }

    @Override
    public ContratoAlquiler actualizar(Integer id, ContratoAlquiler entidad) {
        ContratoAlquiler guardado = super.actualizar(id, entidad);
        try {
            Notificacion notif = Notificacion.builder()
                    .titulo("Contrato Actualizado")
                    .mensaje("Contrato " + guardado.getCodigo() + " modificado/actualizado.")
                    .tipo("CONTRATO")
                    .build();
            notificacionRepo.save(notif);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return guardado;
    }

    @Override
    public void eliminar(Integer id) {
        repo.findById(id).ifPresent(c -> {
            c.setEstado("RESCINDIDO");
            repo.save(c);
            try {
                Notificacion notif = Notificacion.builder()
                        .titulo("Contrato Cancelado")
                        .mensaje("Contrato " + c.getCodigo() + " marcado como rescindido/cancelado.")
                        .tipo("CONTRATO")
                        .build();
                notificacionRepo.save(notif);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
