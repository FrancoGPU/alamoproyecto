package com.alamo.asistencia.repository;

import com.alamo.asistencia.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByDniAndContrasenia(String dni, String contrasenia);

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.rutaCV = :nombreArchivo WHERE u.idUsuario = :idUsuario")
    void actualizarRutaCV(@Param("idUsuario") Integer idUsuario,
                          @Param("nombreArchivo") String nombreArchivo);

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.ruta_foto_id = :ruta, u.foto_id_nombre = :nombre, u.fecha_modificacion = CURRENT_TIMESTAMP WHERE u.idUsuario = :idUsuario")
    void actualizarRutaFotoID(@Param("idUsuario") Integer idUsuario,
                              @Param("ruta") String ruta,
                              @Param("nombre") String nombre);

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.ruta_foto_id_trasera = :ruta, u.foto_id_nombre_trasera = :nombre, u.fecha_modificacion = CURRENT_TIMESTAMP WHERE u.idUsuario = :idUsuario")
    void actualizarRutaFotoIDTrasera(@Param("idUsuario") Integer idUsuario,
                                     @Param("ruta") String ruta,
                                     @Param("nombre") String nombre);

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.fecha_nacimiento = :fecha_nacimiento, u.sexo = :sexo, u.estado_civil = :estado_civil, " +
            "u.nacionalidad = :nacionalidad, u.grupo_sanguineo = :grupo_sanguineo, u.correo = :correo, u.telefono = :telefono, " +
            "u.direccion = :direccion, u.nombre_contacto_emergencia = :nombre_contacto_emergencia, " +
            "u.relacion_contacto_emergencia = :relacion_contacto_emergencia, u.telefono_emergencia = :telefono_emergencia, " +
            "u.fecha_modificacion = CURRENT_TIMESTAMP WHERE u.idUsuario = :idUsuario")
    void actualizarDatosPersonales(
            @Param("idUsuario") Integer idUsuario,
            @Param("fecha_nacimiento") LocalDate fecha_nacimiento,
            @Param("sexo") String sexo,
            @Param("estado_civil") String estado_civil,
            @Param("nacionalidad") String nacionalidad,
            @Param("grupo_sanguineo") String grupo_sanguineo,
            @Param("correo") String correo,
            @Param("telefono") String telefono,
            @Param("direccion") String direccion,
            @Param("nombre_contacto_emergencia") String nombre_contacto_emergencia,
            @Param("relacion_contacto_emergencia") String relacion_contacto_emergencia,
            @Param("telefono_emergencia") String telefono_emergencia
    );

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.fecha_ingreso = :fecha_ingreso, u.tipo_contrato = :tipo_contrato, u.modalidad = :modalidad, " +
            "u.numero_ruc = :numero_ruc, u.fecha_modificacion = CURRENT_TIMESTAMP WHERE u.idUsuario = :idUsuario")
    void actualizarDatosLaborales(
            @Param("idUsuario") Integer idUsuario,
            @Param("fecha_ingreso") LocalDate fecha_ingreso,
            @Param("tipo_contrato") String tipo_contrato,
            @Param("modalidad") String modalidad,
            @Param("numero_ruc") String numero_ruc
    );

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.banco = :banco, u.cuenta_bancaria = :cuenta_bancaria, u.cuenta_interbancaria = :cuenta_interbancaria, " +
            "u.banca_movil = :banca_movil, u.fecha_modificacion = CURRENT_TIMESTAMP WHERE u.idUsuario = :idUsuario")
    void actualizarDatosBancarios(
            @Param("idUsuario") Integer idUsuario,
            @Param("banco") String banco,
            @Param("cuenta_bancaria") String cuenta_bancaria,
            @Param("cuenta_interbancaria") String cuenta_interbancaria,
            @Param("banca_movil") String banca_movil
    );

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.nivel_educativo = :nivel_educativo, u.instituciones_educativas = :instituciones_educativas, " +
            "u.certificaciones = :certificaciones, u.tipo_seguro = :tipo_seguro, u.fecha_modificacion = CURRENT_TIMESTAMP WHERE u.idUsuario = :idUsuario")
    void actualizarFormacionSalud(
            @Param("idUsuario") Integer idUsuario,
            @Param("nivel_educativo") String nivel_educativo,
            @Param("instituciones_educativas") String instituciones_educativas,
            @Param("certificaciones") String certificaciones,
            @Param("tipo_seguro") String tipo_seguro
    );
}