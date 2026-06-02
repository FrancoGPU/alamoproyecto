package com.alamo.asistencia.repository;

import com.alamo.asistencia.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProductoRepository extends JpaRepository<Producto, Integer> {

    // =========================================================
    // CONSULTAS EXISTENTES
    // =========================================================
    List<Producto> findByIngreso_IdIngreso(Integer idIngreso);

    List<Producto> findByIngreso_IdIngresoAndNombreContainingIgnoreCase(Integer idIngreso, String nombre);

    @Query("""
        SELECT COALESCE(SUM(p.precio * p.cantidad), 0)
        FROM Producto p
        WHERE p.ingreso.idIngreso = :idIngreso
        """)
    Optional<Double> sumTotalByIngresoId(@Param("idIngreso") Integer idIngreso);

    // =========================================================
    // FILTRO GLOBAL + PAGINACIÓN
    // =========================================================
    @Query(
        value = """
            SELECT p
            FROM Producto p
            WHERE
                (
                    :search IS NULL OR :search = '' OR
                    LOWER(COALESCE(p.nombre, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(p.numeroSerie, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(p.comprador, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(p.localDestino, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(p.numeroRuc, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(p.responsable, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(p.emisor, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(p.cliente, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                )
                AND (:local IS NULL OR :local = '' OR LOWER(COALESCE(p.localDestino, '')) = LOWER(:local))
                AND (:prop IS NULL OR :prop = '' OR LOWER(COALESCE(p.propiedad, '')) = LOWER(:prop))
                AND (:buyer IS NULL OR :buyer = '' OR LOWER(COALESCE(p.comprador, '')) = LOWER(:buyer))
                AND (:resp IS NULL OR :resp = '' OR LOWER(COALESCE(p.responsable, '')) = LOWER(:resp))
                AND (:emisor IS NULL OR :emisor = '' OR LOWER(COALESCE(p.emisor, '')) = LOWER(:emisor))
                AND (:state IS NULL OR :state = '' OR LOWER(COALESCE(p.estadoProducto, '')) = LOWER(:state))
                AND (:assign IS NULL OR :assign = '' OR UPPER(COALESCE(p.aplicaAsignacion, '')) = UPPER(:assign))
            ORDER BY p.ingreso.mes ASC, p.idProducto ASC
            """,
        countQuery = """
            SELECT COUNT(p)
            FROM Producto p
            WHERE
                (
                    :search IS NULL OR :search = '' OR
                    LOWER(COALESCE(p.nombre, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(p.numeroSerie, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(p.comprador, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(p.localDestino, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(p.numeroRuc, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(p.responsable, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(p.emisor, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(p.cliente, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                )
                AND (:local IS NULL OR :local = '' OR LOWER(COALESCE(p.localDestino, '')) = LOWER(:local))
                AND (:prop IS NULL OR :prop = '' OR LOWER(COALESCE(p.propiedad, '')) = LOWER(:prop))
                AND (:buyer IS NULL OR :buyer = '' OR LOWER(COALESCE(p.comprador, '')) = LOWER(:buyer))
                AND (:resp IS NULL OR :resp = '' OR LOWER(COALESCE(p.responsable, '')) = LOWER(:resp))
                AND (:emisor IS NULL OR :emisor = '' OR LOWER(COALESCE(p.emisor, '')) = LOWER(:emisor))
                AND (:state IS NULL OR :state = '' OR LOWER(COALESCE(p.estadoProducto, '')) = LOWER(:state))
                AND (:assign IS NULL OR :assign = '' OR UPPER(COALESCE(p.aplicaAsignacion, '')) = UPPER(:assign))
            """
    )
    Page<Producto> buscarFiltradosGlobal(
            @Param("search") String search,
            @Param("local") String local,
            @Param("prop") String prop,
            @Param("buyer") String buyer,
            @Param("resp") String resp,
            @Param("emisor") String emisor,
            @Param("state") String state,
            @Param("assign") String assign,
            Pageable pageable
    );

    // =========================================================
    // FILTRO GLOBAL SIN PAGINACIÓN
    // =========================================================
    @Query("""
        SELECT p
        FROM Producto p
        WHERE
            (
                :search IS NULL OR :search = '' OR
                LOWER(COALESCE(p.nombre, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(COALESCE(p.numeroSerie, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(COALESCE(p.comprador, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(COALESCE(p.localDestino, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(COALESCE(p.numeroRuc, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(COALESCE(p.responsable, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(COALESCE(p.emisor, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(COALESCE(p.cliente, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            AND (:local IS NULL OR :local = '' OR LOWER(COALESCE(p.localDestino, '')) = LOWER(:local))
            AND (:prop IS NULL OR :prop = '' OR LOWER(COALESCE(p.propiedad, '')) = LOWER(:prop))
            AND (:buyer IS NULL OR :buyer = '' OR LOWER(COALESCE(p.comprador, '')) = LOWER(:buyer))
            AND (:resp IS NULL OR :resp = '' OR LOWER(COALESCE(p.responsable, '')) = LOWER(:resp))
            AND (:emisor IS NULL OR :emisor = '' OR LOWER(COALESCE(p.emisor, '')) = LOWER(:emisor))
            AND (:state IS NULL OR :state = '' OR LOWER(COALESCE(p.estadoProducto, '')) = LOWER(:state))
            AND (:assign IS NULL OR :assign = '' OR UPPER(COALESCE(p.aplicaAsignacion, '')) = UPPER(:assign))
        ORDER BY p.ingreso.mes ASC, p.idProducto ASC
        """)
    List<Producto> listarFiltradosGlobal(
            @Param("search") String search,
            @Param("local") String local,
            @Param("prop") String prop,
            @Param("buyer") String buyer,
            @Param("resp") String resp,
            @Param("emisor") String emisor,
            @Param("state") String state,
            @Param("assign") String assign
    );

    // =========================================================
    // SUMA GLOBAL FILTRADA
    // =========================================================
    @Query("""
        SELECT COALESCE(SUM(p.precio * p.cantidad), 0)
        FROM Producto p
        WHERE
            (
                :search IS NULL OR :search = '' OR
                LOWER(COALESCE(p.nombre, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(COALESCE(p.numeroSerie, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(COALESCE(p.comprador, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(COALESCE(p.localDestino, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(COALESCE(p.numeroRuc, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(COALESCE(p.responsable, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(COALESCE(p.emisor, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(COALESCE(p.cliente, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            AND (:local IS NULL OR :local = '' OR LOWER(COALESCE(p.localDestino, '')) = LOWER(:local))
            AND (:prop IS NULL OR :prop = '' OR LOWER(COALESCE(p.propiedad, '')) = LOWER(:prop))
            AND (:buyer IS NULL OR :buyer = '' OR LOWER(COALESCE(p.comprador, '')) = LOWER(:buyer))
            AND (:resp IS NULL OR :resp = '' OR LOWER(COALESCE(p.responsable, '')) = LOWER(:resp))
            AND (:emisor IS NULL OR :emisor = '' OR LOWER(COALESCE(p.emisor, '')) = LOWER(:emisor))
            AND (:state IS NULL OR :state = '' OR LOWER(COALESCE(p.estadoProducto, '')) = LOWER(:state))
            AND (:assign IS NULL OR :assign = '' OR UPPER(COALESCE(p.aplicaAsignacion, '')) = UPPER(:assign))
        """)
    Double sumarTotalFiltradoGlobal(
            @Param("search") String search,
            @Param("local") String local,
            @Param("prop") String prop,
            @Param("buyer") String buyer,
            @Param("resp") String resp,
            @Param("emisor") String emisor,
            @Param("state") String state,
            @Param("assign") String assign
    );

    // =========================================================
    // COMBOS DE FILTROS
    // =========================================================
    @Query("""
        SELECT DISTINCT p.localDestino
        FROM Producto p
        WHERE p.localDestino IS NOT NULL AND TRIM(p.localDestino) <> ''
        ORDER BY p.localDestino
        """)
    List<String> obtenerLocales();

    @Query("""
        SELECT DISTINCT p.propiedad
        FROM Producto p
        WHERE p.propiedad IS NOT NULL AND TRIM(p.propiedad) <> ''
        ORDER BY p.propiedad
        """)
    List<String> obtenerPropiedades();

    @Query("""
        SELECT DISTINCT p.comprador
        FROM Producto p
        WHERE p.comprador IS NOT NULL AND TRIM(p.comprador) <> ''
        ORDER BY p.comprador
        """)
    List<String> obtenerCompradores();

    @Query("""
        SELECT DISTINCT p.responsable
        FROM Producto p
        WHERE p.responsable IS NOT NULL AND TRIM(p.responsable) <> ''
        ORDER BY p.responsable
        """)
    List<String> obtenerResponsables();

    @Query("""
        SELECT DISTINCT p.emisor
        FROM Producto p
        WHERE p.emisor IS NOT NULL AND TRIM(p.emisor) <> ''
        ORDER BY p.emisor
        """)
    List<String> obtenerEmisores();
}