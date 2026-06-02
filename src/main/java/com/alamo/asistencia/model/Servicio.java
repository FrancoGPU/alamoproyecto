package com.alamo.asistencia.model;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "tb_servicio")
@Data
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Integer idServicio;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "transporte")
    private Double transporte; 

    @Column(name = "comida")
    private Double comida;

    @Column(name = "gasto_restaurante")
    private Double gastoRestaurante;

    @Column(name = "celulares")
    private Double celulares;

    @Column(name = "canva")
    private Double canva;

    @Column(name = "chatgpt")
    private Double chatgpt;

    @Column(name = "icloud")
    private Double icloud;

    @Column(name = "google_capa")
    private Double googleCapa;

    @Column(name = "onedrive")
    private Double oneDrive;

    @Column(name = "servidor")
    private Double servidor;

    @Column(name = "otros")
    private Double otros;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "comprador", length = 100)
    private String comprador;

    // ✅ NUEVO CAMPO PARA FOTO DEL COMPROBANTE
    @Column(name = "foto_comprobante", length = 255)
    private String fotoComprobante;

    // ✅ CORRECCIÓN: fetch LAZY y exclusiones de Lombok para evitar Error 500
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ingreso")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Ingreso ingreso;

    public double getTotal() {
        return (transporte != null ? transporte : 0.0)
             + (comida != null ? comida : 0.0)
             + (gastoRestaurante != null ? gastoRestaurante : 0.0)
             + (celulares != null ? celulares : 0.0)
             + (canva != null ? canva : 0.0)
             + (chatgpt != null ? chatgpt : 0.0)
             + (icloud != null ? icloud : 0.0)
             + (googleCapa != null ? googleCapa : 0.0)
             + (oneDrive != null ? oneDrive : 0.0)
             + (servidor != null ? servidor : 0.0)
             + (otros != null ? otros : 0.0);
    }

    public Double getSuscripcionesTotal() {
        return (canva != null ? canva : 0.0)
             + (chatgpt != null ? chatgpt : 0.0)
             + (icloud != null ? icloud : 0.0)
             + (googleCapa != null ? googleCapa : 0.0)
             + (oneDrive != null ? oneDrive : 0.0)
             + (servidor != null ? servidor : 0.0)
             + (otros != null ? otros : 0.0);
    }
}