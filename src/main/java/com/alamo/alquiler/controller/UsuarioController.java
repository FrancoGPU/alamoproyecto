package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.Usuario;
import com.alamo.alquiler.repository.UsuarioRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController extends AbstractCrudController<Usuario, Integer> {

    private final UsuarioRepository repo;

    public UsuarioController(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Override
    protected JpaRepository<Usuario, Integer> repo() {
        return repo;
    }
}

