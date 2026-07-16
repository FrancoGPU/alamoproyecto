package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.ContratoAlquiler;
import com.alamo.alquiler.repository.ContratoAlquilerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.alamo.alquiler.repository.NotificacionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContratoAlquilerController.class)
public class ContratoAlquilerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContratoAlquilerRepository repository;

    @MockitoBean
    private NotificacionRepository notificacionRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private ContratoAlquiler contrato;

    @BeforeEach
    void setUp() {
        contrato = ContratoAlquiler.builder()
                .idContrato(1)
                .codigo("CON-2026-001")
                .fechaInicio(LocalDate.of(2026, 7, 16))
                .fechaFin(LocalDate.of(2026, 7, 20))
                .montoTotal(new BigDecimal("350.00"))
                .build();
    }

    @Test
    void testListarContratos() throws Exception {
        when(repository.findAll()).thenReturn(Arrays.asList(contrato));

        mockMvc.perform(get("/api/contratos-alquiler"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigo", is("CON-2026-001")))
                .andExpect(jsonPath("$[0].montoTotal", is(350.0)));

        verify(repository, times(1)).findAll();
    }

    @Test
    void testObtenerContratoPorId() throws Exception {
        when(repository.findById(1)).thenReturn(Optional.of(contrato));

        mockMvc.perform(get("/api/contratos-alquiler/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo", is("CON-2026-001")))
                .andExpect(jsonPath("$.montoTotal", is(350.0)));

        verify(repository, times(1)).findById(1);
    }

    @Test
    void testObtenerContratoNoEncontrado() throws Exception {
        when(repository.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/contratos-alquiler/99"))
                .andExpect(status().isNotFound());

        verify(repository, times(1)).findById(99);
    }

    @Test
    void testCrearContrato() throws Exception {
        when(repository.save(any(ContratoAlquiler.class))).thenReturn(contrato);

        mockMvc.perform(post("/api/contratos-alquiler")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contrato)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo", is("CON-2026-001")))
                .andExpect(jsonPath("$.montoTotal", is(350.0)));

        verify(repository, times(1)).save(any(ContratoAlquiler.class));
    }
}
