package com.alamo.asistencia.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
    name = "tb_revision_informes",
    uniqueConstraints = @UniqueConstraint(name = "uq_usuario_periodo", columnNames = {"id_usuario", "anio", "mes"})
)
@Data
public class RevisionInforme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_revision")
    private Integer idRevision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "mes", nullable = false)
    private Integer mes;

    @Column(name = "revisado", nullable = false)
    private Boolean revisado = false;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}