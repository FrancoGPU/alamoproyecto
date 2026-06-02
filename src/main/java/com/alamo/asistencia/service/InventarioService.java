package com.alamo.asistencia.service;

import com.alamo.asistencia.model.Ingreso;
import com.alamo.asistencia.model.Producto;
import com.alamo.asistencia.model.Servicio;
import com.alamo.asistencia.repository.IIngresoRepository;
import com.alamo.asistencia.repository.IProductoRepository;
import com.alamo.asistencia.repository.IServicioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class InventarioService {

    @Autowired
    private IIngresoRepository ingresoRepository;

    @Autowired
    private IProductoRepository productoRepository;
    
    @Autowired
    private IServicioRepository servicioRepository;

    // =======================================================
    // MÉTODOS DE CÁLCULO (OPTIMIZADOS)
    // =======================================================
    
    @Transactional(readOnly = true)
    public double calcularGastoProductos(Integer idIngreso) {
        return ingresoRepository.findById(idIngreso)
            .map(ingreso -> ingreso.getProductos().stream()
                .mapToDouble(p -> (p.getPrecio() != null ? p.getPrecio() : 0.0) * (p.getCantidad() != null ? p.getCantidad() : 0.0))
                .sum())
            .orElse(0.0);
    }

    @Transactional(readOnly = true)
    public double calcularGastoServicios(Integer idIngreso) {
        return ingresoRepository.findById(idIngreso)
            .map(ingreso -> ingreso.getServicios().stream()
                .mapToDouble(s -> s.getTotal())
                .sum())
            .orElse(0.0);
    }
    
    @Transactional(readOnly = true)
    public double calcularSaldoDisponible(Integer idIngreso) {
        return ingresoRepository.findById(idIngreso)
            .map(ingreso -> {
                double montoIngreso = ingreso.getMonto() != null ? ingreso.getMonto() : 0.0;
                double gastoProds = ingreso.getProductos().stream()
                    .mapToDouble(p -> (p.getPrecio() != null ? p.getPrecio() : 0.0) * (p.getCantidad() != null ? p.getCantidad() : 0.0))
                    .sum();
                double gastoServs = ingreso.getServicios().stream()
                    .mapToDouble(s -> s.getTotal())
                    .sum();
                return montoIngreso - (gastoProds + gastoServs);
            })
            .orElse(0.0);
    }
    
    // ✅ CORRECCIÓN DEFINITIVA PARA EL ERROR MultipleBagFetchException
    @Transactional(readOnly = true)
    public Ingreso obtenerIngresoPorMes(LocalDate fecha) {
        // Paso 1: Cargar el ingreso con sus productos (Esto abre la sesión y trae la primera bolsa)
        Optional<Ingreso> ingresoOpt = ingresoRepository.findMesConProductos(fecha);
        
        if (ingresoOpt.isPresent()) {
            // Paso 2: Cargar el ingreso con sus servicios (Hibernate une esta bolsa al objeto en memoria)
            // Usamos findMesConServicios que definimos en el repositorio corregido
            return ingresoRepository.findMesConServicios(fecha).orElse(ingresoOpt.get());
        }
        
        return null;
    }

    // =======================================================
    // MÉTODOS CRUD BÁSICOS
    // =======================================================
    
    @Transactional
    public void guardarProducto(Producto producto) {
        productoRepository.save(producto);
    }

    @Transactional
    public void guardarServicio(Servicio servicio) {
        servicioRepository.save(servicio);
    }

    @Transactional
    public void eliminarProducto(Integer idProducto) {
        productoRepository.findById(idProducto).ifPresent(p -> {
            String folder = "/root/Asistencia/uploads/Inventario/";
            if (p.getFotoFactura() != null && !p.getFotoFactura().isEmpty()) {
                eliminarFotoFactura(p.getFotoFactura(), folder); 
            }
            if (p.getFotoProducto() != null && !p.getFotoProducto().isEmpty()) {
                eliminarFotoFactura(p.getFotoProducto(), folder); 
            }
        });
        productoRepository.deleteById(idProducto);
    }

    @Transactional
    public void eliminarServicio(Integer idServicio) {
        servicioRepository.deleteById(idServicio);
    }
    
    @Transactional
    public boolean eliminarIngreso(Integer idIngreso) {
        if (ingresoRepository.existsById(idIngreso)) {
            ingresoRepository.deleteById(idIngreso);
            return true;
        }
        return false;
    }

    // =======================================================
    // MANEJO DE ARCHIVOS FÍSICOS
    // =======================================================

    public String guardarFotoFactura(MultipartFile file, String folderPath) throws IOException {
        if (file == null || file.isEmpty()) return null;

        Path uploadPath = Paths.get(folderPath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath); 
        }

        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_"); 
        Path filePath = uploadPath.resolve(uniqueFilename);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename; 
    }
    
    public void eliminarFotoFactura(String nombreFoto, String folderPath) {
        if (nombreFoto == null || nombreFoto.isEmpty()) return;
        
        String cleanName = nombreFoto.replace("/uploads/", "");
        Path filePath = Paths.get(folderPath).resolve(cleanName);

        try {
            Files.deleteIfExists(filePath);
            System.out.println("Archivo borrado del servidor: " + cleanName);
        } catch (IOException e) {
            System.err.println("Error al eliminar archivo físico: " + e.getMessage());
        }
    }

    // =======================================================
    // MÉTODOS AUXILIARES
    // =======================================================

    @Transactional(readOnly = true)
    public Optional<Ingreso> obtenerIngresoPorProductoId(Integer idProducto) {
        return productoRepository.findById(idProducto).map(Producto::getIngreso);
    }

    @Transactional(readOnly = true)
    public Optional<Ingreso> obtenerIngresoPorServicioId(Integer idServicio) {
        return servicioRepository.findById(idServicio).map(Servicio::getIngreso);
    }
}