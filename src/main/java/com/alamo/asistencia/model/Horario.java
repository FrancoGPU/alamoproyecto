package com.alamo.asistencia.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tb_horario")
@Data
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario")
    private Integer idHorario;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario; // Relación con tu clase Usuario

    @ManyToOne
    @JoinColumn(name = "id_turno", nullable = false)
    private Turno turno; // Relación con la clase Turno de arriba

    @Column(name = "dia", nullable = false)
    private Integer dia; // 1=Lunes, 7=Domingo
}