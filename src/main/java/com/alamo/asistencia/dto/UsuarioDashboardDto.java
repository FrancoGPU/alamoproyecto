package com.alamo.asistencia.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UsuarioDashboardDto {

    // =============================
    // IDENTIDAD
    // =============================
    private Integer idUsuario;
    private String nombreCompleto;

    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;

    private String dni;
    private String correo;
    private String telefono;
    private String direccion;

    private LocalDate fechaNacimiento;
    private String sexo;
    private String estadoCivil;
    private String nacionalidad;
    private String grupoSanguineo;

    // =============================
    // UBIGEO
    // =============================
    private Integer idDep;
    private Integer idProv;
    private Integer idDist;

    // =============================
    // CONTACTO EMERGENCIA
    // =============================
    private String telefonoEmergencia;
    private String nombreContactoEmergencia;
    private String relacionContactoEmergencia;

    // =============================
    // LABORAL
    // =============================
    private String cargo;
    private String rol;
    private String estado;

    private LocalDate fechaIngreso;
    private String tipoContrato;
    private String modalidad;
    private String numeroRuc;

    // =============================
    // BANCARIO / PAGOS
    // =============================
    private String bancoRaw;
    private String cuentaBancariaRaw;
    private String cuentaInterbancariaRaw;
    private String bancaMovilRaw;

    // =============================
    // OTROS
    // =============================
    private String tipoSeguro;
    private String idiomas;

    private String nivelEducativo;
    private String institucionesEducativas;
    private String certificacionesTexto;

    private LocalDate secundariaInicio;
    private LocalDate secundariaFin;
    private LocalDate universidadInicio;
    private LocalDate universidadFin;

    // =============================
    // AUDITORÍA
    // =============================
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    // =============================
    // DOCUMENTOS
    // =============================
    private String fotoPerfilUrl;
    private String cvUrl;
    private String dniFrontalUrl;
    private String dniTraseroUrl;

    private String cvNombre;
    private String dniFrontalNombre;
    private String dniTraseroNombre;

    private Boolean tieneCv;
    private Boolean tieneDniFrontal;
    private Boolean tieneDniTrasero;

    // =============================
    // ESTADO ASISTENCIA
    // =============================
    private String estadoAsistencia;

    // =============================
    // LISTAS
    // =============================
    private List<FormacionDto> formaciones;
    private List<CertificacionDto> certificaciones;
    private List<BancoDto> bancos;
    private List<MetodoPagoDto> metodosPago;
    private List<HorarioDto> horarios;

    // =====================================================
    // SUB DTOs
    // =====================================================

    @Data
    public static class FormacionDto {
        private Integer idFormacion;
        private String nivel;
        private String institucion;
        private String programa;
        private String estado;

        private LocalDate fechaInicio;
        private LocalDate fechaFin;
    }

    @Data
    public static class CertificacionDto {
        private Integer idCertificacion;

        private String nombre;
        private String entidad;
        private String codigo;
        private String horas;

        private LocalDate fechaObtencion;
        private LocalDate fechaVencimiento;
    }

    @Data
    public static class BancoDto {
        private String banco;
        private String titular;
        private String cuenta;
        private String cci;
    }

    @Data
    public static class MetodoPagoDto {
        private String metodo;
        private String numero;
        private String titular;
    }

    @Data
    public static class HorarioDto {
        private Integer dia;
        private String diaLabel;
        private String entrada;
        private String salida;
    }
}