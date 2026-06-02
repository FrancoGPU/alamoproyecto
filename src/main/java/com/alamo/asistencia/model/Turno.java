package com.alamo.asistencia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;

@Entity
@Table(name = "tb_turno")
@Data
public class Turno {
    
    @Id
    // SE ELIMINÓ @GeneratedValue porque vas a insertar IDs manuales (1 y 2)
    @Column(name = "id_turno")
    private Integer idTurno;

    @Column(name = "entrada", nullable = false)
    private LocalTime entrada;

    @Column(name = "salida", nullable = false)
    private LocalTime salida;
}