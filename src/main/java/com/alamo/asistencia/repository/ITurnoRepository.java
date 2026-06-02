package com.alamo.asistencia.repository;

import com.alamo.asistencia.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface ITurnoRepository extends JpaRepository<Turno, Integer> {
    // Necesario para que el Service busque si el turno manual ya existe
    Optional<Turno> findByEntradaAndSalida(LocalTime entrada, LocalTime salida);
}