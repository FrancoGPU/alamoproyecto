package com.alamo.asistencia.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.alamo.asistencia.model.Informe;
import com.alamo.asistencia.model.Usuario;

public interface InformeRepository extends JpaRepository<Informe, Integer> {

    // Obtener informes por usuario
    List<Informe> findByAsistencia_Usuario(Usuario usuario);

    // Obtener informes de un usuario filtrando por rango de fechas
    List<Informe> findByAsistencia_UsuarioAndAsistencia_FechaBetween(
            Usuario usuario,
            LocalDate inicio,
            LocalDate fin
    );

    // Obtener informes por fecha de asistencia
    List<Informe> findByAsistencia_FechaBetween(LocalDate inicio, LocalDate fin);
}
