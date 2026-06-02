package com.alamo.asistencia.service;

import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alamo.asistencia.model.Horario;
import com.alamo.asistencia.model.Turno;
import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.repository.IHorarioRepository;
import com.alamo.asistencia.repository.ITurnoRepository;
import com.alamo.asistencia.repository.IUsuarioRepository;

@Service
public class HorarioService {

    @Autowired
    private IHorarioRepository horarioRepo;

    @Autowired
    private ITurnoRepository turnoRepo;

    @Autowired
    private IUsuarioRepository usuarioRepo;

    public List<Horario> listarPorUsuario(Integer idUsuario) {
        return horarioRepo.findByUsuario_IdUsuarioOrderByDiaAsc(idUsuario);
    }

    @Transactional
    public Horario guardarHorarioManual(Integer idUsuario, String entradaStr, String salidaStr, Integer dia, Integer bloqueFijo) {
        
        // 1. Convertir horas recibidas
        LocalTime entrada = LocalTime.parse(entradaStr);
        LocalTime salida = LocalTime.parse(salidaStr);

        // 2. BUSCAR O CREAR TURNO
        Turno turnoAsignar = turnoRepo.findByEntradaAndSalida(entrada, salida)
                .orElseGet(() -> {
                    Turno nuevo = new Turno();
                    Integer maxId = turnoRepo.findAll().stream()
                            .mapToInt(Turno::getIdTurno)
                            .max().orElse(0);
                    nuevo.setIdTurno(maxId + 1);
                    nuevo.setEntrada(entrada);
                    nuevo.setSalida(salida);
                    return turnoRepo.save(nuevo);
                });

        Usuario usuario = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        // 3. IDENTIFICAR POR ORDEN DE CREACIÓN (Evita acumulación)
        List<Horario> actuales = horarioRepo.findByUsuario_IdUsuarioAndDiaOrderByIdHorarioAsc(idUsuario, dia);

        Horario horarioFinal;

        if (bloqueFijo == 1 && !actuales.isEmpty()) {
            horarioFinal = actuales.get(0);
        } 
        else if (bloqueFijo == 2 && actuales.size() >= 2) {
            horarioFinal = actuales.get(1);
        } 
        else {
            horarioFinal = new Horario();
            horarioFinal.setUsuario(usuario);
            horarioFinal.setDia(dia);
        }

        horarioFinal.setTurno(turnoAsignar);

        return horarioRepo.save(horarioFinal);
    }

    /**
     * NUEVO MÉTODO CORREGIDO: Elimina el registro físico de la DB
     * para que no se quede guardado como 00:00 o turno vacío.
     */
    @Transactional
    public void eliminarHorarioManual(Integer idUsuario, Integer dia, Integer bloqueFijo) {
        // Buscamos los registros actuales del día ordenados por ID
        List<Horario> actuales = horarioRepo.findByUsuario_IdUsuarioAndDiaOrderByIdHorarioAsc(idUsuario, dia);

        if (bloqueFijo == 1 && !actuales.isEmpty()) {
            // Eliminar el primer horario del día (T1)
            horarioRepo.delete(actuales.get(0));
        } 
        else if (bloqueFijo == 2 && actuales.size() >= 2) {
            // Eliminar el segundo horario del día (T2)
            horarioRepo.delete(actuales.get(1));
        }
    }

    @Transactional
    public void eliminarHorario(Integer idHorario) {
        if (horarioRepo.existsById(idHorario)) {
            horarioRepo.deleteById(idHorario);
        } else {
            throw new RuntimeException("No se encontró el horario.");
        }
    }
}