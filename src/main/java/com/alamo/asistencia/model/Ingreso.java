package com.alamo.asistencia.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "tb_ingreso")
@Data
public class Ingreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ingreso")
    private Integer idIngreso;

    @Column(name = "mes", nullable = false)
    private LocalDate mes;

    @Column(name = "monto", nullable = false)
    private Double monto;

    // Regresamos a LAZY para evitar el error MultipleBagFetchException
    @OneToMany(mappedBy = "ingreso", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude 
    @EqualsAndHashCode.Exclude
    private List<Producto> productos = new ArrayList<>();

    @OneToMany(mappedBy = "ingreso", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude 
    @EqualsAndHashCode.Exclude
    private List<Servicio> servicios = new ArrayList<>();
}