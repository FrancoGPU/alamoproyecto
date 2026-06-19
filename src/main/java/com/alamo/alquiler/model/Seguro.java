package com.alamo.alquiler.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_seguro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seguro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seguro")
    private Integer idSeguro;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private boolean activo;
}

