package com.alamo.asistencia.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(
        name = "tb_contrato_alquiler",
        uniqueConstraints = @UniqueConstraint(name = "uk_contrato_alquiler_codigo", columnNames = "codigo_contrato")
)
@Data
public class ContratoAlquiler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato_alquiler")
    private Integer idContratoAlquiler;

    @Column(name = "codigo_contrato", nullable = false, length = 40)
    private String codigoContrato;

    @Column(name = "fecha_hora_pickup", nullable = false)
    private LocalDateTime fechaHoraPickup;

    @Column(name = "fecha_hora_dropoff", nullable = false)
    private LocalDateTime fechaHoraDropoff;

    @Column(name = "ubicacion_entrega", nullable = false, length = 150)
    private String ubicacionEntrega;

    @Column(name = "ubicacion_devolucion", length = 150)
    private String ubicacionDevolucion;

    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoContratoAlquiler estado = EstadoContratoAlquiler.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_agente", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario agente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vehiculo", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Vehiculo vehiculo;

    @OneToOne(mappedBy = "contrato", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PagoGarantia pagoGarantia;

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<InspeccionVehiculo> inspecciones = new ArrayList<>();

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ContratoSeguro> segurosContratados = new ArrayList<>();

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ServicioAdicional> serviciosAdicionales = new ArrayList<>();

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @PrePersist
    public void prePersist() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = fechaCreacion;
        if (estado == null) estado = EstadoContratoAlquiler.PENDIENTE;
        if (codigoContrato == null || codigoContrato.isBlank()) {
            codigoContrato = "ALQ-" + fechaCreacion.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}
