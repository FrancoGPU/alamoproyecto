package com.alamo.asistencia.service;

import com.alamo.asistencia.model.Asistencia;
import com.alamo.asistencia.model.AsistenciaAudit;
import com.alamo.asistencia.model.Horario;
import com.alamo.asistencia.model.PermisoExtra;
import com.alamo.asistencia.model.Turno;
import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.repository.AsistenciaAuditRepository;
import com.alamo.asistencia.repository.IAsistenciaRepository;
import com.alamo.asistencia.repository.IHorarioRepository;
import com.alamo.asistencia.repository.IPermisoExtraRepository;
import com.alamo.asistencia.repository.IUsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AsistenciaService {

    @Autowired private IAsistenciaRepository repo;
    @Autowired private IHorarioRepository horarioRepo;
    @Autowired private IPermisoExtraRepository permisoRepo;
    @Autowired private AsistenciaAuditRepository auditRepo;
    @Autowired private IUsuarioRepository usuarioRepo;

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    private final ConcurrentHashMap<Integer, String> confirmacionesSalida = new ConcurrentHashMap<>();

    @Transactional
    public String registrarEntrada(Usuario usuario, String ubicacion) {
        if (usuario == null || usuario.getIdUsuario() == null) return "ERROR: Usuario inválido.";

        ZonedDateTime ahoraLima = ZonedDateTime.now(ZONA_LIMA);
        LocalDate hoy = ahoraLima.toLocalDate();
        LocalTime horaActual = normalizarHora(ahoraLima.toLocalTime());

        List<Asistencia> asistenciasHoy = safeList(repo.findByUsuario_IdUsuarioAndFecha(usuario.getIdUsuario(), hoy));

        if (asistenciasHoy.stream().anyMatch(a -> a != null && a.getHoraEntrada() != null && a.getHoraSalida() == null)) {
            return "ERROR: Ya tienes una entrada activa. Marca salida primero.";
        }

        Optional<PermisoExtra> potestad = permisoRepo.findByUsuario_IdUsuarioAndFechaSolicitudAndEstadoAndUsadoFalse(
                usuario.getIdUsuario(), hoy, "APROBADO"
        );
        if (potestad.isPresent()) {
            return ejecutarRegistroBloqueExtra(usuario, ubicacion, hoy, horaActual, potestad.get());
        }

        int diaSemana = hoy.getDayOfWeek().getValue();
        List<Horario> listaHorarios = safeList(horarioRepo.findByUsuario_IdUsuarioAndDia(usuario.getIdUsuario(), diaSemana));
        if (listaHorarios.isEmpty()) return "ERROR: No tienes horarios asignados para hoy.";

        List<Turno> turnosOrdenados = listaHorarios.stream()
                .map(Horario::getTurno)
                .filter(Objects::nonNull)
                .filter(t -> t.getEntrada() != null)
                .sorted(Comparator.comparing(Turno::getEntrada))
                .collect(Collectors.toList());

        int nroAsistenciasPrevias = asistenciasHoy.size();
        if (nroAsistenciasPrevias >= turnosOrdenados.size()) {
            return "ERROR: Ya completaste todos tus turnos programados de hoy.";
        }

        Turno turnoCorrespondiente = turnosOrdenados.get(nroAsistenciasPrevias);
        LocalTime entradaOficial = normalizarHora(turnoCorrespondiente.getEntrada());

        if (entradaOficial != null && horaActual.isBefore(entradaOficial)) {
            return "ERROR: No puedes marcar antes de tu hora. Tu entrada es a las " + turnoCorrespondiente.getEntrada();
        }

        Asistencia asistencia = new Asistencia();
        asistencia.setUsuario(usuario);
        asistencia.setFecha(hoy);
        asistencia.setHoraEntrada(horaActual);
        asistencia.setUbicacion((ubicacion != null && !ubicacion.isBlank()) ? ubicacion : "No disponible");

        if (entradaOficial != null && horaActual.isAfter(entradaOficial)) {
            long minutos = Duration.between(entradaOficial, horaActual).toMinutes();
            asistencia.setMinutosTardanza((int) Math.max(minutos, 0));
            asistencia.setEstadoAsistencia("TARDE");
        } else {
            asistencia.setMinutosTardanza(0);
            asistencia.setEstadoAsistencia("PUNTUAL");
        }

        repo.save(asistencia);
        return "✅ Entrada registrada (" + (nroAsistenciasPrevias + 1) + "º turno): " + asistencia.getEstadoAsistencia();
    }

    @Transactional
    public String registrarSalida(Usuario usuario, String ubicacion) {
        if (usuario == null || usuario.getIdUsuario() == null) return "ERROR: Usuario inválido.";

        ZonedDateTime ahoraLima = ZonedDateTime.now(ZONA_LIMA);
        LocalDate hoy = ahoraLima.toLocalDate();
        LocalTime horaActual = normalizarHora(ahoraLima.toLocalTime());

        List<Asistencia> asistenciasHoy = safeList(repo.findByUsuario_IdUsuarioAndFecha(usuario.getIdUsuario(), hoy))
                .stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Asistencia::getHoraEntrada, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        Asistencia pendiente = asistenciasHoy.stream()
                .filter(a -> a.getHoraEntrada() != null && a.getHoraSalida() == null)
                .findFirst()
                .orElse(null);

        if (pendiente == null) return "ERROR: No tienes una entrada activa.";

        int diaSemana = hoy.getDayOfWeek().getValue();
        List<Horario> listaHorarios = safeList(horarioRepo.findByUsuario_IdUsuarioAndDia(usuario.getIdUsuario(), diaSemana));

        List<Turno> turnosOrdenados = listaHorarios.stream()
                .map(Horario::getTurno)
                .filter(Objects::nonNull)
                .filter(t -> t.getEntrada() != null)
                .sorted(Comparator.comparing(Turno::getEntrada))
                .collect(Collectors.toList());

        int posicion = calcularPosicionTurno(asistenciasHoy, pendiente);
        Turno turnoProgramado = (posicion >= 0 && posicion < turnosOrdenados.size()) ? turnosOrdenados.get(posicion) : null;

        if (turnoProgramado != null && turnoProgramado.getSalida() != null) {
            LocalTime salidaOficial = normalizarHora(turnoProgramado.getSalida());

            if (salidaOficial != null && horaActual.isAfter(salidaOficial)) {
                pendiente.setHoraSalida(salidaOficial);
                confirmacionesSalida.remove(usuario.getIdUsuario());
            } else if (salidaOficial != null && horaActual.isBefore(salidaOficial)) {
                String token = horaActual.toString();
                String prev = confirmacionesSalida.get(usuario.getIdUsuario());
                if (!token.equals(prev)) {
                    confirmacionesSalida.put(usuario.getIdUsuario(), token);
                    return "⚠️ Salida anticipada. Confirma otra vez.";
                }
                confirmacionesSalida.remove(usuario.getIdUsuario());
                pendiente.setHoraSalida(horaActual);
            } else {
                pendiente.setHoraSalida(horaActual);
                confirmacionesSalida.remove(usuario.getIdUsuario());
            }

            pendiente.setMinutosExtra(0);
        } else {
            pendiente.setHoraSalida(horaActual);
            confirmacionesSalida.remove(usuario.getIdUsuario());

            if ("EXTRA".equalsIgnoreCase(pendiente.getEstadoAsistencia()) && pendiente.getHoraEntrada() != null) {
                long minExtra = Duration.between(normalizarHora(pendiente.getHoraEntrada()), horaActual).toMinutes();
                pendiente.setMinutosExtra((int) Math.max(minExtra, 0));
            } else {
                pendiente.setMinutosExtra(0);
            }
        }

        if (ubicacion != null && !ubicacion.isBlank()) pendiente.setUbicacion(ubicacion);

        recalcularTotales(pendiente);
        repo.save(pendiente);

        return "✅ Salida registrada.";
    }

    @Transactional
    public void actualizarManual(Integer idAsistencia,
                                 LocalTime nuevaEntrada,
                                 LocalTime nuevaSalida,
                                 Usuario actor,
                                 String ip) {

        Asistencia asis = repo.findById(idAsistencia)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado"));

        LocalTime entradaAntes = asis.getHoraEntrada();
        LocalTime salidaAntes = asis.getHoraSalida();

        LocalTime entradaNorm = (nuevaEntrada != null) ? normalizarHora(nuevaEntrada) : null;
        LocalTime salidaNorm = (nuevaSalida != null) ? normalizarHora(nuevaSalida) : null;

        boolean cambio = !Objects.equals(entradaAntes, entradaNorm) || !Objects.equals(salidaAntes, salidaNorm);

        asis.setHoraEntrada(entradaNorm);
        asis.setHoraSalida(salidaNorm);

        recalcularEstadoSegunTurno(asis);
        recalcularTotales(asis);
        repo.save(asis);

        if (cambio && actor != null) {
            auditRepo.save(AsistenciaAudit.builder()
                    .idAsistencia(asis.getIdAsistencia())
                    .accion("EDITAR_HORAS")
                    .entradaAntes(entradaAntes)
                    .salidaAntes(salidaAntes)
                    .entradaDespues(asis.getHoraEntrada())
                    .salidaDespues(asis.getHoraSalida())
                    .usuarioActor(actor)
                    .fechaAccion(ZonedDateTime.now(ZONA_LIMA).toLocalDateTime())
                    .ipActor((ip != null && !ip.isBlank()) ? ip : null)
                    .build());
        }
    }

    @Transactional
    public void registrarManual(Integer idUsuario,
                                LocalDate fecha,
                                LocalTime entrada,
                                LocalTime salida,
                                String ubicacion,
                                Usuario actor,
                                String ip) {

        if (idUsuario == null) throw new RuntimeException("El usuario es obligatorio");
        if (fecha == null) throw new RuntimeException("La fecha es obligatoria");
        if (entrada == null) throw new RuntimeException("La hora de entrada es obligatoria");

        Usuario usuarioRef = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Asistencia nueva = new Asistencia();
        nueva.setUsuario(usuarioRef);
        nueva.setFecha(fecha);
        nueva.setHoraEntrada(normalizarHora(entrada));
        nueva.setHoraSalida(normalizarHora(salida));
        nueva.setUbicacion((ubicacion != null && !ubicacion.isBlank()) ? ubicacion.trim() : "Registro manual");
        nueva.setMinutosExtra(0);
        nueva.setMinutosTardanza(0);
        nueva.setEstadoAsistencia("PUNTUAL");

        recalcularEstadoSegunTurno(nueva);
        recalcularTotales(nueva);

        repo.save(nueva);

        if (actor != null) {
            auditRepo.save(AsistenciaAudit.builder()
                    .idAsistencia(nueva.getIdAsistencia())
                    .accion("CREAR_MANUAL")
                    .entradaAntes(null)
                    .salidaAntes(null)
                    .entradaDespues(nueva.getHoraEntrada())
                    .salidaDespues(nueva.getHoraSalida())
                    .usuarioActor(actor)
                    .fechaAccion(ZonedDateTime.now(ZONA_LIMA).toLocalDateTime())
                    .ipActor((ip != null && !ip.isBlank()) ? ip : null)
                    .build());
        }
    }

    @Transactional
    public void eliminarManual(Integer idAsistencia, Usuario actor, String ip) {
        Asistencia asis = repo.findById(idAsistencia)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado"));

        LocalTime entradaAntes = asis.getHoraEntrada();
        LocalTime salidaAntes = asis.getHoraSalida();

        if (actor != null) {
            auditRepo.save(AsistenciaAudit.builder()
                    .idAsistencia(asis.getIdAsistencia())
                    .accion("ELIMINAR_MANUAL")
                    .entradaAntes(entradaAntes)
                    .salidaAntes(salidaAntes)
                    .entradaDespues(null)
                    .salidaDespues(null)
                    .usuarioActor(actor)
                    .fechaAccion(ZonedDateTime.now(ZONA_LIMA).toLocalDateTime())
                    .ipActor((ip != null && !ip.isBlank()) ? ip : null)
                    .build());
        }

        repo.delete(asis);
    }

    private void recalcularEstadoSegunTurno(Asistencia asis) {
        if (asis == null || asis.getFecha() == null || asis.getUsuario() == null || asis.getUsuario().getIdUsuario() == null) {
            return;
        }

        int diaSemana = asis.getFecha().getDayOfWeek().getValue();
        List<Horario> listaHorarios = safeList(
                horarioRepo.findByUsuario_IdUsuarioAndDia(asis.getUsuario().getIdUsuario(), diaSemana)
        );

        if (listaHorarios.isEmpty()) {
            if (!"EXTRA".equalsIgnoreCase(asis.getEstadoAsistencia())) {
                asis.setEstadoAsistencia("PUNTUAL");
                asis.setMinutosTardanza(0);
            }
            return;
        }

        List<Turno> turnosProg = listaHorarios.stream()
                .map(Horario::getTurno)
                .filter(Objects::nonNull)
                .filter(t -> t.getEntrada() != null)
                .sorted(Comparator.comparing(Turno::getEntrada))
                .collect(Collectors.toList());

        List<Asistencia> marcasDia = safeList(
                repo.findByUsuario_IdUsuarioAndFecha(asis.getUsuario().getIdUsuario(), asis.getFecha())
        ).stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Asistencia::getHoraEntrada, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        boolean existeEnBD = asis.getIdAsistencia() != null;
        int pos = existeEnBD ? indexById(marcasDia, asis.getIdAsistencia()) : marcasDia.size();

        if (pos < 0 || pos >= turnosProg.size()) {
            asis.setMinutosTardanza(0);
            if (!"EXTRA".equalsIgnoreCase(asis.getEstadoAsistencia())) {
                asis.setEstadoAsistencia("PUNTUAL");
            }
            return;
        }

        Turno turno = turnosProg.get(pos);
        LocalTime entradaOficial = normalizarHora(turno.getEntrada());
        LocalTime entradaMarcada = normalizarHora(asis.getHoraEntrada());

        if (entradaMarcada != null && entradaOficial != null && entradaMarcada.isAfter(entradaOficial)) {
            long min = Duration.between(entradaOficial, entradaMarcada).toMinutes();
            asis.setMinutosTardanza((int) Math.max(min, 0));
            asis.setEstadoAsistencia("TARDE");
        } else {
            asis.setMinutosTardanza(0);
            if (!"EXTRA".equalsIgnoreCase(asis.getEstadoAsistencia())) {
                asis.setEstadoAsistencia("PUNTUAL");
            }
        }
    }

    private void recalcularTotales(Asistencia a) {
        if (a == null) return;

        if (a.getHoraEntrada() != null && a.getHoraSalida() != null) {
            LocalTime inicio = normalizarHora(a.getHoraEntrada());
            LocalTime fin = normalizarHora(a.getHoraSalida());

            if (inicio != null && fin != null && fin.isAfter(inicio)) {
                long minutosTotales = Duration.between(inicio, fin).toMinutes();
                BigDecimal horas = BigDecimal.valueOf(minutosTotales)
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                a.setHorasTrabajadas(horas);
            } else {
                a.setHorasTrabajadas(BigDecimal.ZERO);
            }
        } else {
            a.setHorasTrabajadas(BigDecimal.ZERO);
        }
    }

    public Asistencia obtenerEntradaHoy(Usuario usuario) {
        if (usuario == null || usuario.getIdUsuario() == null) return null;
        LocalDate hoy = LocalDate.now(ZONA_LIMA);
        List<Asistencia> lista = repo.findByUsuario_IdUsuarioAndFecha(usuario.getIdUsuario(), hoy);
        return (lista != null)
                ? lista.stream().filter(a -> a.getHoraSalida() == null).findFirst().orElse(null)
                : null;
    }

    public List<Asistencia> obtenerHistorialDelDia(Usuario usuario, LocalDate fecha) {
        if (usuario == null || usuario.getIdUsuario() == null) return List.of();
        return safeList(repo.findByUsuario_IdUsuarioAndFecha(usuario.getIdUsuario(), fecha));
    }

    public List<Asistencia> obtenerHistorialPorRangoUsuario(Usuario usuario, LocalDate inicio, LocalDate fin) {
        if (usuario == null || usuario.getIdUsuario() == null) return List.of();
        return safeList(repo.findByUsuario_IdUsuarioAndFechaBetweenOrderByFechaAsc(usuario.getIdUsuario(), inicio, fin));
    }

    public List<Asistencia> obtenerHistorialPorRango(LocalDate inicio, LocalDate fin) {
        return safeList(repo.findByFechaBetweenOrderByFechaDescHoraEntradaDesc(inicio, fin));
    }

    private String ejecutarRegistroBloqueExtra(Usuario u, String ub, LocalDate f, LocalTime h, PermisoExtra p) {
        Asistencia extra = new Asistencia();
        extra.setUsuario(u);
        extra.setFecha(f);
        extra.setHoraEntrada(normalizarHora(h));
        extra.setUbicacion((ub != null && !ub.isBlank()) ? ub : "No disponible");
        extra.setEstadoAsistencia("EXTRA");
        extra.setMinutosExtra(0);
        extra.setMinutosTardanza(0);

        repo.save(extra);

        p.setUsado(true);
        permisoRepo.save(p);

        return "✅ Entrada EXTRA iniciada.";
    }

    public Map<Integer, Map<LocalDate, List<Asistencia>>> obtenerAsistenciasAgrupadasPorDia(LocalDate inicio, LocalDate fin) {
        List<Asistencia> lista = obtenerHistorialPorRango(inicio, fin);
        return lista.stream()
                .filter(a -> a.getUsuario() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getUsuario().getIdUsuario(),
                        Collectors.groupingBy(Asistencia::getFecha, TreeMap::new, Collectors.toList())
                ));
    }

    public Map<Integer, Double> obtenerTotalesHorasPorUsuario(List<Asistencia> lista) {
        return safeList(lista).stream()
                .filter(a -> a != null && a.getUsuario() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getUsuario().getIdUsuario(),
                        Collectors.summingDouble(a -> a.getHorasTrabajadas() != null ? a.getHorasTrabajadas().doubleValue() : 0.0)
                ));
    }

    private static LocalTime normalizarHora(LocalTime t) {
        return (t == null) ? null : t.withSecond(0).withNano(0);
    }

    private static <T> List<T> safeList(List<T> x) {
        return (x == null) ? new ArrayList<>() : x;
    }

    private static int calcularPosicionTurno(List<Asistencia> ordenadas, Asistencia target) {
        if (ordenadas == null || target == null) return -1;
        return indexById(ordenadas, target.getIdAsistencia());
    }

    private static int indexById(List<Asistencia> list, Integer idAsistencia) {
        if (list == null || idAsistencia == null) return -1;
        for (int i = 0; i < list.size(); i++) {
            Asistencia a = list.get(i);
            if (a != null && Objects.equals(a.getIdAsistencia(), idAsistencia)) return i;
        }
        return -1;
    }
}