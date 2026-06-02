package com.alamo.asistencia.repository;

import com.alamo.asistencia.model.UsuarioExperiencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioExperienciaRepository extends JpaRepository<UsuarioExperiencia, Integer> {

    List<UsuarioExperiencia> findByUsuario_IdUsuario(Integer idUsuario);

    void deleteByUsuario_IdUsuario(Integer idUsuario);

}