package com.alamo.asistencia.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ubigeo")
@RequiredArgsConstructor
public class UbigeoController {

    private final JdbcTemplate jdbc;

    @GetMapping(value = "/departamentos", produces = "application/json")
    public List<Map<String, Object>> departamentos() {
        return jdbc.queryForList("""
            SELECT id, name
            FROM ubigeo_peru_departments
            ORDER BY name
        """);
    }

    @GetMapping(value = "/provincias", produces = "application/json")
    public List<Map<String, Object>> provincias(@RequestParam(required = false) String dep) {
        if (!StringUtils.hasText(dep)) return Collections.emptyList();

        return jdbc.queryForList("""
            SELECT id, name
            FROM ubigeo_peru_provinces
            WHERE department_id = ?
            ORDER BY name
        """, dep.trim());
    }

    @GetMapping(value = "/distritos", produces = "application/json")
    public List<Map<String, Object>> distritos(@RequestParam(required = false) String prov) {
        if (!StringUtils.hasText(prov)) return Collections.emptyList();

        return jdbc.queryForList("""
            SELECT id, name
            FROM ubigeo_peru_districts
            WHERE province_id = ?
            ORDER BY name
        """, prov.trim());
    }
}