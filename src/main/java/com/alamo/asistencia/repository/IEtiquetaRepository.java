package com.alamo.asistencia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alamo.asistencia.model.Etiqueta;

@Repository
public interface IEtiquetaRepository extends JpaRepository<Etiqueta, Integer> {

    Optional<Etiqueta> findByNombre(String nombre);

    List<Etiqueta> findByEstadoTrueOrderByNombreAsc();
}
