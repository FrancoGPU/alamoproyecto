package com.alamo.asistencia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alamo.asistencia.model.ContactoEtiqueta;
import com.alamo.asistencia.model.ContactoEtiquetaId;

@Repository
public interface IContactoEtiquetaRepository
        extends JpaRepository<ContactoEtiqueta, ContactoEtiquetaId> {

    // ✅ Listar relaciones por contacto (si lo necesitas para algo interno)
    @Query("""
        SELECT ce
        FROM ContactoEtiqueta ce
        WHERE ce.id_contacto = :idContacto
    """)
    List<ContactoEtiqueta> findByIdContacto(@Param("idContacto") Integer idContacto);

    // ✅ Listar relaciones por etiqueta (si lo necesitas)
    @Query("""
        SELECT ce
        FROM ContactoEtiqueta ce
        WHERE ce.id_etiqueta = :idEtiqueta
    """)
    List<ContactoEtiqueta> findByIdEtiqueta(@Param("idEtiqueta") Integer idEtiqueta);

    // ✅ Listar SOLO los IDs de etiqueta de un contacto (para el frontend ✅)
    @Query("""
        SELECT ce.id_etiqueta
        FROM ContactoEtiqueta ce
        WHERE ce.id_contacto = :idContacto
        ORDER BY ce.id_etiqueta ASC
    """)
    List<Integer> listarIdsEtiquetasPorContacto(@Param("idContacto") Integer idContacto);

    // ✅ Verificar si ya existe relación (para no duplicar)
    @Query("""
        SELECT (COUNT(ce) > 0)
        FROM ContactoEtiqueta ce
        WHERE ce.id_contacto = :idContacto
          AND ce.id_etiqueta = :idEtiqueta
    """)
    boolean existsRelacion(@Param("idContacto") Integer idContacto,
                           @Param("idEtiqueta") Integer idEtiqueta);

    // ✅ Eliminar relación contacto-etiqueta (idempotente)
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        DELETE FROM ContactoEtiqueta ce
        WHERE ce.id_contacto = :idContacto
          AND ce.id_etiqueta = :idEtiqueta
    """)
    int deleteRelacion(@Param("idContacto") Integer idContacto,
                       @Param("idEtiqueta") Integer idEtiqueta);
}
