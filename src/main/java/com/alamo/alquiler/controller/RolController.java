package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.Rol;
import com.alamo.alquiler.repository.RolRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
public class RolController extends AbstractCrudController<Rol, Integer> {

    private final RolRepository repo;

    public RolController(RolRepository repo) {
        this.repo = repo;
    }

    @Override
    protected JpaRepository<Rol, Integer> repo() {
        return repo;
    }
}

