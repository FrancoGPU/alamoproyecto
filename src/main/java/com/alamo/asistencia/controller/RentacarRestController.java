package com.alamo.asistencia.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.alamo.asistencia.model.*;
import com.alamo.asistencia.repository.*;

@RestController
@RequestMapping("/api/rentacar")
public class RentacarRestController {

    private final ICategoriaVehiculoRepository categoriaVehiculoRepository;
    private final IVehiculoRepository vehiculoRepository;
    private final IClienteRepository clienteRepository;
    private final IContratoAlquilerRepository contratoAlquilerRepository;
    private final IPagoGarantiaRepository pagoGarantiaRepository;
    private final IInspeccionVehiculoRepository inspeccionVehiculoRepository;
    private final ISeguroRepository seguroRepository;

    public RentacarRestController(
            ICategoriaVehiculoRepository categoriaVehiculoRepository,
            IVehiculoRepository vehiculoRepository,
            IClienteRepository clienteRepository,
            IContratoAlquilerRepository contratoAlquilerRepository,
            IPagoGarantiaRepository pagoGarantiaRepository,
            IInspeccionVehiculoRepository inspeccionVehiculoRepository,
            ISeguroRepository seguroRepository
    ) {
        this.categoriaVehiculoRepository = categoriaVehiculoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.clienteRepository = clienteRepository;
        this.contratoAlquilerRepository = contratoAlquilerRepository;
        this.pagoGarantiaRepository = pagoGarantiaRepository;
        this.inspeccionVehiculoRepository = inspeccionVehiculoRepository;
        this.seguroRepository = seguroRepository;
    }

    @GetMapping("/categorias")
    public List<CategoriaVehiculo> listarCategorias() {
        return categoriaVehiculoRepository.findAll();
    }

    @GetMapping("/vehiculos/disponibles")
    public List<Vehiculo> buscarVehiculosDisponibles(
            @RequestParam(required = false) TipoCategoriaVehiculo categoria,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime pickup,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dropoff
    ) {
        return vehiculoRepository.buscarDisponibles(categoria, pickup, dropoff);
    }

    @PostMapping("/clientes")
    public Cliente guardarCliente(@RequestBody Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @PostMapping("/contratos")
    public ContratoAlquiler guardarContrato(@RequestBody ContratoAlquiler contrato) {
        return contratoAlquilerRepository.save(contrato);
    }

    @PostMapping("/pagos-garantias")
    public PagoGarantia guardarPagoGarantia(@RequestBody PagoGarantia pagoGarantia) {
        return pagoGarantiaRepository.save(pagoGarantia);
    }

    @PostMapping("/inspecciones")
    public InspeccionVehiculo guardarInspeccion(@RequestBody InspeccionVehiculo inspeccionVehiculo) {
        return inspeccionVehiculoRepository.save(inspeccionVehiculo);
    }

    @GetMapping("/seguros")
    public List<Seguro> listarSegurosActivos() {
        return seguroRepository.findByActivoTrueOrderByNombreAsc();
    }
}
