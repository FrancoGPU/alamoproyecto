package com.alamo.alquiler.repository;

import com.alamo.alquiler.model.InspeccionVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InspeccionVehiculoRepository extends JpaRepository<InspeccionVehiculo, Integer> {}

