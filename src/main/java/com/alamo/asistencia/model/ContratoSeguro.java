package com.alamo.asistencia.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "tb_contrato_seguro")
@IdClass(ContratoSeguroId.class)
@Data
public class ContratoSeguro {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrato_alquiler", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ContratoAlquiler contrato;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_seguro", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Seguro seguro;

    @Column(name = "costo_dia_aplicado", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoDiaAplicado;
}
