package com.alamo.asistencia.model;

import java.io.Serializable;
import lombok.Data;

@Data
public class ContratoSeguroId implements Serializable {
    private Integer contrato;
    private Integer seguro;
}
