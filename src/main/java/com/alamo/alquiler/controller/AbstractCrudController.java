package com.alamo.alquiler.controller;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public abstract class AbstractCrudController<T, ID> {

    protected abstract JpaRepository<T, ID> repo();

    @GetMapping
    public List<T> listar() {
        return repo().findAll();
    }

    @GetMapping("/{id}")
    public T obtener(@PathVariable ID id) {
        return repo().findById(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public T crear(@RequestBody T entidad) {
        return repo().save(entidad);
    }

    @PutMapping("/{id}")
    public T actualizar(@PathVariable ID id, @RequestBody T entidad) {
        if (!repo().existsById(id)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return repo().save(entidad);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable ID id) {
        repo().deleteById(id);
    }
}

