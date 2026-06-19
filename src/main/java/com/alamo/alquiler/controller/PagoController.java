package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.Pago;
import com.alamo.alquiler.repository.PagoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagos")
public class PagoController extends AbstractCrudController<Pago, Integer> {

    private final PagoRepository repo;

    public PagoController(PagoRepository repo) {
        this.repo = repo;
    }

    @Override
    protected JpaRepository<Pago, Integer> repo() {
        return repo;
    }
}

