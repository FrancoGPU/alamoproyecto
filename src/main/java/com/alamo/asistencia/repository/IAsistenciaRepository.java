package com.alamo.asistencia.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.alamo.asistencia.model.Asistencia;

@Repository
public interface IAsistenciaRepository extends JpaRepository<Asistencia, Integer> {

    List<Asistencia> findByUsuario_IdUsuarioAndFecha(Integer idUsuario, LocalDate fecha);

    List<Asistencia> findByUsuario_IdUsuarioAndFechaBetweenOrderByFechaAsc(
            Integer idUsuario, LocalDate inicio, LocalDate fin
    );

    List<Asistencia> findByFechaBetweenOrderByFechaDescHoraEntradaDesc(
            LocalDate inicio, LocalDate fin
    );

    @Query("""
        SELECT a
        FROM Asistencia a
        WHERE a.usuario.idUsuario = :idUsuario
          AND a.fecha = CURRENT_DATE
          AND a.horaSalida IS NULL
    """)
    List<Asistencia> findEntradasPendientesHoy(@Param("idUsuario") Integer idUsuario);
}