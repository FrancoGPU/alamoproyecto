package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.Informe;
import com.alamo.alquiler.repository.InformeRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/informes")
public class InformeController extends AbstractCrudController<Informe, Integer> {

    private final InformeRepository repo;

    public InformeController(InformeRepository repo) {
        this.repo = repo;
    }

    @Override
    protected JpaRepository<Informe, Integer> repo() {
        return repo;
    }
}

