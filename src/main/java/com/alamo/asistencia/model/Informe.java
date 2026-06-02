package com.alamo.asistencia.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_informe")
@Data
@NoArgsConstructor // genera constructor vacío
public class Informe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_informe")
    private Integer idInforme;

    @ManyToOne
    @JoinColumn(name = "id_asistencia", nullable = true) // ahora puede ser null
    private Asistencia asistencia;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String rutaArchivo;

    // Constructor útil solo para nombre y ruta, sin asistencia
    public Informe(String nombreArchivo, String rutaArchivo) {
        this.nombreArchivo = nombreArchivo;
        this.rutaArchivo = rutaArchivo;
    }
}