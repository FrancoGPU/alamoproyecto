package com.alamo.asistencia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.alamo.asistencia.model.Servicio;
import java.util.Optional;

public interface IServicioRepository extends JpaRepository<Servicio, Integer> {
    
    // 💡 Método para sumar el gasto total de todos los campos de Servicio
    @Query("SELECT SUM(s.transporte + s.comida + s.gastoRestaurante + s.celulares + s.canva + s.chatgpt + s.icloud + s.googleCapa + s.oneDrive + s.servidor + s.otros) FROM Servicio s WHERE s.ingreso.idIngreso = :idIngreso")
    Optional<Double> sumTotalByIngresoId(@Param("idIngreso") Integer idIngreso);
}