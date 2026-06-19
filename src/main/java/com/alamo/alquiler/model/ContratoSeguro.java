package com.alamo.alquiler.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_contrato_seguro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratoSeguro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato_seguro")
    private Integer idContratoSeguro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seguro", nullable = false)
    private Seguro seguro;

    @Column(name = "poliza", length = 100)
    private String poliza;
}

