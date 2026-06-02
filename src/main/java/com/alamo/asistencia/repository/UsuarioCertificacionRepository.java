package com.alamo.asistencia.repository;

import com.alamo.asistencia.model.UsuarioCertificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioCertificacionRepository extends JpaRepository<UsuarioCertificacion, Integer> {

    List<UsuarioCertificacion> findByUsuario_IdUsuario(Integer idUsuario);

    void deleteByUsuario_IdUsuario(Integer idUsuario);
}