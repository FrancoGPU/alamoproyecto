package com.alamo.alquiler.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_categoria_vehiculo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;

    @Column(name = "tipo", nullable = false, unique = true, length = 120)
    private String tipo;

    @Column(name = "descripcion", length = 500)
    private String descripcion;
}

