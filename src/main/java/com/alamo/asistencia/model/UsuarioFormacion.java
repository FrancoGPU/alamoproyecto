package com.alamo.asistencia.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_usuario_formacion")
@Data
public class UsuarioFormacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_formacion")
    private Integer idFormacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel", nullable = false, length = 30)
    private NivelFormacion nivel;

    @Column(name = "institucion", nullable = false, length = 180)
    private String institucion;

    @Column(name = "programa", length = 180)
    private String programa;

    @Column(name = "grado", length = 100)
    private String grado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoFormacion estado = EstadoFormacion.COMPLETO;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===========================
    // ENUMS DENTRO DEL MISMO ARCHIVO
    // ===========================
    public enum NivelFormacion {
        INICIAL,
        PRIMARIA,
        SECUNDARIA,
        TECNICO,
        UNIVERSITARIO,
        POSTGRADO,
        OTRO
    }

    public enum EstadoFormacion {
        COMPLETO,
        INCOMPLETO,
        EN_CURSO
    }
}