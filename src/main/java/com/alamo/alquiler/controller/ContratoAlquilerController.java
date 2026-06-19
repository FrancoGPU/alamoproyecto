package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.ContratoAlquiler;
import com.alamo.alquiler.repository.ContratoAlquilerRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contratos-alquiler")
public class ContratoAlquilerController extends AbstractCrudController<ContratoAlquiler, Integer> {

    private final ContratoAlquilerRepository repo;

    public ContratoAlquilerController(ContratoAlquilerRepository repo) {
        this.repo = repo;
    }

    @Override
    protected JpaRepository<ContratoAlquiler, Integer> repo() {
        return repo;
    }
}

