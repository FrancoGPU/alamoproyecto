package com.alamo.asistencia.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.stereotype.Service;

import com.alamo.asistencia.dto.DiaControlDTO;
import com.alamo.asistencia.model.Asistencia;
import com.alamo.asistencia.model.Horario;
import com.alamo.asistencia.repository.IAsistenciaRepository;
import com.alamo.asistencia.repository.IHorarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InformeCalendarioService {

    private final IAsistenciaRepository asistenciaRepo;
    private final IHorarioRepository horarioRepo;

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    public Map<Integer, List<DiaControlDTO>> buildCalendario(List<Integer> userIds,
                                                            LocalDate inicio,
                                                            LocalDate fin) {

        Map<Integer, List<DiaControlDTO>> out = new HashMap<>();

        for (Integer uid : userIds) {

            // 1) Días laborables según horario
            List<Horario> horarios = horarioRepo.findByUsuario_IdUsuarioOrderByDiaAsc(uid);
            Set<Integer> diasLaborables = new HashSet<>();
            for (Horario h : horarios) {
                if (h.getDia() != null) diasLaborables.add(h.getDia()); // 1..7
            }

            // 2) Asistencias en rango (ya tienes método)
            List<Asistencia> asistencias = asistenciaRepo
                    .findByUsuario_IdUsuarioAndFechaBetweenOrderByFechaAsc(uid, inicio, fin);

            // Mapa fecha -> asistencia (si hubiera duplicados, prioriza la que tenga salida)
            Map<LocalDate, Asistencia> byFecha = new HashMap<>();
            for (Asistencia a : asistencias) {
                Asistencia prev = byFecha.get(a.getFecha());
                if (prev == null) {
                    byFecha.put(a.getFecha(), a);
                } else {
                    if (prev.getHoraSalida() == null && a.getHoraSalida() != null) {
                        byFecha.put(a.getFecha(), a);
                    }
                }
            }

            // 3) Construir día por día
            List<DiaControlDTO> dias = new ArrayList<>();
            LocalDate d = inicio;

            while (!d.isAfter(fin)) {
                int dia1a7 = d.getDayOfWeek().getValue(); // Java: Lun=1..Dom=7 (igual que tu Horario.dia)

                boolean trabaja = diasLaborables.contains(dia1a7);

                DiaControlDTO dto = new DiaControlDTO();
                dto.setFecha(d);

                if (!trabaja) {
                    // ✅ DESCANSO
                    dto.setDescanso(true);
                    dto.setAsistio(false);
                    dto.setEntrada("-");
                    dto.setSalida("-");
                    dto.setHorasTrabajadas(BigDecimal.ZERO);
                    dto.setMinutosTardanza(0);
                    dto.setMinutosExtra(0);
                } else {
                    dto.setDescanso(false);

                    Asistencia a = byFecha.get(d);
                    if (a == null) {
                        // ✅ NO ASISTIÓ
                        dto.setAsistio(false);
                        dto.setEntrada("-");
                        dto.setSalida("-");
                        dto.setHorasTrabajadas(BigDecimal.ZERO);
                        dto.setMinutosTardanza(0);
                        dto.setMinutosExtra(0);
                    } else {
                        // ✅ ASISTIÓ
                        dto.setAsistio(true);
                        dto.setEntrada(a.getHoraEntrada() != null ? a.getHoraEntrada().format(HHMM) : "-");
                        dto.setSalida(a.getHoraSalida() != null ? a.getHoraSalida().format(HHMM) : "-");
                        dto.setHorasTrabajadas(a.getHorasTrabajadas() != null ? a.getHorasTrabajadas() : BigDecimal.ZERO);
                        dto.setMinutosTardanza(a.getMinutosTardanza() != null ? a.getMinutosTardanza() : 0);
                        dto.setMinutosExtra(a.getMinutosExtra() != null ? a.getMinutosExtra() : 0);
                    }
                }

                dias.add(dto);
                d = d.plusDays(1);
            }

            out.put(uid, dias);
        }

        return out;
    }
}
