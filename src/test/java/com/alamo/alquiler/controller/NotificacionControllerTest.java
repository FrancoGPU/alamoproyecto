package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.Notificacion;
import com.alamo.alquiler.repository.NotificacionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificacionController.class)
public class NotificacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificacionRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private Notificacion notificacion;

    @BeforeEach
    void setUp() {
        notificacion = Notificacion.builder()
                .idNotificacion(1)
                .titulo("Nuevo Alquiler")
                .mensaje("Contrato CON-2026-001 registrado con éxito.")
                .leido(false)
                .tipo("CONTRATO")
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    @Test
    void testListarNotificaciones() throws Exception {
        when(repository.findAll()).thenReturn(Arrays.asList(notificacion));

        mockMvc.perform(get("/api/notificaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo", is("Nuevo Alquiler")));

        verify(repository, times(1)).findAll();
    }

    @Test
    void testMarcarComoLeida() throws Exception {
        when(repository.findById(1)).thenReturn(Optional.of(notificacion));
        when(repository.save(any(Notificacion.class))).thenReturn(notificacion);

        mockMvc.perform(put("/api/notificaciones/1/leer"))
                .andExpect(status().isOk());

        verify(repository, times(1)).findById(1);
    }
}
