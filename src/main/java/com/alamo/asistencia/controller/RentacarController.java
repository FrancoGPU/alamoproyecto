package com.alamo.asistencia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alamo.asistencia.model.EstadoContratoAlquiler;
import com.alamo.asistencia.model.EstadoVehiculo;
import com.alamo.asistencia.repository.IContratoAlquilerRepository;
import com.alamo.asistencia.repository.IVehiculoRepository;

@Controller
@RequestMapping("/rentacar")
public class RentacarController {

    private final IVehiculoRepository vehiculoRepository;
    private final IContratoAlquilerRepository contratoAlquilerRepository;

    public RentacarController(
            IVehiculoRepository vehiculoRepository,
            IContratoAlquilerRepository contratoAlquilerRepository
    ) {
        this.vehiculoRepository = vehiculoRepository;
        this.contratoAlquilerRepository = contratoAlquilerRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("vehiculosDisponibles", vehiculoRepository.findByEstado(EstadoVehiculo.DISPONIBLE));
        model.addAttribute("contratosActivos", contratoAlquilerRepository.findByEstadoOrderByFechaHoraPickupDesc(EstadoContratoAlquiler.ACTIVO));
        return "rentacar/dashboard";
    }

    @GetMapping("/contratos/nuevo")
    public String nuevoContrato() {
        return "rentacar/contrato-form";
    }

    @GetMapping("/inspecciones")
    public String inspecciones() {
        return "rentacar/inspecciones";
    }
}
