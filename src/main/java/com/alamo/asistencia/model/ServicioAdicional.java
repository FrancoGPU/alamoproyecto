package com.alamo.asistencia.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "tb_servicio_adicional")
@Data
public class ServicioAdicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio_adicional")
    private Integer idServicioAdicional;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrato_alquiler", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ContratoAlquiler contrato;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad = 1;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @PrePersist
    public void prePersist() {
        if (cantidad == null || cantidad < 1) cantidad = 1;
    }
}
