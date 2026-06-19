package com.alamo.alquiler.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_rol")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;

    @Column(name = "nombre", nullable = false, length = 80, unique = true)
    private String nombre;
}

