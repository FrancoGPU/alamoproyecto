package com.alamo.asistencia.model;

import java.time.LocalDateTime;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
    name = "tb_actividad",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_act", columnNames = {"id_proyecto", "nombre"})
    }
)
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actividad")
    private Integer idActividad;

    /**
     * Proyecto dueño de esta actividad (tb_tarea es_proyecto=1)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proyecto", nullable = false)
    @JsonIgnoreProperties({"subtareas", "creador", "responsable"})
    private Tarea proyecto;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "tipo_actividad", length = 100)
    private String tipoActividad;

    @Column(name = "fase", length = 100)
    private String fase;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (activo == null) activo = true;
        if (createdAt == null) createdAt = LocalDateTime.now();

        if (nombre != null) nombre = nombre.trim();
        if (tipoActividad != null) tipoActividad = tipoActividad.trim().toUpperCase(Locale.ROOT);
        if (fase != null) fase = fase.trim().toUpperCase(Locale.ROOT);
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();

        if (nombre != null) nombre = nombre.trim();
        if (tipoActividad != null) tipoActividad = tipoActividad.trim().toUpperCase(Locale.ROOT);
        if (fase != null) fase = fase.trim().toUpperCase(Locale.ROOT);
    }
}
