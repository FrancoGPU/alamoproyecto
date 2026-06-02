package com.alamo.asistencia.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_usuario_experiencia")
@Data
public class UsuarioExperiencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_experiencia")
    private Integer idExperiencia;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    // ============================
    // DATOS DE EXPERIENCIA
    // ============================

    @Column(name = "empresa", nullable = false, length = 200)
    private String empresa;

    @Column(name = "puesto", length = 150)
    private String puesto;

    @Column(name = "funciones", columnDefinition = "TEXT")
    private String funciones;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    // ============================
    // REFERENCIA LABORAL
    // ============================

    @Column(name = "nombre_referencia", length = 150)
    private String nombreReferencia;

    @Column(name = "telefono_referencia", length = 30)
    private String telefonoReferencia;

    @Column(name = "correo_referencia", length = 150)
    private String correoReferencia;

    @Column(name = "motivo_salida", length = 200)
    private String motivoSalida;

    // ============================
    // SUSTENTO (RECIBO / CONTRATO)
    // ============================

    @Column(name = "tipo_sustento", length = 50)
    private String tipoSustento;

    @Column(name = "numero_recibo", length = 50)
    private String numeroRecibo;

    @Column(name = "monto_recibo", precision = 10, scale = 2)
    private BigDecimal montoRecibo;

    @Column(name = "fecha_emision_recibo")
    private LocalDate fechaEmisionRecibo;

    // ============================
    // ARCHIVO ADJUNTO
    // ============================

    @Column(name = "archivo_nombre", length = 200)
    private String archivoNombre;

    @Column(name = "archivo_ruta", length = 255)
    private String archivoRuta;

    @Column(name = "archivo_tipo", length = 100)
    private String archivoTipo;

    @Column(name = "fecha_subida")
    private LocalDateTime fechaSubida;

    // ============================
    // AUDITORÍA
    // ============================

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ============================
    // EVENTOS AUTOMÁTICOS
    // ============================

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (fechaSubida == null && archivoRuta != null && !archivoRuta.isBlank()) {
            fechaSubida = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        if (fechaSubida == null && archivoRuta != null && !archivoRuta.isBlank()) {
            fechaSubida = LocalDateTime.now();
        }
    }
}