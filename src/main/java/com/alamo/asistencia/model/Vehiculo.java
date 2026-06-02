package com.alamo.asistencia.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(
        name = "tb_vehiculo",
        uniqueConstraints = @UniqueConstraint(name = "uk_vehiculo_placa", columnNames = "placa")
)
@Data
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehiculo")
    private Integer idVehiculo;

    @Column(name = "placa", nullable = false, length = 12)
    private String placa;

    @Column(name = "marca", nullable = false, length = 60)
    private String marca;

    @Column(name = "modelo", nullable = false, length = 60)
    private String modelo;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "color", length = 40)
    private String color;

    @Column(name = "kilometraje", nullable = false)
    private Integer kilometraje = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoVehiculo estado = EstadoVehiculo.DISPONIBLE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria_vehiculo", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CategoriaVehiculo categoria;

    @OneToMany(mappedBy = "vehiculo", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ContratoAlquiler> contratos = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void normalizar() {
        if (placa != null) placa = placa.trim().toUpperCase();
        if (estado == null) estado = EstadoVehiculo.DISPONIBLE;
        if (kilometraje == null) kilometraje = 0;
    }
}
