package com.alamo.asistencia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alamo.asistencia.model.Cliente;
import com.alamo.asistencia.model.TipoDocumentoCliente;

public interface IClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByTipoDocumentoAndNroDocumento(TipoDocumentoCliente tipoDocumento, String nroDocumento);
}
