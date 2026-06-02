package com.alamo.asistencia.model;

import java.io.Serializable;
import lombok.Data;

@Data
public class ContactoEtiquetaId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id_contacto;
    private Integer id_etiqueta;
}
