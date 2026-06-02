package com.alamo.asistencia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alamo.asistencia.model.InspeccionVehiculo;
import com.alamo.asistencia.model.TipoInspeccionVehiculo;

public interface IInspeccionVehiculoRepository extends JpaRepository<InspeccionVehiculo, Integer> {
    List<InspeccionVehiculo> findByContratoIdContratoAlquilerOrderByFechaInspeccionDesc(Integer idContratoAlquiler);
    List<InspeccionVehiculo> findByContratoIdContratoAlquilerAndTipoInspeccion(Integer idContratoAlquiler, TipoInspeccionVehiculo tipoInspeccion);
}
