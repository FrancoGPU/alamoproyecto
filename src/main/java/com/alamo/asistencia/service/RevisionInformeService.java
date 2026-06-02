package com.alamo.asistencia.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alamo.asistencia.model.RevisionInforme;
import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.repository.IRevisionInformeRepository;
import com.alamo.asistencia.repository.IUsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RevisionInformeService {

    private final IRevisionInformeRepository revisionRepo;
    private final IUsuarioRepository usuarioRepo;

    @Transactional(readOnly = true)
    public Map<Integer, Boolean> obtenerMapRevisados(Integer anio, Integer mes) {
        List<RevisionInforme> list = revisionRepo.findAllByAnioAndMes(anio, mes);

        Map<Integer, Boolean> map = new LinkedHashMap<>();
        for (RevisionInforme r : list) {
            if (r.getUsuario() != null && r.getUsuario().getIdUsuario() != null) {
                map.put(r.getUsuario().getIdUsuario(), Boolean.TRUE.equals(r.getRevisado()));
            }
        }
        return map;
    }

    @Transactional
    public boolean upsert(Integer idUsuario, Integer anio, Integer mes, boolean revisado) {
        RevisionInforme reg = revisionRepo
                .findByUsuario_IdUsuarioAndAnioAndMes(idUsuario, anio, mes)
                .orElseGet(() -> {
                    Usuario u = usuarioRepo.findById(idUsuario)
                            .orElseThrow(() -> new IllegalArgumentException("Usuario no existe: " + idUsuario));
                    RevisionInforme r = new RevisionInforme();
                    r.setUsuario(u);
                    r.setAnio(anio);
                    r.setMes(mes);
                    return r;
                });

        reg.setRevisado(revisado);
        revisionRepo.save(reg);
        return revisado;
    }
}