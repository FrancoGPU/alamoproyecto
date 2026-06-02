package com.alamo.asistencia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alamo.asistencia.model.PagoGarantia;

public interface IPagoGarantiaRepository extends JpaRepository<PagoGarantia, Integer> {
    Optional<PagoGarantia> findByContratoIdContratoAlquiler(Integer idContratoAlquiler);
}
