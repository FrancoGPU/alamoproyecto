package com.alamo.asistencia.repository;

import com.alamo.asistencia.model.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IHorarioRepository extends JpaRepository<Horario, Integer> {

    // 1. Para listar todos los horarios de un usuario
    List<Horario> findByUsuario_IdUsuarioOrderByDiaAsc(Integer idUsuario);

    // 2. PARA HORARIO SERVICE: Ordenar por ID es la clave para que NO SE ACUMULEN.
    // El primer horario creado (T1) siempre tendrá el ID más bajo.
    List<Horario> findByUsuario_IdUsuarioAndDiaOrderByIdHorarioAsc(Integer idUsuario, Integer dia);

    // 3. Para AsistenciaService (Mantiene compatibilidad con la lógica de entrada/salida)
    List<Horario> findByUsuario_IdUsuarioAndDia(Integer idUsuario, Integer dia);

    // 4. Compatibilidad para otros Controllers (Reportes/Vistas)
    Optional<Horario> findFirstByUsuario_IdUsuarioAndDia(Integer idUsuario, Integer dia);
}