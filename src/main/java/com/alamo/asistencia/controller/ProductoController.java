package com.alamo.asistencia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductoController {

    @GetMapping("/inventario/global")
    public String redirigirInventarioGlobal() {
        return "redirect:/producto/global";
    }
}