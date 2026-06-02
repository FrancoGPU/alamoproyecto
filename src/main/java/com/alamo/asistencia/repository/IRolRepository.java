package com.alamo.asistencia.repository;

import com.alamo.asistencia.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// La interfaz extiende JpaRepository, usando la entidad Rol y el tipo de su ID (Integer)
@Repository
public interface IRolRepository extends JpaRepository<Rol, Integer> {
    // Spring Data JPA proveerá automáticamente los métodos básicos: findAll(), findById(), save(), etc.
}