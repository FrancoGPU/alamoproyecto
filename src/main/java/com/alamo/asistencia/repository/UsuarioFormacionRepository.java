package com.alamo.asistencia.repository;

import com.alamo.asistencia.model.UsuarioFormacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioFormacionRepository extends JpaRepository<UsuarioFormacion, Integer> {

    List<UsuarioFormacion> findByUsuario_IdUsuario(Integer idUsuario);

    void deleteByUsuario_IdUsuario(Integer idUsuario);
}