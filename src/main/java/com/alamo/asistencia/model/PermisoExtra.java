package com.alamo.asistencia.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tb_permiso_extra")
@Data
public class PermisoExtra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Integer idPermiso;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario; // El trabajador que solicita o recibe la potestad

    @ManyToOne
    @JoinColumn(name = "id_admin_aprobador")
    private Usuario adminAprobador; // El administrador que presiona el botón de Autorizar/Rechazar

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDate fechaSolicitud; // Día para el cual se aplica la potestad

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion; // Momento exacto en que el admin procesó la solicitud

    @Column(name = "motivo", length = 255)
    private String motivo; // Por qué se requieren las horas extras

    @Column(name = "estado", length = 20)
    private String estado; // PENDIENTE, APROBADO, RECHAZADO

    @Column(name = "usado")
    private boolean usado = false; // Útil para el modo re-ingreso (para no duplicar entradas extra)
}