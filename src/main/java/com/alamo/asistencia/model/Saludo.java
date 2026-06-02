package com.alamo.asistencia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_saludos")
@Data
public class Saludo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_saludo")
    private Integer idSaludo;

    // Relación con quien envía el mensaje (Emisor)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_emisor", referencedColumnName = "id_usuario", nullable = false)
    private Usuario emisor;

    // Relación con el cumpleañero (Receptor)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_receptor", referencedColumnName = "id_usuario", nullable = false)
    private Usuario receptor;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    // Campos estratégicos para el filtrado del Muro Mensual
    @Column(name = "mes_saludo", nullable = false)
    private Integer mesSaludo;

    @Column(name = "anio_saludo", nullable = false)
    private Integer anioSaludo;

    /**
     * Asegura que antes de insertar en la BD, se llenen los campos de tiempo.
     * Esto hace que tu tabla tb_saludos siempre tenga el mes y año correctos.
     */
    @PrePersist
    protected void onCreate() {
        if (this.fechaEnvio == null) {
            this.fechaEnvio = LocalDateTime.now();
        }
        // Extraemos mes y año de la fecha de envío final
        this.mesSaludo = this.fechaEnvio.getMonthValue();
        this.anioSaludo = this.fechaEnvio.getYear();
    }
}