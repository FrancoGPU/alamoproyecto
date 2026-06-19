package com.alamo.alquiler.repository;

import com.alamo.alquiler.model.CategoriaVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaVehiculoRepository extends JpaRepository<CategoriaVehiculo, Integer> {}

