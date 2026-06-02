package com.alamo.asistencia.service;

import com.alamo.asistencia.model.Saludo;
import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.repository.ISaludoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SaludoService {

    @Autowired
    private ISaludoRepository saludoRepository;

    /**
     * Obtiene los cumpleañeros del mes actual para que el usuario pueda enviarles saludos.
     */
    public List<Usuario> obtenerCumpleanerosParaSaludar(Integer idUsuarioLogueado) {
        int mes = LocalDate.now().getMonthValue();
        List<Usuario> lista = saludoRepository.findCumpleanerosDelMes(mes);

        if (lista == null) return new ArrayList<>();

        return lista.stream()
                .filter(u -> u.getIdUsuario() != null && !u.getIdUsuario().equals(idUsuarioLogueado))
                .collect(Collectors.toList());
    }

    /**
     * CORREGIDO: Trae solo los saludos dirigidos al usuario actual.
     * Así evitamos que veas los mensajes que tú enviaste o los de otros.
     */
    public List<Saludo> obtenerSaludosDelMuro(Integer idUsuarioLogueado) {
        if (idUsuarioLogueado == null) return new ArrayList<>();
        // Llamamos al nuevo método del repositorio filtrando por receptor
        return saludoRepository.findMisSaludosRecibidos(idUsuarioLogueado);
    }

    public boolean esMesDeCumpleaniosDeUsuario(Usuario u) {
        if (u == null || u.getFecha_nacimiento() == null) return false;
        return u.getFecha_nacimiento().getMonthValue() == LocalDate.now().getMonthValue();
    }

    @Transactional
    public void guardarSaludo(Usuario emisor, Integer idReceptor, String mensaje) {
        if (emisor == null || idReceptor == null || mensaje == null || mensaje.trim().isEmpty()) return;

        Usuario receptor = new Usuario();
        receptor.setIdUsuario(idReceptor);

        Saludo s = new Saludo();
        s.setEmisor(emisor);
        s.setReceptor(receptor);
        s.setMensaje(mensaje);
        
        // El @PrePersist en la entidad Saludo llenará automáticamente mes_saludo y anio_saludo
        saludoRepository.save(s);
    }
}