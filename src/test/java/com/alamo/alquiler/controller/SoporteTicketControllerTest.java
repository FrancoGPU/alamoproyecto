package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.SoporteTicket;
import com.alamo.alquiler.repository.SoporteTicketRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.alamo.alquiler.repository.NotificacionRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SoporteTicketController.class)
public class SoporteTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SoporteTicketRepository repository;

    @MockitoBean
    private NotificacionRepository notificacionRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private SoporteTicket ticket;

    @BeforeEach
    void setUp() {
        ticket = SoporteTicket.builder()
                .idTicket(1)
                .cliente("Carlos Rivas")
                .correo("carlos@example.com")
                .asunto("Problema con alquiler")
                .mensaje("No puedo registrar el retorno del auto")
                .estado("ABIERTO")
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    @Test
    void testListarTickets() throws Exception {
        when(repository.findAll()).thenReturn(Arrays.asList(ticket));

        mockMvc.perform(get("/api/soporte"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cliente", is("Carlos Rivas")))
                .andExpect(jsonPath("$[0].asunto", is("Problema con alquiler")));

        verify(repository, times(1)).findAll();
    }

    @Test
    void testObtenerTicketPorId() throws Exception {
        when(repository.findById(1)).thenReturn(Optional.of(ticket));

        mockMvc.perform(get("/api/soporte/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente", is("Carlos Rivas")))
                .andExpect(jsonPath("$.mensaje", is("No puedo registrar el retorno del auto")));

        verify(repository, times(1)).findById(1);
    }

    @Test
    void testCrearTicket() throws Exception {
        when(repository.save(any(SoporteTicket.class))).thenReturn(ticket);

        mockMvc.perform(post("/api/soporte")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticket)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cliente", is("Carlos Rivas")))
                .andExpect(jsonPath("$.estado", is("ABIERTO")));

        verify(repository, times(1)).save(any(SoporteTicket.class));
    }
}
