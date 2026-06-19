package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.Vehiculo;
import com.alamo.alquiler.repository.VehiculoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController extends AbstractCrudController<Vehiculo, Integer> {

    private final VehiculoRepository repo;

    public VehiculoController(VehiculoRepository repo) {
        this.repo = repo;
    }

    @Override
    protected JpaRepository<Vehiculo, Integer> repo() {
        return repo;
    }
}

