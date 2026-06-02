package com.alamo.asistencia.repository;

import com.alamo.asistencia.model.AsistenciaAudit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AsistenciaAuditRepository extends JpaRepository<AsistenciaAudit, Long> {

    // ==================================================
    // 1) Auditoría por asistencia específica (TOP 20)
    // ==================================================
    @EntityGraph(attributePaths = {
            "usuarioActor"
    })
    List<AsistenciaAudit> findTop20ByIdAsistenciaOrderByFechaAccionDesc(
            Integer idAsistencia
    );

    // ==================================================
    // 2) Auditoría GENERAL por rango (sin límite)
    // ==================================================
    @EntityGraph(attributePaths = {
            "usuarioActor"
    })
    List<AsistenciaAudit> findByFechaAccionGreaterThanEqualAndFechaAccionLessThanOrderByFechaAccionDesc(
            LocalDateTime desde,
            LocalDateTime hasta
    );

    // ==================================================
    // 3) Auditoría GENERAL por rango con límite (RECOMENDADO)
    // ==================================================
    @EntityGraph(attributePaths = {
            "usuarioActor"
    })
    List<AsistenciaAudit> findByFechaAccionGreaterThanEqualAndFechaAccionLessThanOrderByFechaAccionDesc(
            LocalDateTime desde,
            LocalDateTime hasta,
            Pageable pageable
    );
}