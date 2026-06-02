package com.alamo.asistencia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alamo.asistencia.model.ServicioAdicional;

public interface IServicioAdicionalRepository extends JpaRepository<ServicioAdicional, Integer> {
    List<ServicioAdicional> findByContratoIdContratoAlquiler(Integer idContratoAlquiler);
}
