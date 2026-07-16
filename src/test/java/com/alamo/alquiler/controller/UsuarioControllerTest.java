package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.Rol;
import com.alamo.alquiler.model.Usuario;
import com.alamo.alquiler.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario usuario;
    private Rol rol;

    @BeforeEach
    void setUp() {
        rol = Rol.builder().idRol(1).nombre("ADMINISTRADOR").build();
        usuario = Usuario.builder()
                .idUsuario(1)
                .nombres("Juan")
                .apellidoPaterno("Perez")
                .apellidoMaterno("Gomez")
                .correo("juan.perez@example.com")
                .contrasenaHash("hash123")
                .rol(rol)
                .build();
    }

    @Test
    void testListarUsuarios() throws Exception {
        when(repository.findAll()).thenReturn(Arrays.asList(usuario));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombres", is("Juan")))
                .andExpect(jsonPath("$[0].correo", is("juan.perez@example.com")));

        verify(repository, times(1)).findAll();
    }

    @Test
    void testObtenerUsuarioPorId() throws Exception {
        when(repository.findById(1)).thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres", is("Juan")))
                .andExpect(jsonPath("$.correo", is("juan.perez@example.com")));

        verify(repository, times(1)).findById(1);
    }

    @Test
    void testObtenerUsuarioNoEncontrado() throws Exception {
        when(repository.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound());

        verify(repository, times(1)).findById(99);
    }

    @Test
    void testCrearUsuario() throws Exception {
        when(repository.save(any(Usuario.class))).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombres", is("Juan")))
                .andExpect(jsonPath("$.correo", is("juan.perez@example.com")));

        verify(repository, times(1)).save(any(Usuario.class));
    }
}
