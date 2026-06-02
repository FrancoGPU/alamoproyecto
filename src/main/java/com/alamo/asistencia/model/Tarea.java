package com.alamo.asistencia.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "tb_tarea")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idTarea")
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarea")
    private Integer idTarea;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // ================= CAMPOS DE SEGUIMIENTO =================

    @Column(name = "fase", length = 100)
    private String fase;

    @Column(name = "sub_actividad", length = 150)
    private String subActividad;

    @Column(name = "tipo_actividad", length = 100)
    private String tipoActividad;

    @Column(name = "entregable_nombre", length = 255)
    private String entregableNombre;

    @Column(name = "entregable_ruta", length = 500)
    private String entregableRuta;

    // ================= NUEVO: ACTIVIDAD (tb_actividad) =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividad")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "proyecto"})
    private Actividad actividad;

    // ================= RELACIONES =================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_creador", nullable = false)
    @JsonIgnoreProperties({"contrasenia", "objRol", "activo", "fotoPerfil"})
    private Usuario creador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_responsable", nullable = false)
    @JsonIgnoreProperties({"contrasenia", "objRol", "activo", "fotoPerfil", "direccion", "telefono"})
    private Usuario responsable;

    // ================= CAMPOS BASE =================

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "prioridad", nullable = false, length = 20)
    private String prioridad;

    @Column(name = "categoria", length = 50)
    private String categoria;

    @Column(name = "color", nullable = false, length = 20)
    private String color;

    // ================= ÁMBITO Y JERARQUÍA =================

    @Column(name = "ambito", length = 30)
    private String ambito;

    @Column(name = "es_proyecto", nullable = false)
    private Boolean esProyecto;

    @Column(name = "es_privada", nullable = false)
    private Boolean esPrivada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto_padre")
    @JsonIgnoreProperties({"subtareas", "creador", "responsable"})
    private Tarea proyectoPadre;

    @OneToMany(mappedBy = "proyectoPadre", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonIgnore
    private List<Tarea> subtareas;

    // ================= FECHAS =================

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    /**
     * ✅ Fecha solo día (DATE) para filtros por mes/día.
     * Recomendación práctica:
     * - Mientras migras registros antiguos, evita nullable=false aquí en JPA.
     * - La restricción fuerte la pones en la BD con DEFAULT y backfill.
     */
    @Column(name = "fecha_dia")
    private LocalDate fechaDia;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "fecha_inicio")
    @JsonProperty("fechaInicio")
    private LocalDateTime fechaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "fecha_limite")
    @JsonProperty("fechaLimite")
    private LocalDateTime fechaLimite;

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    // ================= CONTROL ADMIN =================

    @Column(name = "id_grupo_masivo", length = 50)
    private String idGrupoMasivo;

    @Column(name = "tipo_origen", length = 30)
    private String tipoOrigen;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    // ================= DEFAULTS SEGUROS =================

    @PrePersist
    public void prePersist() {
        if (activo == null) activo = true;

        if (estado == null || estado.isBlank()) estado = "PENDIENTE";
        else estado = estado.trim().toUpperCase(Locale.ROOT);

        if (prioridad == null || prioridad.isBlank()) prioridad = "MEDIA";
        else prioridad = prioridad.trim().toUpperCase(Locale.ROOT);

        if (color == null || color.isBlank()) color = "#0d52f2";
        else color = color.trim();

        if (ambito == null || ambito.isBlank()) ambito = "LISTA";
        else ambito = ambito.trim().toUpperCase(Locale.ROOT);

        if (esProyecto == null) esProyecto = false;
        if (esPrivada == null) esPrivada = false;

        if (fechaAsignacion == null) fechaAsignacion = LocalDateTime.now();

        // ✅ mantener fechaDia consistente
        if (fechaDia == null) fechaDia = fechaAsignacion.toLocalDate();

        if (titulo != null) titulo = titulo.trim();

        if (subActividad != null) subActividad = subActividad.trim().toUpperCase(Locale.ROOT);
        if (tipoActividad != null) tipoActividad = tipoActividad.trim().toUpperCase(Locale.ROOT);
        if (fase != null) fase = fase.trim().toUpperCase(Locale.ROOT);
    }

    @PreUpdate
    public void preUpdate() {
        if (estado != null) estado = estado.trim().toUpperCase(Locale.ROOT);
        if (prioridad != null) prioridad = prioridad.trim().toUpperCase(Locale.ROOT);
        if (ambito != null) ambito = ambito.trim().toUpperCase(Locale.ROOT);

        if (titulo != null) titulo = titulo.trim();

        if (subActividad != null) subActividad = subActividad.trim().toUpperCase(Locale.ROOT);
        if (tipoActividad != null) tipoActividad = tipoActividad.trim().toUpperCase(Locale.ROOT);
        if (fase != null) fase = fase.trim().toUpperCase(Locale.ROOT);

        if (color != null) color = color.trim();

        // ✅ auto-curativo: si falta fechaDia o si cambió fechaAsignacion
        if (fechaAsignacion != null && (fechaDia == null || !fechaDia.equals(fechaAsignacion.toLocalDate()))) {
            fechaDia = fechaAsignacion.toLocalDate();
        }
        if (fechaDia == null) {
            fechaDia = LocalDate.now();
        }
    }
}
