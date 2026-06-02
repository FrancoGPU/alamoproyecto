package com.alamo.asistencia.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class DiaControlDTO {
    private LocalDate fecha;

    private boolean descanso; // no trabaja según horario
    private boolean asistio;  // tiene asistencia ese día

    private String entrada;   // "HH:mm" o "-"
    private String salida;    // "HH:mm" o "-"
    private BigDecimal horasTrabajadas;

    private Integer minutosTardanza;
    private Integer minutosExtra;
}
