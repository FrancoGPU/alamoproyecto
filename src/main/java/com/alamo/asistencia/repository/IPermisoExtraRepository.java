package com.alamo.asistencia.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.alamo.asistencia.model.PermisoExtra;

@Repository
public interface IPermisoExtraRepository extends JpaRepository<PermisoExtra, Integer> {
    
    // 1. Conteo para el badge de notificaciones (Pendientes)
    long countByEstado(String estado);
    
    // 2. Listado de solicitudes pendientes para el modal de aprobación
    List<PermisoExtra> findByEstadoOrderByFechaSolicitudDesc(String estado);
    
    // 3. Validación para RECORTE DE SALIDA: Verifica permiso aprobado hoy
    Optional<PermisoExtra> findByUsuario_IdUsuarioAndFechaSolicitudAndEstado(Integer idUsuario, LocalDate fecha, String estado);

    // 4. Validación para RE-INGRESO: Verifica permiso aprobado no utilizado
    Optional<PermisoExtra> findByUsuario_IdUsuarioAndFechaSolicitudAndEstadoAndUsadoFalse(Integer idUsuario, LocalDate fecha, String estado);

    // 5. Historial general sin filtros (Orden cronológico inverso)
    List<PermisoExtra> findAllByOrderByFechaSolicitudDesc();

    // 6. Filtro dinámico: Historial por MES y AÑO
    @Query("SELECT p FROM PermisoExtra p WHERE MONTH(p.fechaSolicitud) = :mes AND YEAR(p.fechaSolicitud) = :anio ORDER BY p.fechaSolicitud DESC")
    List<PermisoExtra> findByMesAnio(@Param("mes") int mes, @Param("anio") int anio);

    // 7. Filtro dinámico: Historial por USUARIO, MES y AÑO
    @Query("SELECT p FROM PermisoExtra p WHERE p.usuario.idUsuario = :idUsuario AND MONTH(p.fechaSolicitud) = :mes AND YEAR(p.fechaSolicitud) = :anio ORDER BY p.fechaSolicitud DESC")
    List<PermisoExtra> findByUsuario_IdUsuarioAndMesAndAnio(
        @Param("idUsuario") Integer idUsuario, 
        @Param("mes") int mes, 
        @Param("anio") int anio
    );
}