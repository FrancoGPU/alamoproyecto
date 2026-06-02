package com.alamo.asistencia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alamo.asistencia.model.Seguro;

public interface ISeguroRepository extends JpaRepository<Seguro, Integer> {
    List<Seguro> findByActivoTrueOrderByNombreAsc();
}
