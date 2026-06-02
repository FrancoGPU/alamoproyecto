package com.alamo.asistencia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alamo.asistencia.model.RevisionInforme;

public interface IRevisionInformeRepository extends JpaRepository<RevisionInforme, Integer> {

    Optional<RevisionInforme> findByUsuario_IdUsuarioAndAnioAndMes(Integer idUsuario, Integer anio, Integer mes);

    List<RevisionInforme> findAllByAnioAndMes(Integer anio, Integer mes);
}