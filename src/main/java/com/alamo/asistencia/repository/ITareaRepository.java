package com.alamo.asistencia.repository;

import com.alamo.asistencia.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ITareaRepository extends JpaRepository<Tarea, Integer> {

    // =========================
    // 1) Monitor Administrativo
    // =========================
    List<Tarea> findByActivoTrue();

    // =========================
    // 2) Kanban Personal (solo tareas ejecutables)
    // =========================
    List<Tarea> findByResponsable_IdUsuarioAndAmbitoAndActivoTrueAndEstadoNotAndEsProyectoFalse(
            Integer idUsuario, String ambito, String estado
    );

    // =========================
    // 3) Proyectos del usuario (contenedores)
    // =========================
    List<Tarea> findByResponsable_IdUsuarioAndEsProyectoTrueAndActivoTrue(Integer idUsuario);

    List<Tarea> findByCreador_IdUsuarioAndEsProyectoTrueAndActivoTrue(Integer idUsuario);

    // =========================
    // 4) Historial / Completadas
    // =========================
    List<Tarea> findByResponsable_IdUsuarioAndEstadoAndActivoTrue(Integer idUsuario, String estado);

    // =========================
    // 5) Jerarquía (hijas de un contenedor)
    // =========================
    List<Tarea> findByProyectoPadre_IdTareaAndActivoTrue(Integer idProyectoPadre);

    List<Tarea> findByProyectoPadre_IdTareaAndEsProyectoFalseAndActivoTrue(Integer idProyectoPadre);

    // =========================
    // 6) Calendario / Listado general del usuario
    // =========================
    List<Tarea> findByResponsable_IdUsuarioAndActivoTrue(Integer idUsuario);

    // =========================
    // 7) Lotes / Masivos
    // =========================
    List<Tarea> findByIdGrupoMasivo(String idGrupoMasivo);

    List<Tarea> findByIdGrupoMasivoAndEsProyectoTrueAndActivoTrue(String idGrupoMasivo);

    // =========================
    // 8) Seguimiento del equipo (admin)
    // =========================
    List<Tarea> findByCreador_IdUsuarioAndResponsable_IdUsuarioNotAndActivoTrue(
            Integer idAdmin, Integer idAdminExcluido
    );

    // =========================
    // 9) Reporte Mensual (usa fechaDia DATE)
    // =========================
    @Query("""
        SELECT t
        FROM Tarea t
        WHERE t.activo = true
          AND t.fechaDia IS NOT NULL
          AND FUNCTION('MONTH', t.fechaDia) = :mes
          AND FUNCTION('YEAR',  t.fechaDia) = :anio
    """)
    List<Tarea> findByMesYAnio(@Param("mes") int mes, @Param("anio") int anio);

    @Query("""
        SELECT t
        FROM Tarea t
        WHERE t.activo = true
          AND t.responsable.idUsuario = :idUsuario
          AND t.fechaDia IS NOT NULL
          AND FUNCTION('MONTH', t.fechaDia) = :mes
          AND FUNCTION('YEAR',  t.fechaDia) = :anio
    """)
    List<Tarea> findByResponsableAndMesYAnio(@Param("idUsuario") Integer idUsuario,
                                            @Param("mes") int mes,
                                            @Param("anio") int anio);

    // ✅ rango exacto por DATE (más sano para performance)
    List<Tarea> findByActivoTrueAndFechaDiaBetween(LocalDate desde, LocalDate hasta);

    // =========================
    // 10) Dashboard rápido
    // =========================
    long countByEstadoAndActivoTrueAndEsProyectoFalse(String estado);

    long countByProyectoPadre_IdTareaAndActivoTrueAndEsProyectoFalse(Integer idProyectoPadre);

    long countByProyectoPadre_IdTareaAndActivoTrueAndEsProyectoFalseAndEstado(
            Integer idProyectoPadre, String estado
    );

    // =========================
    // 11) Tabs / filtros
    // =========================
    List<Tarea> findByEsProyectoTrueAndActivoTrue();

    List<Tarea> findByEsProyectoFalseAndProyectoPadreIsNullAndActivoTrue();

    // =========================
    // ✅ CLAVE PARA TU UI: TAREAS POR ACTIVIDAD
    // =========================

    /** ✅ TAREAS ejecutables de una actividad (sin depender del proyectoPadre) */
    List<Tarea> findByActividad_IdActividadAndActivoTrueAndEsProyectoFalse(Integer idActividad);

    /** ✅ TAREAS de una actividad dentro de un proyecto específico */
    @Query("""
        SELECT t
        FROM Tarea t
        WHERE t.activo = true
          AND t.esProyecto = false
          AND t.actividad.idActividad = :idActividad
          AND t.actividad.proyecto.idTarea = :idProyecto
    """)
    List<Tarea> findByProyectoAndActividad(@Param("idProyecto") Integer idProyecto,
                                          @Param("idActividad") Integer idActividad);

    /** ✅ Si además quieres filtrar por padre (por si aún lo usas) */
    List<Tarea> findByProyectoPadre_IdTareaAndActividad_IdActividadAndEsProyectoFalseAndActivoTrue(
            Integer idProyectoPadre, Integer idActividad
    );

    // =========================
    // 12) ✅ CONTADOR PARA BADGE (pendientes)
    // =========================

    /**
     * ✅ RECOMENDADO: cuenta SOLO tareas ejecutables (no proyectos)
     * y que NO estén completadas.
     */
    long countByResponsable_IdUsuarioAndActivoTrueAndEsProyectoFalseAndEstadoNot(
            Integer idUsuario, String estado
    );

    /**
     * (Opcional) Excluye varios estados, ej: COMPLETADA y CANCELADA
     */
    long countByResponsable_IdUsuarioAndActivoTrueAndEsProyectoFalseAndEstadoNotIn(
            Integer idUsuario, List<String> estados
    );
}
