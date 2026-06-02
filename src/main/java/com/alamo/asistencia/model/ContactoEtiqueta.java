package com.alamo.asistencia.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tb_contacto_etiqueta")
@Data
@IdClass(ContactoEtiquetaId.class)
public class ContactoEtiqueta {

    @Id
    @Column(name = "id_contacto")
    private Integer id_contacto;

    @Id
    @Column(name = "id_etiqueta")
    private Integer id_etiqueta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contacto", insertable = false, updatable = false)
    private Contacto contacto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_etiqueta", insertable = false, updatable = false)
    private Etiqueta etiqueta;

    @Column(name = "date_create", nullable = false, updatable = false)
    private LocalDateTime date_create;

    @PrePersist
    void prePersist() {
        this.date_create = LocalDateTime.now();
    }
}
