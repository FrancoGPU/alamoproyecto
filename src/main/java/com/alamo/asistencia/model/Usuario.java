package com.alamo.asistencia.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "tb_usuario")
@Data
public class Usuario {

    // =======================
    // ID
    // =======================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    // =======================
    // DATOS PERSONALES
    // =======================
    @Column(name = "nombres", nullable = false, length = 60)
    private String nombres;

    @Column(name = "apellido_paterno", nullable = false, length = 60)
    private String apellido_paterno;

    @Column(name = "apellido_materno", nullable = false, length = 60)
    private String apellido_materno;

    @Column(name = "dni", nullable = false, length = 8, unique = true)
    private String dni;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_nacimiento")
    private LocalDate fecha_nacimiento;

    @Column(name = "correo", length = 80)
    private String correo;

    @Column(name = "direccion", length = 120)
    private String direccion;

    @Column(name = "referencia", length = 150)
    private String referencia;

    @Column(name = "coordenadas", length = 100)
    private String coordenadas;

    @Column(name = "telefono", length = 9)
    private String telefono;

    @Column(name = "sexo", length = 10)
    private String sexo;

    @Column(name = "estado_civil", length = 50)
    private String estado_civil;

    @Column(name = "nacionalidad", length = 50)
    private String nacionalidad;

    @Column(name = "grupo_sanguineo", length = 5)
    private String grupo_sanguineo;

    @Column(name = "biografia", length = 255)
    private String biografia;

    // =======================
    // UBIGEO
    // =======================
    @Column(name = "id_dep")
    private Integer id_dep;

    @Column(name = "id_prov")
    private Integer id_prov;

    @Column(name = "id_dist")
    private Integer id_dist;

    // =======================
    // CONTACTO(S) DE EMERGENCIA
    // =======================
    @Column(name = "telefono_emergencia", columnDefinition = "TEXT")
    private String telefono_emergencia;

    @Column(name = "nombre_contacto_emergencia", columnDefinition = "TEXT")
    private String nombre_contacto_emergencia;

    @Column(name = "relacion_contacto_emergencia", columnDefinition = "TEXT")
    private String relacion_contacto_emergencia;

    // =======================
    // DATOS LABORALES
    // =======================
    @Column(name = "cargo", length = 60)
    private String cargo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_ingreso")
    private LocalDate fecha_ingreso;

    @Column(name = "tipo_contrato", length = 50)
    private String tipo_contrato;

    @Column(name = "modalidad", length = 20)
    private String modalidad;

    @Column(name = "estado", length = 60, nullable = false)
    private String estado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol objRol;

    // =======================
    // CREDENCIALES
    // =======================
    @Column(name = "contrasenia", nullable = false, length = 255)
    private String contrasenia;

    // =======================
    // DATOS BANCARIOS
    // =======================
    @Column(name = "banco", columnDefinition = "TEXT")
    private String banco;

    @Column(name = "cuenta_bancaria", columnDefinition = "TEXT")
    private String cuenta_bancaria;

    @Column(name = "cuenta_interbancaria", columnDefinition = "TEXT")
    private String cuenta_interbancaria;

    @Column(name = "banca_movil", columnDefinition = "TEXT")
    private String banca_movil;

    @Column(name = "numero_ruc", length = 11)
    private String numero_ruc;

    // =======================
    // FORMACIÓN / SALUD (compatibilidad)
    // =======================
    @Column(name = "nivel_educativo", length = 50)
    private String nivel_educativo;

    @Column(name = "instituciones_educativas", columnDefinition = "TEXT")
    private String instituciones_educativas;

    @Column(name = "certificaciones", columnDefinition = "TEXT")
    private String certificaciones;

    @Column(name = "idiomas", columnDefinition = "TEXT")
    private String idiomas;

    @Column(name = "tipo_seguro", length = 50)
    private String tipo_seguro;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "secundaria_inicio")
    private LocalDate secundaria_inicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "secundaria_fin")
    private LocalDate secundaria_fin;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "universidad_inicio")
    private LocalDate universidad_inicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "universidad_fin")
    private LocalDate universidad_fin;

    // =======================
    // LISTAS (TABLAS RELACIONADAS)
    // =======================
    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaInicio ASC")
    private List<UsuarioFormacion> formaciones = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaObtencion DESC")
    private List<UsuarioCertificacion> certificacionesList = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaInicio DESC, fechaFin DESC")
    private List<UsuarioIdioma> idiomasList = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaInicio DESC, fechaFin DESC")
    private List<UsuarioExperiencia> experienciasList = new ArrayList<>();

    // =======================
    // ARCHIVOS / DOCUMENTOS
    // =======================
    @Column(name = "cv_nombre", length = 200)
    private String cv_nombre;

    @Column(name = "ruta_cv", length = 255)
    private String rutaCV;

    @Column(name = "foto_perfil", length = 255)
    private String fotoPerfil;

    @Column(name = "foto_id_nombre", length = 200)
    private String foto_id_nombre;

    @Column(name = "ruta_foto_id", length = 255)
    private String ruta_foto_id;

    @Column(name = "foto_id_nombre_trasera", length = 200)
    private String foto_id_nombre_trasera;

    @Column(name = "ruta_foto_id_trasera", length = 255)
    private String ruta_foto_id_trasera;

    // =======================
    // NUEVOS DOCUMENTOS
    // =======================
    @Column(name = "certijoven_nombre", length = 200)
    private String certijoven_nombre;

    @Column(name = "ruta_certijoven", length = 255)
    private String ruta_certijoven;

    @Column(name = "certiadulto_nombre", length = 200)
    private String certiadulto_nombre;

    @Column(name = "ruta_certiadulto", length = 255)
    private String ruta_certiadulto;

    @Column(name = "tiene_antecedentes", length = 2)
    private String tiene_antecedentes; // SI / NO

    @Column(name = "antecedentes_nombre", length = 200)
    private String antecedentes_nombre;

    @Column(name = "ruta_antecedentes", length = 255)
    private String ruta_antecedentes;

    @Column(name = "antecedentes_policiales_nombre", length = 200)
    private String antecedentes_policiales_nombre;

    @Column(name = "ruta_antecedentes_policiales", length = 255)
    private String ruta_antecedentes_policiales;

    // =======================
    // AUDITORÍA
    // =======================
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fecha_creacion;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "fecha_modificacion")
    private LocalDateTime fecha_modificacion;

    @PrePersist
    protected void onCreate() {
        this.fecha_creacion = LocalDateTime.now();
        this.fecha_modificacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.fecha_modificacion = LocalDateTime.now();
    }

    // =======================
    // MÉTODO UTILITARIO
    // =======================
    public String getNombreCompletoParaRuta() {
        String nombreLimpio = this.nombres != null ? this.nombres.trim().replace(" ", "_") : "";
        String apellidoPaternoLimpio = this.apellido_paterno != null ? this.apellido_paterno.trim().replace(" ", "_") : "";
        String apellidoMaternoLimpio = this.apellido_materno != null ? this.apellido_materno.trim().replace(" ", "_") : "";

        return String.join("_",
                nombreLimpio,
                apellidoPaternoLimpio,
                apellidoMaternoLimpio
        ).replaceAll("_+", "_").replaceAll("^_|_$", "");
    }
}