package com.alamo.asistencia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alamo.asistencia.model.CategoriaVehiculo;
import com.alamo.asistencia.model.TipoCategoriaVehiculo;

public interface ICategoriaVehiculoRepository extends JpaRepository<CategoriaVehiculo, Integer> {
    Optional<CategoriaVehiculo> findByTipo(TipoCategoriaVehiculo tipo);
}
