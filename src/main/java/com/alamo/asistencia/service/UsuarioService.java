package com.alamo.asistencia.service;

import com.alamo.asistencia.dto.UsuarioDashboardDto;
import com.alamo.asistencia.model.Asistencia;
import com.alamo.asistencia.model.Rol;
import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.model.UsuarioCertificacion;
import com.alamo.asistencia.model.UsuarioExperiencia;
import com.alamo.asistencia.model.UsuarioFormacion;
import com.alamo.asistencia.model.UsuarioIdioma;
import com.alamo.asistencia.repository.IAsistenciaRepository;
import com.alamo.asistencia.repository.IRolRepository;
import com.alamo.asistencia.repository.IUsuarioRepository;
import com.alamo.asistencia.repository.UsuarioCertificacionRepository;
import com.alamo.asistencia.repository.UsuarioExperienciaRepository;
import com.alamo.asistencia.repository.UsuarioFormacionRepository;
import com.alamo.asistencia.repository.UsuarioIdiomaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService {

    @Autowired private IUsuarioRepository usuarioRepository;
    @Autowired private IRolRepository rolRepository;
    @Autowired private IAsistenciaRepository asistenciaRepository;

    @Autowired private UsuarioFormacionRepository formacionRepo;
    @Autowired private UsuarioCertificacionRepository certRepo;
    @Autowired private UsuarioIdiomaRepository idiomaRepo;
    @Autowired private UsuarioExperienciaRepository experienciaRepo;

    @Value("${file.base-dir}")
    private String uploadDir;

    private static final String PROFILE_PHOTO_FOLDER = "fotodeperfil";
    private static final String IDIOMAS_FOLDER = "idiomas";
    private static final String CERTIFICACIONES_FOLDER = "certificaciones";
    private static final String EXPERIENCIAS_FOLDER = "experiencias";

    private static final String CERTIJOVEN_FOLDER = "certijoven";
    private static final String CERTIADULTO_FOLDER = "certiadulto";
    private static final String ANTECEDENTES_FOLDER = "antecedentes";

    private final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> obtenerUsuario(Integer id) {
        return usuarioRepository.findById(id);
    }

    public Optional<UsuarioIdioma> obtenerIdiomaPorId(Integer idIdioma) {
        return idiomaRepo.findById(idIdioma);
    }

    public Optional<UsuarioCertificacion> obtenerCertificacionPorId(Integer idCertificacion) {
        return certRepo.findById(idCertificacion);
    }

    public Optional<UsuarioExperiencia> obtenerExperienciaPorId(Integer idExperiencia) {
        return experienciaRepo.findById(idExperiencia);
    }

    @Transactional
    public Usuario guardarUsuario(Usuario usuario) {
        Integer rolIdForm = (usuario.getObjRol() != null) ? usuario.getObjRol().getId_rol() : null;

        if (usuario.getIdUsuario() == null) {
            if (usuario.getContrasenia() == null || usuario.getContrasenia().isEmpty()) {
                usuario.setContrasenia(usuario.getDni());
            }
            usuario.setFecha_creacion(ZonedDateTime.now(ZONA_LIMA).toLocalDateTime());

            Integer idRolAsignar = (rolIdForm != null && rolIdForm != 0) ? rolIdForm : 2;
            Rol rolAsignado = rolRepository.findById(idRolAsignar)
                    .orElseThrow(() -> new RuntimeException("Error: No se encontró el Rol con ID " + idRolAsignar));

            usuario.setObjRol(rolAsignado);

            if (usuario.getEstado() == null || usuario.getEstado().isEmpty()) usuario.setEstado("Activo");
            if (usuario.getCargo() == null || usuario.getCargo().isEmpty()) usuario.setCargo("N/A");

            usuario.setCorreo(emptyToNull(usuario.getCorreo()));
            usuario.setDireccion(emptyToNull(usuario.getDireccion()));
            usuario.setReferencia(emptyToNull(usuario.getReferencia()));
            usuario.setTelefono(emptyToNull(usuario.getTelefono()));
            usuario.setCuenta_bancaria(emptyToNull(usuario.getCuenta_bancaria()));
            usuario.setRutaCV(emptyToNull(usuario.getRutaCV()));

            String rucNuevo = emptyToNull(usuario.getNumero_ruc());
            if (rucNuevo != null && (rucNuevo.equals("-") || rucNuevo.isBlank())) {
                usuario.setNumero_ruc(null);
            } else {
                usuario.setNumero_ruc(rucNuevo);
            }

        } else {
            Usuario existente = usuarioRepository.findById(usuario.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException(
                            "Error al actualizar: Usuario con ID " + usuario.getIdUsuario() + " no encontrado."
                    ));

            if (usuario.getContrasenia() == null || usuario.getContrasenia().isEmpty()) {
                usuario.setContrasenia(existente.getContrasenia());
            }

            usuario.setFecha_creacion(existente.getFecha_creacion());

            if (usuario.getFotoPerfil() == null || usuario.getFotoPerfil().trim().isEmpty()) {
                usuario.setFotoPerfil(existente.getFotoPerfil());
            }

            if (rolIdForm != null && rolIdForm != 0) {
                rolRepository.findById(rolIdForm).ifPresent(usuario::setObjRol);
            } else {
                usuario.setObjRol(existente.getObjRol());
            }

            usuario.setFecha_modificacion(ZonedDateTime.now(ZONA_LIMA).toLocalDateTime());

            if (usuario.getEstado() == null || usuario.getEstado().trim().isEmpty()) {
                usuario.setEstado(existente.getEstado());
            }

            usuario.setCorreo(emptyToNull(usuario.getCorreo()));
            usuario.setDireccion(emptyToNull(usuario.getDireccion()));
            usuario.setReferencia(emptyToNull(usuario.getReferencia()));
            usuario.setTelefono(emptyToNull(usuario.getTelefono()));
            usuario.setCuenta_bancaria(emptyToNull(usuario.getCuenta_bancaria()));
            usuario.setRutaCV(emptyToNull(usuario.getRutaCV()));

            if (usuario.getCargo() != null && usuario.getCargo().isEmpty()) usuario.setCargo("N/A");

            String rucActualizar = emptyToNull(usuario.getNumero_ruc());
            if (rucActualizar != null && (rucActualizar.equals("-") || rucActualizar.isBlank())) {
                usuario.setNumero_ruc(null);
            } else {
                usuario.setNumero_ruc(rucActualizar);
            }
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizarPerfilBase(Integer userId, Usuario uForm) {
        Usuario uDb = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        uDb.setFecha_nacimiento(uForm.getFecha_nacimiento());
        uDb.setSexo(emptyToNull(uForm.getSexo()));
        uDb.setEstado_civil(emptyToNull(uForm.getEstado_civil()));
        uDb.setNacionalidad(emptyToNull(uForm.getNacionalidad()));
        uDb.setGrupo_sanguineo(emptyToNull(uForm.getGrupo_sanguineo()));

        uDb.setCorreo(emptyToNull(uForm.getCorreo()));
        uDb.setTelefono(emptyToNull(uForm.getTelefono()));
        uDb.setDireccion(emptyToNull(uForm.getDireccion()));
        uDb.setReferencia(emptyToNull(uForm.getReferencia()));
        uDb.setNombre_contacto_emergencia(emptyToNull(uForm.getNombre_contacto_emergencia()));
        uDb.setRelacion_contacto_emergencia(emptyToNull(uForm.getRelacion_contacto_emergencia()));
        uDb.setTelefono_emergencia(emptyToNull(uForm.getTelefono_emergencia()));

        uDb.setId_dep(uForm.getId_dep());
        uDb.setId_prov(uForm.getId_prov());
        uDb.setId_dist(uForm.getId_dist());

        uDb.setTipo_seguro(emptyToNull(uForm.getTipo_seguro()));
        uDb.setIdiomas(emptyToNull(uForm.getIdiomas()));

        uDb.setNivel_educativo(emptyToNull(uForm.getNivel_educativo()));
        uDb.setInstituciones_educativas(emptyToNull(uForm.getInstituciones_educativas()));
        uDb.setCertificaciones(emptyToNull(uForm.getCertificaciones()));
        uDb.setSecundaria_inicio(uForm.getSecundaria_inicio());
        uDb.setSecundaria_fin(uForm.getSecundaria_fin());
        uDb.setUniversidad_inicio(uForm.getUniversidad_inicio());
        uDb.setUniversidad_fin(uForm.getUniversidad_fin());

        uDb.setFecha_ingreso(uForm.getFecha_ingreso());
        uDb.setTipo_contrato(emptyToNull(uForm.getTipo_contrato()));
        uDb.setModalidad(emptyToNull(uForm.getModalidad()));

        String ruc = emptyToNull(uForm.getNumero_ruc());
        if (ruc != null && (ruc.equals("-") || ruc.isBlank())) ruc = null;
        uDb.setNumero_ruc(ruc);

        uDb.setBanco(emptyToNull(uForm.getBanco()));
        uDb.setCuenta_bancaria(emptyToNull(uForm.getCuenta_bancaria()));
        uDb.setCuenta_interbancaria(emptyToNull(uForm.getCuenta_interbancaria()));
        uDb.setBanca_movil(emptyToNull(uForm.getBanca_movil()));

        String tieneAntecedentes = emptyToNull(uForm.getTiene_antecedentes());
        if (tieneAntecedentes != null) {
            tieneAntecedentes = tieneAntecedentes.trim().toUpperCase();
            if (!"SI".equals(tieneAntecedentes) && !"NO".equals(tieneAntecedentes)) {
                tieneAntecedentes = null;
            }
        }
        uDb.setTiene_antecedentes(tieneAntecedentes);

        uDb.setFecha_modificacion(ZonedDateTime.now(ZONA_LIMA).toLocalDateTime());

        return usuarioRepository.save(uDb);
    }

    @Transactional
    public Usuario actualizarPerfilTodo(
            Integer userId,
            Usuario uForm,
            MultipartFile[] idiomasFiles,
            MultipartFile[] certificacionesFiles,
            MultipartFile[] experienciasFiles,
            MultipartFile certijovenFile,
            MultipartFile certiadultoFile,
            MultipartFile antecedentesFile
    ) {
        Usuario uDb = actualizarPerfilBase(userId, uForm);

        boolean llegaronFormaciones = hasAnyFormacion(uForm);
        boolean llegaronCerts = hasAnyCertificacion(uForm);
        boolean llegaronIdiomas = hasAnyIdioma(uForm);
        boolean llegaronExperiencias = hasAnyExperiencia(uForm);

        if (llegaronFormaciones) {
            formacionRepo.deleteByUsuario_IdUsuario(userId);

            List<UsuarioFormacion> formacionesForm = uForm.getFormaciones() != null
                    ? uForm.getFormaciones()
                    : Collections.emptyList();

            for (UsuarioFormacion f : formacionesForm) {
                if (f == null) continue;

                String inst = emptyToNull(f.getInstitucion());
                if (inst == null) continue;
                if (f.getNivel() == null) continue;

                UsuarioFormacion nuevo = new UsuarioFormacion();
                nuevo.setUsuario(uDb);
                nuevo.setNivel(f.getNivel());
                nuevo.setInstitucion(inst);
                nuevo.setPrograma(emptyToNull(f.getPrograma()));
                nuevo.setEstado(f.getEstado() != null ? f.getEstado() : UsuarioFormacion.EstadoFormacion.COMPLETO);
                nuevo.setFechaInicio(f.getFechaInicio());

                if (nuevo.getEstado() == UsuarioFormacion.EstadoFormacion.EN_CURSO) {
                    nuevo.setFechaFin(null);
                } else {
                    nuevo.setFechaFin(f.getFechaFin());
                }

                formacionRepo.save(nuevo);
            }
        }

        if (llegaronCerts) {
            List<UsuarioCertificacion> existentes = certRepo.findByUsuario_IdUsuario(userId);
            Map<Integer, UsuarioCertificacion> existentesMap = new HashMap<>();
            for (UsuarioCertificacion ex : existentes) {
                if (ex.getIdCertificacion() != null) {
                    existentesMap.put(ex.getIdCertificacion(), ex);
                }
            }

            certRepo.deleteByUsuario_IdUsuario(userId);

            List<UsuarioCertificacion> certsForm = uForm.getCertificacionesList() != null
                    ? uForm.getCertificacionesList()
                    : Collections.emptyList();

            for (int i = 0; i < certsForm.size(); i++) {
                UsuarioCertificacion c = certsForm.get(i);
                if (c == null) continue;

                String nombre = emptyToNull(c.getNombre());
                if (nombre == null) continue;

                UsuarioCertificacion nuevo = new UsuarioCertificacion();
                nuevo.setUsuario(uDb);
                nuevo.setNombre(nombre);
                nuevo.setEntidad(emptyToNull(c.getEntidad()));
                nuevo.setCodigo(emptyToNull(c.getCodigo()));
                nuevo.setFechaObtencion(c.getFechaObtencion());
                nuevo.setFechaVencimiento(c.getFechaVencimiento());
                nuevo.setHoras(c.getHoras());

                UsuarioCertificacion anterior = null;
                if (c.getIdCertificacion() != null) {
                    anterior = existentesMap.get(c.getIdCertificacion());
                }

                MultipartFile archivo = getFileAt(certificacionesFiles, i);

                if (archivo != null && !archivo.isEmpty()) {
                    ArchivoGuardado archivoGuardado = guardarArchivoCertificacion(uDb, archivo);
                    nuevo.setArchivoNombre(archivoGuardado.nombreOriginal());
                    nuevo.setArchivoRuta(archivoGuardado.rutaRelativa());
                    nuevo.setArchivoTipo(archivoGuardado.contentType());
                    nuevo.setFechaSubida(LocalDateTime.now());
                } else if (anterior != null) {
                    nuevo.setArchivoNombre(anterior.getArchivoNombre());
                    nuevo.setArchivoRuta(anterior.getArchivoRuta());
                    nuevo.setArchivoTipo(anterior.getArchivoTipo());
                    nuevo.setFechaSubida(anterior.getFechaSubida());
                }

                certRepo.save(nuevo);
            }
        }

        if (llegaronIdiomas) {
            List<UsuarioIdioma> existentes = idiomaRepo.findByUsuario_IdUsuario(userId);
            Map<Integer, UsuarioIdioma> existentesMap = new HashMap<>();
            for (UsuarioIdioma ex : existentes) {
                if (ex.getIdIdioma() != null) {
                    existentesMap.put(ex.getIdIdioma(), ex);
                }
            }

            idiomaRepo.deleteByUsuario_IdUsuario(userId);

            List<UsuarioIdioma> idiomasForm = uForm.getIdiomasList() != null
                    ? uForm.getIdiomasList()
                    : Collections.emptyList();

            for (int i = 0; i < idiomasForm.size(); i++) {
                UsuarioIdioma idi = idiomasForm.get(i);
                if (idi == null) continue;

                String idioma = emptyToNull(idi.getIdioma());
                if (idioma == null) continue;

                UsuarioIdioma nuevo = new UsuarioIdioma();
                nuevo.setUsuario(uDb);
                nuevo.setIdioma(idioma);
                nuevo.setNivel(emptyToNull(idi.getNivel()));
                nuevo.setCertificacion(emptyToNull(idi.getCertificacion()));
                nuevo.setInstitucion(emptyToNull(idi.getInstitucion()));
                nuevo.setFechaInicio(idi.getFechaInicio());
                nuevo.setFechaFin(idi.getFechaFin());

                UsuarioIdioma anterior = null;
                if (idi.getIdIdioma() != null) {
                    anterior = existentesMap.get(idi.getIdIdioma());
                }

                MultipartFile archivo = getFileAt(idiomasFiles, i);

                if (archivo != null && !archivo.isEmpty()) {
                    ArchivoGuardado archivoGuardado = guardarArchivoIdioma(uDb, archivo);
                    nuevo.setArchivoNombre(archivoGuardado.nombreOriginal());
                    nuevo.setArchivoRuta(archivoGuardado.rutaRelativa());
                    nuevo.setArchivoTipo(archivoGuardado.contentType());
                    nuevo.setFechaSubida(LocalDateTime.now());
                } else if (anterior != null) {
                    nuevo.setArchivoNombre(anterior.getArchivoNombre());
                    nuevo.setArchivoRuta(anterior.getArchivoRuta());
                    nuevo.setArchivoTipo(anterior.getArchivoTipo());
                    nuevo.setFechaSubida(anterior.getFechaSubida());
                }

                idiomaRepo.save(nuevo);
            }
        }

        if (llegaronExperiencias) {
            List<UsuarioExperiencia> existentes = experienciaRepo.findByUsuario_IdUsuario(userId);
            Map<Integer, UsuarioExperiencia> existentesMap = new HashMap<>();
            for (UsuarioExperiencia ex : existentes) {
                if (ex.getIdExperiencia() != null) {
                    existentesMap.put(ex.getIdExperiencia(), ex);
                }
            }

            experienciaRepo.deleteByUsuario_IdUsuario(userId);

            List<UsuarioExperiencia> experienciasForm = uForm.getExperienciasList() != null
                    ? uForm.getExperienciasList()
                    : Collections.emptyList();

            for (int i = 0; i < experienciasForm.size(); i++) {
                UsuarioExperiencia exp = experienciasForm.get(i);
                if (exp == null) continue;

                String empresa = emptyToNull(exp.getEmpresa());
                if (empresa == null) continue;

                UsuarioExperiencia nuevo = new UsuarioExperiencia();
                nuevo.setUsuario(uDb);

                nuevo.setEmpresa(empresa);
                nuevo.setPuesto(emptyToNull(exp.getPuesto()));
                nuevo.setFechaInicio(exp.getFechaInicio());
                nuevo.setFechaFin(exp.getFechaFin());

                nuevo.setNombreReferencia(emptyToNull(exp.getNombreReferencia()));
                nuevo.setTelefonoReferencia(emptyToNull(exp.getTelefonoReferencia()));
                nuevo.setCorreoReferencia(emptyToNull(exp.getCorreoReferencia()));
                nuevo.setMotivoSalida(emptyToNull(exp.getMotivoSalida()));

                nuevo.setTipoSustento(emptyToNull(exp.getTipoSustento()));
                nuevo.setNumeroRecibo(emptyToNull(exp.getNumeroRecibo()));
                nuevo.setMontoRecibo(exp.getMontoRecibo());
                nuevo.setFechaEmisionRecibo(exp.getFechaEmisionRecibo());

                UsuarioExperiencia anterior = null;
                if (exp.getIdExperiencia() != null) {
                    anterior = existentesMap.get(exp.getIdExperiencia());
                }

                MultipartFile archivo = getFileAt(experienciasFiles, i);

                if (archivo != null && !archivo.isEmpty()) {
                    ArchivoGuardado archivoGuardado = guardarArchivoExperiencia(uDb, archivo);
                    nuevo.setArchivoNombre(archivoGuardado.nombreOriginal());
                    nuevo.setArchivoRuta(archivoGuardado.rutaRelativa());
                    nuevo.setArchivoTipo(archivoGuardado.contentType());
                    nuevo.setFechaSubida(LocalDateTime.now());
                } else if (anterior != null) {
                    nuevo.setArchivoNombre(anterior.getArchivoNombre());
                    nuevo.setArchivoRuta(anterior.getArchivoRuta());
                    nuevo.setArchivoTipo(anterior.getArchivoTipo());
                    nuevo.setFechaSubida(anterior.getFechaSubida());
                }

                experienciaRepo.save(nuevo);
            }
        }

        if (certijovenFile != null && !certijovenFile.isEmpty()) {
            ArchivoGuardado archivoGuardado = guardarArchivoCertijoven(uDb, certijovenFile);
            uDb.setCertijoven_nombre(archivoGuardado.nombreOriginal());
            uDb.setRuta_certijoven(archivoGuardado.rutaRelativa());
        }

        if (certiadultoFile != null && !certiadultoFile.isEmpty()) {
            ArchivoGuardado archivoGuardado = guardarArchivoCertiadulto(uDb, certiadultoFile);
            uDb.setCertiadulto_nombre(archivoGuardado.nombreOriginal());
            uDb.setRuta_certiadulto(archivoGuardado.rutaRelativa());
        }

        if ("SI".equalsIgnoreCase(uDb.getTiene_antecedentes())) {
            if (antecedentesFile != null && !antecedentesFile.isEmpty()) {
                ArchivoGuardado archivoGuardado = guardarArchivoAntecedentes(uDb, antecedentesFile);
                uDb.setAntecedentes_nombre(archivoGuardado.nombreOriginal());
                uDb.setRuta_antecedentes(archivoGuardado.rutaRelativa());
            }
        } else {
            uDb.setAntecedentes_nombre(null);
            uDb.setRuta_antecedentes(null);
        }

        uDb.setFecha_modificacion(ZonedDateTime.now(ZONA_LIMA).toLocalDateTime());
        usuarioRepository.save(uDb);

        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
    }

    @Transactional(readOnly = true)
    public UsuarioDashboardDto obtenerDashboard(Integer idUsuario) {
        Usuario u = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + idUsuario));

        UsuarioDashboardDto dto = new UsuarioDashboardDto();

        dto.setIdUsuario(u.getIdUsuario());
        dto.setNombres(u.getNombres());
        dto.setApellidoPaterno(u.getApellido_paterno());
        dto.setApellidoMaterno(u.getApellido_materno());
        dto.setNombreCompleto(
                (safe(u.getNombres()) + " " + safe(u.getApellido_paterno()) + " " + safe(u.getApellido_materno())).trim()
        );

        dto.setDni(u.getDni());
        dto.setCorreo(u.getCorreo());
        dto.setTelefono(u.getTelefono());
        dto.setDireccion(u.getDireccion());

        dto.setFechaNacimiento(u.getFecha_nacimiento());
        dto.setSexo(u.getSexo());
        dto.setEstadoCivil(u.getEstado_civil());
        dto.setNacionalidad(u.getNacionalidad());
        dto.setGrupoSanguineo(u.getGrupo_sanguineo());

        dto.setIdDep(u.getId_dep());
        dto.setIdProv(u.getId_prov());
        dto.setIdDist(u.getId_dist());

        dto.setTelefonoEmergencia(u.getTelefono_emergencia());
        dto.setNombreContactoEmergencia(u.getNombre_contacto_emergencia());
        dto.setRelacionContactoEmergencia(u.getRelacion_contacto_emergencia());

        dto.setCargo(u.getCargo());
        dto.setRol(u.getObjRol() != null ? u.getObjRol().getDescripcion() : null);
        dto.setEstado(u.getEstado());

        dto.setFechaIngreso(u.getFecha_ingreso());
        dto.setTipoContrato(u.getTipo_contrato());
        dto.setModalidad(u.getModalidad());
        dto.setNumeroRuc(u.getNumero_ruc());

        dto.setTipoSeguro(u.getTipo_seguro());
        dto.setIdiomas(u.getIdiomas());

        dto.setNivelEducativo(u.getNivel_educativo());
        dto.setInstitucionesEducativas(u.getInstituciones_educativas());
        dto.setCertificacionesTexto(u.getCertificaciones());

        dto.setSecundariaInicio(u.getSecundaria_inicio());
        dto.setSecundariaFin(u.getSecundaria_fin());
        dto.setUniversidadInicio(u.getUniversidad_inicio());
        dto.setUniversidadFin(u.getUniversidad_fin());

        dto.setFechaCreacion(u.getFecha_creacion());
        dto.setFechaModificacion(u.getFecha_modificacion());

        dto.setFotoPerfilUrl(
                (u.getFotoPerfil() != null && !u.getFotoPerfil().isBlank())
                        ? "/perfil/foto/" + u.getFotoPerfil()
                        : "/img/admin.png"
        );

        dto.setCvUrl(u.getRutaCV() != null ? "/cv/descargar/" + u.getIdUsuario() : null);
        dto.setDniFrontalUrl(u.getRuta_foto_id() != null ? "/cv/foto-id-frontal/descargar/" + u.getIdUsuario() : null);
        dto.setDniTraseroUrl(u.getRuta_foto_id_trasera() != null ? "/cv/foto-id-trasera/descargar/" + u.getIdUsuario() : null);

        dto.setCvNombre(u.getCv_nombre());
        dto.setDniFrontalNombre(u.getFoto_id_nombre());
        dto.setDniTraseroNombre(u.getFoto_id_nombre_trasera());

        dto.setTieneCv(u.getRutaCV() != null && !u.getRutaCV().isBlank());
        dto.setTieneDniFrontal(u.getRuta_foto_id() != null && !u.getRuta_foto_id().isBlank());
        dto.setTieneDniTrasero(u.getRuta_foto_id_trasera() != null && !u.getRuta_foto_id_trasera().isBlank());

        dto.setEstadoAsistencia(obtenerEstadoAsistenciaActual(u.getIdUsuario()));

        dto.setBancoRaw(u.getBanco());
        dto.setCuentaBancariaRaw(u.getCuenta_bancaria());
        dto.setCuentaInterbancariaRaw(u.getCuenta_interbancaria());
        dto.setBancaMovilRaw(u.getBanca_movil());

        List<UsuarioFormacion> formaciones = formacionRepo.findByUsuario_IdUsuario(u.getIdUsuario());
        List<UsuarioDashboardDto.FormacionDto> formDtos = new ArrayList<>();
        for (UsuarioFormacion f : formaciones) {
            UsuarioDashboardDto.FormacionDto fd = new UsuarioDashboardDto.FormacionDto();
            fd.setIdFormacion(f.getIdFormacion());
            fd.setNivel(f.getNivel() != null ? f.getNivel().name() : null);
            fd.setInstitucion(f.getInstitucion());
            fd.setPrograma(f.getPrograma());
            fd.setEstado(f.getEstado() != null ? f.getEstado().name() : null);
            fd.setFechaInicio(f.getFechaInicio());
            fd.setFechaFin(f.getFechaFin());
            formDtos.add(fd);
        }
        dto.setFormaciones(formDtos);

        List<UsuarioCertificacion> certs = certRepo.findByUsuario_IdUsuario(u.getIdUsuario());
        List<UsuarioDashboardDto.CertificacionDto> certDtos = new ArrayList<>();
        for (UsuarioCertificacion c : certs) {
            UsuarioDashboardDto.CertificacionDto cd = new UsuarioDashboardDto.CertificacionDto();
            cd.setIdCertificacion(c.getIdCertificacion());
            cd.setNombre(c.getNombre());
            cd.setEntidad(c.getEntidad());
            cd.setCodigo(c.getCodigo());
            cd.setFechaObtencion(c.getFechaObtencion());
            cd.setFechaVencimiento(c.getFechaVencimiento());
            cd.setHoras(formatHoras(c.getHoras()));
            certDtos.add(cd);
        }
        dto.setCertificaciones(certDtos);

        dto.setBancos(parseBancos(u.getBanco(), u.getCuenta_bancaria(), u.getCuenta_interbancaria()));
        dto.setMetodosPago(parseMovil(u.getBanca_movil()));
        dto.setHorarios(new ArrayList<>());

        return dto;
    }

    private String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private boolean hasAnyFormacion(Usuario uForm) {
        if (uForm == null || uForm.getFormaciones() == null) return false;
        return uForm.getFormaciones().stream().anyMatch(f ->
                f != null && emptyToNull(f.getInstitucion()) != null
        );
    }

    private boolean hasAnyCertificacion(Usuario uForm) {
        if (uForm == null || uForm.getCertificacionesList() == null) return false;
        return uForm.getCertificacionesList().stream().anyMatch(c ->
                c != null && emptyToNull(c.getNombre()) != null
        );
    }

    private boolean hasAnyIdioma(Usuario uForm) {
        if (uForm == null || uForm.getIdiomasList() == null) return false;
        return uForm.getIdiomasList().stream().anyMatch(i ->
                i != null && emptyToNull(i.getIdioma()) != null
        );
    }

    private boolean hasAnyExperiencia(Usuario uForm) {
        if (uForm == null || uForm.getExperienciasList() == null) return false;
        return uForm.getExperienciasList().stream().anyMatch(e ->
                e != null && emptyToNull(e.getEmpresa()) != null
        );
    }

    private MultipartFile getFileAt(MultipartFile[] files, int index) {
        if (files == null) return null;
        if (index < 0 || index >= files.length) return null;
        return files[index];
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private String formatHoras(BigDecimal horas) {
        if (horas == null) return null;
        return horas.stripTrailingZeros().toPlainString();
    }

    private List<String> splitLines(String v) {
        String s = (v == null) ? "" : v.trim();
        if (s.isEmpty()) return Collections.emptyList();

        String[] arr = s.split("\\r?\\n");
        List<String> out = new ArrayList<>();
        for (String x : arr) {
            String t = (x == null) ? "" : x.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    private List<UsuarioDashboardDto.BancoDto> parseBancos(String bancoTxt, String cuentaTxt, String cciTxt) {
        List<String> bancoLines = splitLines(bancoTxt);
        List<String> cuentaLines = splitLines(cuentaTxt);
        List<String> cciLines = splitLines(cciTxt);

        int max = Math.max(bancoLines.size(), Math.max(cuentaLines.size(), cciLines.size()));
        List<UsuarioDashboardDto.BancoDto> out = new ArrayList<>();

        for (int i = 0; i < max; i++) {
            String bt = (i < bancoLines.size() ? bancoLines.get(i) : "");
            String cuenta = (i < cuentaLines.size() ? cuentaLines.get(i) : "");
            String cci = (i < cciLines.size() ? cciLines.get(i) : "");

            String banco = "";
            String titular = "";

            if (bt != null && !bt.isBlank()) {
                String[] parts = bt.split("\\|\\|", -1);
                banco = parts.length > 0 ? parts[0].trim() : "";
                titular = parts.length > 1 ? parts[1].trim() : "";
            }

            if ((banco + titular + cuenta + cci).trim().isEmpty()) continue;

            UsuarioDashboardDto.BancoDto dto = new UsuarioDashboardDto.BancoDto();
            dto.setBanco(emptyToNull(banco));
            dto.setTitular(emptyToNull(titular));
            dto.setCuenta(emptyToNull(cuenta));
            dto.setCci(emptyToNull(cci));
            out.add(dto);
        }

        return out;
    }

    private List<UsuarioDashboardDto.MetodoPagoDto> parseMovil(String movilTxt) {
        List<String> lines = splitLines(movilTxt);
        List<UsuarioDashboardDto.MetodoPagoDto> out = new ArrayList<>();

        for (String line : lines) {
            if (line == null || line.isBlank()) continue;

            String[] parts = line.split("\\|\\|", -1);

            String metodo = parts.length > 0 ? parts[0].trim() : "";
            String numero = parts.length > 1 ? parts[1].trim() : "";
            String titular = parts.length > 2 ? parts[2].trim() : "";

            if ((metodo + numero + titular).trim().isEmpty()) continue;

            UsuarioDashboardDto.MetodoPagoDto dto = new UsuarioDashboardDto.MetodoPagoDto();
            dto.setMetodo(emptyToNull(metodo));
            dto.setNumero(emptyToNull(numero));
            dto.setTitular(emptyToNull(titular));
            out.add(dto);
        }

        return out;
    }

    private ArchivoGuardado guardarArchivoIdioma(Usuario usuario, MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) return null;

            validarArchivoAdjunto(file, "idioma");

            String contentType = file.getContentType();
            if (contentType == null) contentType = "application/octet-stream";

            String originalFilename = file.getOriginalFilename();
            String extension = obtenerExtension(originalFilename);

            Path carpetaUsuario = Paths.get(uploadDir, IDIOMAS_FOLDER, "usuario-" + usuario.getIdUsuario()).normalize();
            Files.createDirectories(carpetaUsuario);

            String nombreGuardado = UUID.randomUUID() + extension;
            Path destino = carpetaUsuario.resolve(nombreGuardado).normalize();

            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            return new ArchivoGuardado(
                    originalFilename,
                    Paths.get("usuario-" + usuario.getIdUsuario(), nombreGuardado).toString().replace("\\", "/"),
                    contentType
            );

        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el archivo del idioma: " + e.getMessage(), e);
        }
    }

    private ArchivoGuardado guardarArchivoCertificacion(Usuario usuario, MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) return null;

            validarArchivoAdjunto(file, "certificación");

            String contentType = file.getContentType();
            if (contentType == null) contentType = "application/octet-stream";

            String originalFilename = file.getOriginalFilename();
            String extension = obtenerExtension(originalFilename);

            Path carpetaUsuario = Paths.get(uploadDir, CERTIFICACIONES_FOLDER, "usuario-" + usuario.getIdUsuario()).normalize();
            Files.createDirectories(carpetaUsuario);

            String nombreGuardado = UUID.randomUUID() + extension;
            Path destino = carpetaUsuario.resolve(nombreGuardado).normalize();

            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            return new ArchivoGuardado(
                    originalFilename,
                    Paths.get("usuario-" + usuario.getIdUsuario(), nombreGuardado).toString().replace("\\", "/"),
                    contentType
            );

        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el archivo de la certificación: " + e.getMessage(), e);
        }
    }

    private ArchivoGuardado guardarArchivoExperiencia(Usuario usuario, MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) return null;

            validarArchivoAdjunto(file, "experiencia");

            String contentType = file.getContentType();
            if (contentType == null) contentType = "application/octet-stream";

            String originalFilename = file.getOriginalFilename();
            String extension = obtenerExtension(originalFilename);

            Path carpetaUsuario = Paths.get(uploadDir, EXPERIENCIAS_FOLDER, "usuario-" + usuario.getIdUsuario()).normalize();
            Files.createDirectories(carpetaUsuario);

            String nombreGuardado = UUID.randomUUID() + extension;
            Path destino = carpetaUsuario.resolve(nombreGuardado).normalize();

            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            return new ArchivoGuardado(
                    originalFilename,
                    Paths.get("usuario-" + usuario.getIdUsuario(), nombreGuardado).toString().replace("\\", "/"),
                    contentType
            );

        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el archivo de experiencia: " + e.getMessage(), e);
        }
    }

    private ArchivoGuardado guardarArchivoCertijoven(Usuario usuario, MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) return null;

            validarArchivoAdjunto(file, "certijoven");

            String contentType = file.getContentType();
            if (contentType == null) contentType = "application/octet-stream";

            String originalFilename = file.getOriginalFilename();
            String extension = obtenerExtension(originalFilename);

            Path carpetaUsuario = Paths.get(uploadDir, CERTIJOVEN_FOLDER, "usuario-" + usuario.getIdUsuario()).normalize();
            Files.createDirectories(carpetaUsuario);

            String nombreGuardado = UUID.randomUUID() + extension;
            Path destino = carpetaUsuario.resolve(nombreGuardado).normalize();

            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            return new ArchivoGuardado(
                    originalFilename,
                    Paths.get("usuario-" + usuario.getIdUsuario(), nombreGuardado).toString().replace("\\", "/"),
                    contentType
            );

        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el archivo de certijoven: " + e.getMessage(), e);
        }
    }

    private ArchivoGuardado guardarArchivoCertiadulto(Usuario usuario, MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) return null;

            validarArchivoAdjunto(file, "certiadulto");

            String contentType = file.getContentType();
            if (contentType == null) contentType = "application/octet-stream";

            String originalFilename = file.getOriginalFilename();
            String extension = obtenerExtension(originalFilename);

            Path carpetaUsuario = Paths.get(uploadDir, CERTIADULTO_FOLDER, "usuario-" + usuario.getIdUsuario()).normalize();
            Files.createDirectories(carpetaUsuario);

            String nombreGuardado = UUID.randomUUID() + extension;
            Path destino = carpetaUsuario.resolve(nombreGuardado).normalize();

            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            return new ArchivoGuardado(
                    originalFilename,
                    Paths.get("usuario-" + usuario.getIdUsuario(), nombreGuardado).toString().replace("\\", "/"),
                    contentType
            );

        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el archivo de certiadulto: " + e.getMessage(), e);
        }
    }

    private ArchivoGuardado guardarArchivoAntecedentes(Usuario usuario, MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) return null;

            validarArchivoAdjunto(file, "antecedentes");

            String contentType = file.getContentType();
            if (contentType == null) contentType = "application/octet-stream";

            String originalFilename = file.getOriginalFilename();
            String extension = obtenerExtension(originalFilename);

            Path carpetaUsuario = Paths.get(uploadDir, ANTECEDENTES_FOLDER, "usuario-" + usuario.getIdUsuario()).normalize();
            Files.createDirectories(carpetaUsuario);

            String nombreGuardado = UUID.randomUUID() + extension;
            Path destino = carpetaUsuario.resolve(nombreGuardado).normalize();

            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            return new ArchivoGuardado(
                    originalFilename,
                    Paths.get("usuario-" + usuario.getIdUsuario(), nombreGuardado).toString().replace("\\", "/"),
                    contentType
            );

        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el archivo de antecedentes: " + e.getMessage(), e);
        }
    }

    private void validarArchivoAdjunto(MultipartFile file, String tipo) {
        String nombre = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        boolean permitido = nombre.endsWith(".pdf")
                || nombre.endsWith(".jpg")
                || nombre.endsWith(".jpeg")
                || nombre.endsWith(".png");

        if (!permitido) {
            throw new RuntimeException("El archivo de " + tipo + " debe ser PDF, JPG, JPEG o PNG.");
        }
    }

    private String obtenerExtension(String filename) {
        if (filename == null || filename.isBlank()) return "";
        int idx = filename.lastIndexOf(".");
        if (idx < 0) return "";
        return filename.substring(idx);
    }

    private record ArchivoGuardado(String nombreOriginal, String rutaRelativa, String contentType) {}

    @Transactional
    public void desactivarUsuario(Integer id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        u.setEstado("Inactivo");
        u.setFecha_modificacion(ZonedDateTime.now(ZONA_LIMA).toLocalDateTime());
        usuarioRepository.save(u);
    }

    public void eliminarUsuario(Integer id) {
        usuarioRepository.deleteById(id);
    }

    public boolean updateProfilePhoto(Integer idUsuario, MultipartFile file) {
        if (file.isEmpty() || file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            return false;
        }

        Optional<Usuario> optionalUsuario = usuarioRepository.findById(idUsuario);
        if (optionalUsuario.isEmpty()) return false;

        Usuario usuario = optionalUsuario.get();

        try {
            Path subPath = Paths.get(uploadDir, PROFILE_PHOTO_FOLDER);
            if (Files.notExists(subPath)) Files.createDirectories(subPath);

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uniqueFileName = UUID.randomUUID() + extension;
            Path destinationFile = subPath.resolve(uniqueFileName);

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            if (usuario.getFotoPerfil() != null && !usuario.getFotoPerfil().isEmpty()) {
                Path oldFilePath = subPath.resolve(usuario.getFotoPerfil());
                Files.deleteIfExists(oldFilePath);
            }

            usuario.setFotoPerfil(uniqueFileName);
            usuarioRepository.save(usuario);
            return true;

        } catch (IOException e) {
            System.err.println("Error al guardar la foto de perfil para el usuario " + idUsuario + ": " + e.getMessage());
            return false;
        }
    }

    public Path loadProfilePhotoPath(String filename) {
        return Paths.get(uploadDir, PROFILE_PHOTO_FOLDER).resolve(filename);
    }

    public Path loadIdiomaFilePath(String relativePath) {
        return Paths.get(uploadDir, IDIOMAS_FOLDER).resolve(relativePath).normalize();
    }

    public Path loadCertificacionFilePath(String relativePath) {
        return Paths.get(uploadDir, CERTIFICACIONES_FOLDER).resolve(relativePath).normalize();
    }

    public Path loadExperienciaFilePath(String relativePath) {
        return Paths.get(uploadDir, EXPERIENCIAS_FOLDER).resolve(relativePath).normalize();
    }

    public Path loadCertijovenFilePath(String relativePath) {
        return Paths.get(uploadDir, CERTIJOVEN_FOLDER).resolve(relativePath).normalize();
    }

    public Path loadCertiadultoFilePath(String relativePath) {
        return Paths.get(uploadDir, CERTIADULTO_FOLDER).resolve(relativePath).normalize();
    }

    public Path loadAntecedentesFilePath(String relativePath) {
        return Paths.get(uploadDir, ANTECEDENTES_FOLDER).resolve(relativePath).normalize();
    }

    public String obtenerEstadoAsistenciaActual(Integer idUsuario) {
        List<Asistencia> asistenciasPendientes = asistenciaRepository.findEntradasPendientesHoy(idUsuario);
        if (asistenciasPendientes != null && !asistenciasPendientes.isEmpty()) return "Trabajando";
        return "Fuera de Servicio";
    }

    public Map<Integer, String> obtenerEstadoAsistenciaParaUsuarios(List<Usuario> usuarios) {
        Map<Integer, String> estados = new HashMap<>();
        for (Usuario u : usuarios) {
            estados.put(u.getIdUsuario(), obtenerEstadoAsistenciaActual(u.getIdUsuario()));
        }
        return estados;
    }

    public Usuario obtenerUsuarioLogueado() {
        return usuarioRepository.findById(1).orElse(null);
    }

    public Asistencia obtenerAsistenciaPorId(Integer id) {
        return asistenciaRepository.findById(id).orElse(null);
    }
}