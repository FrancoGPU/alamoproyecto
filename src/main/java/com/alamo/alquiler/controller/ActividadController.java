package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.Actividad;
import com.alamo.alquiler.repository.ActividadRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/actividades")
public class ActividadController extends AbstractCrudController<Actividad, Integer> {

    private final ActividadRepository repo;

    public ActividadController(ActividadRepository repo) {
        this.repo = repo;
    }

    @Override
    protected JpaRepository<Actividad, Integer> repo() {
        return repo;
    }
}

