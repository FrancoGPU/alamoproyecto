package com.alamo.asistencia.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "tb_asistencia_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenciaAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_audit")
    private Long idAudit;

    @Column(name = "id_asistencia", nullable = false)
    private Integer idAsistencia;

    @Column(name = "accion", nullable = false, length = 50)
    private String accion;

    @Column(name = "entrada_antes")
    private LocalTime entradaAntes;

    @Column(name = "salida_antes")
    private LocalTime salidaAntes;

    @Column(name = "entrada_despues")
    private LocalTime entradaDespues;

    @Column(name = "salida_despues")
    private LocalTime salidaDespues;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_actor", nullable = false)
    private Usuario usuarioActor;

    @Column(name = "fecha_accion", nullable = false)
    private LocalDateTime fechaAccion;

    @Column(name = "ip_actor", length = 45)
    private String ipActor;
}