package com.alamo.asistencia.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(
        name = "tb_categoria_vehiculo",
        uniqueConstraints = @UniqueConstraint(name = "uk_categoria_vehiculo_tipo", columnNames = "tipo")
)
@Data
public class CategoriaVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria_vehiculo")
    private Integer idCategoriaVehiculo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoCategoriaVehiculo tipo;

    @Column(name = "tarifa_base_dia", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaBaseDia;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Vehiculo> vehiculos = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (activo == null) activo = true;
    }
}
