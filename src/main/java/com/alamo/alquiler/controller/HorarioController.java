package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.Horario;
import com.alamo.alquiler.repository.HorarioRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/horarios")
public class HorarioController extends AbstractCrudController<Horario, Integer> {

    private final HorarioRepository repo;

    public HorarioController(HorarioRepository repo) {
        this.repo = repo;
    }

    @Override
    protected JpaRepository<Horario, Integer> repo() {
        return repo;
    }
}

