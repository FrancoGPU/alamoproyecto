package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.ContratoSeguro;
import com.alamo.alquiler.repository.ContratoSeguroRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contratos-seguro")
public class ContratoSeguroController extends AbstractCrudController<ContratoSeguro, Integer> {

    private final ContratoSeguroRepository repo;

    public ContratoSeguroController(ContratoSeguroRepository repo) {
        this.repo = repo;
    }

    @Override
    protected JpaRepository<ContratoSeguro, Integer> repo() {
        return repo;
    }
}

