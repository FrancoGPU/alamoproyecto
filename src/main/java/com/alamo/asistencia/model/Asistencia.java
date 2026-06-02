package com.alamo.asistencia.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tb_asistencia")
@Data
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asistencia")
    private Integer idAsistencia;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_entrada")
    private LocalTime horaEntrada;

    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    @Column(name = "ubicacion", length = 200)
    private String ubicacion;

    @Column(name = "horas_trabajadas")
    private BigDecimal horasTrabajadas;

    @Column(name = "minutos_tardanza")
    private Integer minutosTardanza = 0;

    // --- CAMPO AGREGADO PARA HORAS EXTRAS ---
    @Column(name = "minutos_extra")
    private Integer minutosExtra = 0; 

    @Column(name = "estado_asistencia", length = 50)
    private String estadoAsistencia; // PUNTUAL, TARDE, EXTRA

    @OneToMany(mappedBy = "asistencia", cascade = CascadeType.ALL)
    private List<Informe> informes;
}