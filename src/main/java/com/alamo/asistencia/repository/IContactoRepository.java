package com.alamo.asistencia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.alamo.asistencia.model.Contacto;

@Repository
public interface IContactoRepository extends JpaRepository<Contacto, Integer> {

    Optional<Contacto> findByDni(String dni);

    // =========================
    // ACTIVOS
    // =========================
    @Query("""
        SELECT DISTINCT c
        FROM Contacto c
        WHERE c.estado = true
          AND (
            :q IS NULL OR :q = '' OR
            LOWER(COALESCE(c.primer_nombre, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.segundo_nombre, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.apellido_paterno, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.apellido_materno, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.alias, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            COALESCE(c.dni, '') LIKE CONCAT('%', :q, '%') OR
            COALESCE(c.telefono_principal, '') LIKE CONCAT('%', :q, '%') OR
            COALESCE(c.celular_whatsapp, '') LIKE CONCAT('%', :q, '%') OR
            LOWER(COALESCE(c.correo, '')) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        ORDER BY COALESCE(c.primer_nombre, '') ASC,
                 COALESCE(c.apellido_paterno, '') ASC,
                 c.id_contacto DESC
    """)
    List<Contacto> buscarActivos(@Param("q") String q);

    // =========================
    // ARCHIVADOS
    // =========================
    @Query("""
        SELECT DISTINCT c
        FROM Contacto c
        WHERE c.estado = false
          AND (
            :q IS NULL OR :q = '' OR
            LOWER(COALESCE(c.primer_nombre, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.segundo_nombre, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.apellido_paterno, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.apellido_materno, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.alias, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            COALESCE(c.dni, '') LIKE CONCAT('%', :q, '%') OR
            COALESCE(c.telefono_principal, '') LIKE CONCAT('%', :q, '%') OR
            COALESCE(c.celular_whatsapp, '') LIKE CONCAT('%', :q, '%') OR
            LOWER(COALESCE(c.correo, '')) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        ORDER BY COALESCE(c.primer_nombre, '') ASC,
                 COALESCE(c.apellido_paterno, '') ASC,
                 c.id_contacto DESC
    """)
    List<Contacto> buscarArchivados(@Param("q") String q);

    // =========================
    // ACTIVOS POR ETIQUETA (FIX REAL)
    // =========================
    @Query("""
        SELECT DISTINCT c
        FROM Contacto c
        WHERE c.estado = true
          AND EXISTS (
              SELECT 1
              FROM ContactoEtiqueta ce
              WHERE ce.id_contacto = c.id_contacto
                AND ce.id_etiqueta = :idEtiqueta
          )
          AND (
            :q IS NULL OR :q = '' OR
            LOWER(COALESCE(c.primer_nombre, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.segundo_nombre, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.apellido_paterno, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.apellido_materno, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.alias, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            COALESCE(c.dni, '') LIKE CONCAT('%', :q, '%') OR
            COALESCE(c.telefono_principal, '') LIKE CONCAT('%', :q, '%') OR
            COALESCE(c.celular_whatsapp, '') LIKE CONCAT('%', :q, '%') OR
            LOWER(COALESCE(c.correo, '')) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        ORDER BY COALESCE(c.primer_nombre, '') ASC,
                 COALESCE(c.apellido_paterno, '') ASC,
                 c.id_contacto DESC
    """)
    List<Contacto> listarPorEtiqueta(@Param("idEtiqueta") Integer idEtiqueta,
                                     @Param("q") String q);
}
