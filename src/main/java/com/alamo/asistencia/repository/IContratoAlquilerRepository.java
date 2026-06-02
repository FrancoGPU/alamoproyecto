package com.alamo.asistencia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alamo.asistencia.model.ContratoAlquiler;
import com.alamo.asistencia.model.EstadoContratoAlquiler;

public interface IContratoAlquilerRepository extends JpaRepository<ContratoAlquiler, Integer> {
    Optional<ContratoAlquiler> findByCodigoContrato(String codigoContrato);
    List<ContratoAlquiler> findByEstadoOrderByFechaHoraPickupDesc(EstadoContratoAlquiler estado);
}
