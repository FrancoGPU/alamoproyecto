package com.alamo.asistencia.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tb_etiqueta")
@Data
public class Etiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_etiqueta")
    private Integer id_etiqueta;

    @Column(name = "nombre", nullable = false, length = 60, unique = true)
    private String nombre;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "descripcion", length = 120)
    private String descripcion;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    @Column(name = "date_create", nullable = false, updatable = false)
    private LocalDateTime date_create;

    @PrePersist
    void prePersist() {
        this.date_create = LocalDateTime.now();
    }
}
