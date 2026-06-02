package com.alamo.asistencia.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tb_usuario_idioma")
@Data
public class UsuarioIdioma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_idioma")
    private Integer idIdioma;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "idioma", nullable = false, length = 60)
    private String idioma;

    @Column(name = "nivel", length = 30)
    private String nivel;

    @Column(name = "certificacion", length = 120)
    private String certificacion;

    @Column(name = "institucion", length = 150)
    private String institucion;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "archivo_nombre", length = 200)
    private String archivoNombre;

    @Column(name = "archivo_ruta", length = 255)
    private String archivoRuta;

    @Column(name = "archivo_tipo", length = 100)
    private String archivoTipo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "fecha_subida")
    private LocalDateTime fechaSubida;

    @PrePersist
    protected void onCreate() {
        if (this.fechaSubida == null) {
            this.fechaSubida = LocalDateTime.now();
        }
    }
}