package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.Servicio;
import com.alamo.alquiler.repository.ServicioRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController extends AbstractCrudController<Servicio, Integer> {

    private final ServicioRepository repo;

    public ServicioController(ServicioRepository repo) {
        this.repo = repo;
    }

    @Override
    protected JpaRepository<Servicio, Integer> repo() {
        return repo;
    }
}

