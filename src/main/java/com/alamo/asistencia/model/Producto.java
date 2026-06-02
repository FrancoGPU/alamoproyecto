package com.alamo.asistencia.model;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "tb_producto")
@Data
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio", nullable = false)
    private Double precio;

    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    @Column(name = "foto_factura", length = 255)
    private String fotoFactura;

    @Column(name = "foto_producto", length = 255)
    private String fotoProducto;

    @Column(name = "codigo_factura", length = 50)
    private String codigoFactura;

    @Column(name = "tipo_transferencia", length = 50)
    private String tipoTransferencia;

    @Column(name = "propiedad", length = 100)
    private String propiedad;

    @Column(name = "comprador", length = 100)
    private String comprador;

    @Column(name = "local_destino", length = 100)
    private String localDestino;

    @Column(name = "responsable", length = 100)
    private String responsable;

    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    @Column(name = "descripcion_producto", length = 500)
    private String descripcionProducto;

    @Column(name = "estado_producto", length = 20)
    private String estadoProducto;

    @Column(name = "tiempo_uso", length = 100)
    private String tiempoUso;

    @Column(name = "fecha_asignacion")
    private LocalDate fechaAsignacion;

    @Column(name = "aplica_asignacion", length = 2)
    private String aplicaAsignacion;

    @Column(name = "emisor", length = 150)
    private String emisor;

    @Column(name = "cliente", length = 150)
    private String cliente;

    @Column(name = "numero_ruc", length = 20)
    private String numeroRuc;

    @Column(name = "garantia", length = 50)
    private String garantia;

    @Column(name = "descripcion_breve", length = 255)
    private String descripcionBreve;

    @ManyToOne(fetch = FetchType.LAZY) // ✅ Mejoramos rendimiento en el servidor
    @JoinColumn(name = "id_ingreso")
    @ToString.Exclude // ✅ EVITA EL BUCLE INFINITO (Error 500)
    @EqualsAndHashCode.Exclude // ✅ EVITA EL BUCLE INFINITO
    private Ingreso ingreso;
}