package com.alamo.asistencia.repository;

import com.alamo.asistencia.model.Saludo;
import com.alamo.asistencia.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ISaludoRepository extends JpaRepository<Saludo, Integer> {

    // 1. Obtener cumpleañeros para la lista de selección (Sigue siendo por mes)
    @Query(value = "SELECT * FROM tb_usuario WHERE MONTH(fecha_nacimiento) = :mes AND estado = 'ACTIVO'", nativeQuery = true)
    List<Usuario> findCumpleanerosDelMes(@Param("mes") Integer mes);

    // 2. CORRECCIÓN DEFINITIVA: Filtrar por id_receptor para que el muro sea personal
    // Solo me salen los saludos que me enviaron a MÍ.
    @Query(value = "SELECT * FROM tb_saludos WHERE id_receptor = :idUsuario ORDER BY fecha_envio DESC", nativeQuery = true)
    List<Saludo> findMisSaludosRecibidos(@Param("idUsuario") Integer idUsuario);
}