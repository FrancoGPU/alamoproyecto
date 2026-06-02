package com.alamo.asistencia.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(
        name = "tb_cliente",
        uniqueConstraints = @UniqueConstraint(name = "uk_cliente_documento", columnNames = {"tipo_documento", "nro_documento"})
)
@Data
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 20)
    private TipoDocumentoCliente tipoDocumento;

    @Column(name = "nro_documento", nullable = false, length = 30)
    private String nroDocumento;

    @Column(name = "licencia_conducir_nro", nullable = false, length = 40)
    private String licenciaConducirNro;

    @Column(name = "licencia_conducir_vigencia", nullable = false)
    private LocalDate licenciaConducirVigencia;

    @Column(name = "pais_origen", nullable = false, length = 80)
    private String paisOrigen;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 120)
    private String apellidos;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "correo", length = 120)
    private String correo;

    @Column(name = "ruta_documento_identidad", length = 255)
    private String rutaDocumentoIdentidad;

    @Column(name = "ruta_licencia_conducir", length = 255)
    private String rutaLicenciaConducir;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ContratoAlquiler> contratos = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = fechaCreacion;
        normalizar();
    }

    @PreUpdate
    public void preUpdate() {
        fechaModificacion = LocalDateTime.now();
        normalizar();
    }

    private void normalizar() {
        if (nroDocumento != null) nroDocumento = nroDocumento.trim().toUpperCase();
        if (licenciaConducirNro != null) licenciaConducirNro = licenciaConducirNro.trim().toUpperCase();
        if (correo != null) correo = correo.trim().toLowerCase();
    }
}
