package com.alamo.asistencia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alamo.asistencia.model.ContratoSeguro;
import com.alamo.asistencia.model.ContratoSeguroId;

public interface IContratoSeguroRepository extends JpaRepository<ContratoSeguro, ContratoSeguroId> {
    List<ContratoSeguro> findByContratoIdContratoAlquiler(Integer idContratoAlquiler);
}
