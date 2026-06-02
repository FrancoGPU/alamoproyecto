package com.alamo.asistencia.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "tb_pago_garantia")
@Data
public class PagoGarantia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago_garantia")
    private Integer idPagoGarantia;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrato_alquiler", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ContratoAlquiler contrato;

    @Column(name = "monto_alquiler", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoAlquiler;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 30)
    private MetodoPago metodoPago;

    @Column(name = "monto_garantia_bloqueado", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoGarantiaBloqueado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_garantia", nullable = false, length = 20)
    private EstadoGarantia estadoGarantia = EstadoGarantia.BLOQUEADO;

    @Column(name = "referencia_pasarela", length = 120)
    private String referenciaPasarela;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Column(name = "fecha_liberacion")
    private LocalDateTime fechaLiberacion;

    @PrePersist
    public void prePersist() {
        if (fechaPago == null) fechaPago = LocalDateTime.now();
        if (estadoGarantia == null) estadoGarantia = EstadoGarantia.BLOQUEADO;
    }
}
