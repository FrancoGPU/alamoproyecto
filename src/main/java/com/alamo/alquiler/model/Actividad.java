package com.alamo.alquiler.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_actividad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actividad")
    private Integer idActividad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrato")
    private ContratoAlquiler contratoAlquiler;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_inspeccion")
    private InspeccionVehiculo inspeccionVehiculo;

    @Column(name = "fecha_actividad", nullable = false)
    private LocalDateTime fechaActividad;

    @Column(name = "descripcion", length = 2000)
    private String descripcion;
}

