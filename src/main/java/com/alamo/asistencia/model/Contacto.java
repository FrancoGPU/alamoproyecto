package com.alamo.asistencia.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tb_contacto")
@Data
public class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contacto")
    private Integer id_contacto;

    @Column(name = "primer_nombre", nullable = false, length = 60)
    private String primer_nombre;

    @Column(name = "segundo_nombre", length = 60)
    private String segundo_nombre;

    @Column(name = "apellido_paterno", length = 60)
    private String apellido_paterno;

    @Column(name = "apellido_materno", length = 60)
    private String apellido_materno;

    @Column(name = "alias", length = 80)
    private String alias;

    @Column(name = "dni", length = 15, unique = true)
    private String dni;

    @Column(name = "correo", length = 120)
    private String correo;

    @Column(name = "telefono_principal", length = 25)
    private String telefono_principal;

    @Column(name = "numero_secundario", length = 25)
    private String numero_secundario;

    @Column(name = "celular_whatsapp", length = 25)
    private String celular_whatsapp;

    @Column(name = "telefono_fijo", length = 25)
    private String telefono_fijo;

    @Column(name = "departamento", length = 80)
    private String departamento;

    @Column(name = "provincia", length = 80)
    private String provincia;

    @Column(name = "distrito", length = 80)
    private String distrito;

    // ✅ NUEVOS CAMPOS
    @Column(name = "direccion", length = 160)
    private String direccion;

    @Column(name = "direccion_exacta", length = 255)
    private String direccion_exacta;

    @Column(name = "fecha_nacimiento")
    private LocalDate fecha_nacimiento;

    @Column(name = "trabajo", length = 120)
    private String trabajo;

    @Column(name = "cargo", length = 120)
    private String cargo;

    @Column(name = "institucion_cargo", length = 160)
    private String institucion_cargo;

    // ✅ FECHAS DEL CARGO
    @Column(name = "fecha_inicio_cargo")
    private LocalDate fecha_inicio_cargo;

    @Column(name = "fecha_fin_cargo")
    private LocalDate fecha_fin_cargo;

    @Column(name = "categoria", length = 80)
    private String categoria;

    @Column(name = "profesion", length = 120)
    private String profesion;

    @Column(name = "referencia", length = 200)
    private String referencia;

    @Column(name = "parentesco_enrique", length = 120)
    private String parentesco_enrique;

    @Column(name = "lenguaje_enrique", length = 120)
    private String lenguaje_enrique;

    @Column(name = "deno_enrique", length = 120)
    private String deno_enrique;

    @Column(name = "dato_importante", columnDefinition = "TEXT")
    private String dato_importante;

    @Column(name = "gustos", columnDefinition = "TEXT")
    private String gustos;

    @Column(name = "contactabilidad_alterna", columnDefinition = "TEXT")
    private String contactabilidad_alterna;

    @Column(name = "foto_url", length = 255)
    private String foto_url;

    @Column(name = "asistio_eventos", nullable = false)
    private Boolean asistio_eventos = false;

    @Column(name = "eventos_detalle", columnDefinition = "TEXT")
    private String eventos_detalle;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    @Column(name = "date_create", nullable = false, updatable = false)
    private LocalDateTime date_create;

    @Column(name = "date_update")
    private LocalDateTime date_update;

    @Column(name = "user_create", length = 50)
    private String user_create;

    @Column(name = "user_update", length = 50)
    private String user_update;

    @PrePersist
    void prePersist() {
        this.date_create = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.date_update = LocalDateTime.now();
    }
}