package com.alamo.alquiler.repository;

import com.alamo.alquiler.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
    List<Notificacion> findFirst10ByOrderByFechaCreacionDesc();
}
