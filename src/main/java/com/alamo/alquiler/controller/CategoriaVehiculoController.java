package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.CategoriaVehiculo;
import com.alamo.alquiler.repository.CategoriaVehiculoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias-vehiculo")
public class CategoriaVehiculoController extends AbstractCrudController<CategoriaVehiculo, Integer> {

    private final CategoriaVehiculoRepository repo;

    public CategoriaVehiculoController(CategoriaVehiculoRepository repo) {
        this.repo = repo;
    }

    @Override
    protected JpaRepository<CategoriaVehiculo, Integer> repo() {
        return repo;
    }
}

