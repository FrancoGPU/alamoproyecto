package com.alamo.asistencia.repository;

import com.alamo.asistencia.model.Ingreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface IIngresoRepository extends JpaRepository<Ingreso, Integer> {

    // ✅ PASO 1: Cargar el ingreso con sus productos
    @Query("SELECT DISTINCT i FROM Ingreso i LEFT JOIN FETCH i.productos WHERE i.mes = :mes")
    Optional<Ingreso> findMesConProductos(@Param("mes") LocalDate mes);

    // ✅ PASO 2: Cargar el ingreso con sus servicios
    @Query("SELECT DISTINCT i FROM Ingreso i LEFT JOIN FETCH i.servicios WHERE i.mes = :mes")
    Optional<Ingreso> findMesConServicios(@Param("mes") LocalDate mes);

    // Mantenemos este para compatibilidad general
    List<Ingreso> findByMes(LocalDate mes); 
    
    @Query("SELECT i FROM Ingreso i WHERE YEAR(i.mes) = :year AND MONTH(i.mes) = :month")
    List<Ingreso> findByMesYear(@Param("year") int year, @Param("month") int month);
    
    Optional<Ingreso> findByProductosIdProducto(Integer idProducto);
    Optional<Ingreso> findByServiciosIdServicio(Integer idServicio);
}