package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.InspeccionVehiculo;
import com.alamo.alquiler.repository.InspeccionVehiculoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inspecciones-vehiculo")
public class InspeccionVehiculoController extends AbstractCrudController<InspeccionVehiculo, Integer> {

    private final InspeccionVehiculoRepository repo;

    public InspeccionVehiculoController(InspeccionVehiculoRepository repo) {
        this.repo = repo;
    }

    @Override
    protected JpaRepository<InspeccionVehiculo, Integer> repo() {
        return repo;
    }
}

