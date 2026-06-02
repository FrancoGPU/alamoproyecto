package com.alamo.asistencia.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_usuario_certificacion")
@Data
public class UsuarioCertificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_certificacion")
    private Integer idCertificacion;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "nombre", nullable = false, length = 180)
    private String nombre;

    @Column(name = "entidad", length = 180)
    private String entidad;

    @Column(name = "fecha_obtencion")
    private LocalDate fechaObtencion;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "codigo", length = 80)
    private String codigo;

    @Column(name = "horas", precision = 6, scale = 2)
    private BigDecimal horas;

    // ===============================
    // ARCHIVO ADJUNTO
    // ===============================
    @Column(name = "archivo_nombre", length = 200)
    private String archivoNombre;

    @Column(name = "archivo_ruta", length = 255)
    private String archivoRuta;

    @Column(name = "archivo_tipo", length = 100)
    private String archivoTipo;

    @Column(name = "fecha_subida")
    private LocalDateTime fechaSubida;

    // ===============================
    // AUDITORÍA
    // ===============================
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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