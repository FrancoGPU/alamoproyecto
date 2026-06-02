package com.alamo.asistencia.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alamo.asistencia.model.Informe;
import com.alamo.asistencia.model.Usuario;

@Repository
public interface IInformeRepository extends JpaRepository<Informe, Integer> {

    // Listar informes filtrando por usuario
    List<Informe> findByAsistencia_Usuario(Usuario usuario);

    // Listar informes entre fechas (por mes/año)
    List<Informe> findByAsistencia_FechaBetween(LocalDate inicio, LocalDate fin);
}
