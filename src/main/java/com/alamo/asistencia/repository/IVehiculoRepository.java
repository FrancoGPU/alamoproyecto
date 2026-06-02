package com.alamo.asistencia.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.alamo.asistencia.model.EstadoVehiculo;
import com.alamo.asistencia.model.TipoCategoriaVehiculo;
import com.alamo.asistencia.model.Vehiculo;

public interface IVehiculoRepository extends JpaRepository<Vehiculo, Integer> {

    List<Vehiculo> findByEstado(EstadoVehiculo estado);

    @Query("""
            SELECT v
            FROM Vehiculo v
            JOIN v.categoria c
            WHERE v.estado = com.alamo.asistencia.model.EstadoVehiculo.DISPONIBLE
              AND (:categoria IS NULL OR c.tipo = :categoria)
              AND NOT EXISTS (
                    SELECT 1
                    FROM ContratoAlquiler ca
                    WHERE ca.vehiculo = v
                      AND ca.estado IN (
                            com.alamo.asistencia.model.EstadoContratoAlquiler.PENDIENTE,
                            com.alamo.asistencia.model.EstadoContratoAlquiler.ACTIVO
                      )
                      AND ca.fechaHoraPickup < :dropoff
                      AND ca.fechaHoraDropoff > :pickup
              )
            ORDER BY c.tipo ASC, v.marca ASC, v.modelo ASC
            """)
    List<Vehiculo> buscarDisponibles(
            @Param("categoria") TipoCategoriaVehiculo categoria,
            @Param("pickup") LocalDateTime pickup,
            @Param("dropoff") LocalDateTime dropoff
    );
}
