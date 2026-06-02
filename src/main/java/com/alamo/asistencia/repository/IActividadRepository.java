package com.alamo.asistencia.repository;

import com.alamo.asistencia.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IActividadRepository extends JpaRepository<Actividad, Integer> {

    // ✅ Lista actividades ACTIVAS por proyecto (ordenadas)
    List<Actividad> findByProyecto_IdTareaAndActivoTrueOrderByNombreAsc(Integer idProyecto);

    // ✅ Buscar por nombre (incluye activas e inactivas) - útil si tú decides reactivar
    Optional<Actividad> findByProyecto_IdTareaAndNombreIgnoreCase(Integer idProyecto, String nombre);

    // ✅ Buscar solo activa (la más típica para "si existe úsala")
    Optional<Actividad> findFirstByProyecto_IdTareaAndNombreIgnoreCaseAndActivoTrue(Integer idProyecto, String nombre);

    // ✅ Validar que una actividad pertenece al proyecto (clave para seguridad)
    boolean existsByIdActividadAndProyecto_IdTarea(Integer idActividad, Integer idProyecto);

    // ✅ Listar incluso inactivas (ordenadas)
    List<Actividad> findByProyecto_IdTareaOrderByNombreAsc(Integer idProyecto);
}
