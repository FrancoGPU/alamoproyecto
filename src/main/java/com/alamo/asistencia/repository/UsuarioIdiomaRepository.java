package com.alamo.asistencia.repository;

import com.alamo.asistencia.model.UsuarioIdioma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioIdiomaRepository extends JpaRepository<UsuarioIdioma, Integer> {
    List<UsuarioIdioma> findByUsuario_IdUsuario(Integer idUsuario);
    void deleteByUsuario_IdUsuario(Integer idUsuario);
}