package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.Seguro;
import com.alamo.alquiler.repository.SeguroRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seguros")
public class SeguroController extends AbstractCrudController<Seguro, Integer> {

    private final SeguroRepository repo;

    public SeguroController(SeguroRepository repo) {
        this.repo = repo;
    }

    @Override
    protected JpaRepository<Seguro, Integer> repo() {
        return repo;
    }
}

