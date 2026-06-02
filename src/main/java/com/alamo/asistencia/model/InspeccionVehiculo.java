package com.alamo.asistencia.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "tb_inspeccion_vehiculo")
@Data
public class InspeccionVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inspeccion_vehiculo")
    private Integer idInspeccionVehiculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrato_alquiler", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ContratoAlquiler contrato;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_inspeccion", nullable = false, length = 20)
    private TipoInspeccionVehiculo tipoInspeccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_combustible", nullable = false, length = 20)
    private NivelCombustible nivelCombustible;

    @Column(name = "kilometraje_actual", nullable = false)
    private Integer kilometrajeActual;

    @Column(name = "danos_preexistentes", columnDefinition = "TEXT")
    private String danosPreexistentes;

    @Column(name = "accesorios", columnDefinition = "TEXT")
    private String accesorios;

    @Column(name = "firma_digital_cliente", length = 255)
    private String firmaDigitalCliente;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_inspeccion", nullable = false)
    private LocalDateTime fechaInspeccion;

    @PrePersist
    public void prePersist() {
        if (fechaInspeccion == null) fechaInspeccion = LocalDateTime.now();
    }
}
