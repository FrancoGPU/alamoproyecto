package com.alamo.alquiler.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_informe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Informe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_informe")
    private Integer idInforme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrato")
    private ContratoAlquiler contratoAlquiler;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_inspeccion")
    private InspeccionVehiculo inspeccionVehiculo;

    @Column(name = "fecha_informe", nullable = false)
    private LocalDateTime fechaInforme;

    @Column(name = "contenido", length = 4000)
    private String contenido;
}

