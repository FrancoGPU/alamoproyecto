package com.alamo.asistencia.service;

import com.alamo.asistencia.model.Producto;
import com.alamo.asistencia.repository.IProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductoService {

    @Autowired
    private IProductoRepository productoRepo;

    /**
     * Obtiene todos los productos registrados en la base de datos.
     */
    public List<Producto> listarTodo() {
        return productoRepo.findAll(Sort.by(Sort.Direction.DESC, "idProducto"));
    }

    /**
     * Obtiene productos por ingreso mensual.
     */
    public List<Producto> listarPorIngreso(Integer idIngreso) {
        return productoRepo.findByIngreso_IdIngreso(idIngreso);
    }

    /**
     * Obtiene productos filtrados con paginación backend.
     */
    public Page<Producto> listarFiltradosPaginados(
            String search,
            String local,
            String prop,
            String buyer,
            String resp,
            String emisor,
            String state,
            String assign,
            Pageable pageable
    ) {
        return productoRepo.buscarFiltradosGlobal(
                normalizar(search),
                normalizar(local),
                normalizar(prop),
                normalizar(buyer),
                normalizar(resp),
                normalizar(emisor),
                normalizar(state),
                normalizar(assign),
                pageable
        );
    }

    /**
     * Obtiene todos los productos filtrados, sin paginación.
     * Ideal para exportar Excel.
     */
    public List<Producto> listarFiltradosSinPaginacion(
            String search,
            String local,
            String prop,
            String buyer,
            String resp,
            String emisor,
            String state,
            String assign
    ) {
        return productoRepo.listarFiltradosGlobal(
                normalizar(search),
                normalizar(local),
                normalizar(prop),
                normalizar(buyer),
                normalizar(resp),
                normalizar(emisor),
                normalizar(state),
                normalizar(assign)
        );
    }

    /**
     * Suma total del inventario filtrado.
     */
    public Double sumarTotalFiltrado(
            String search,
            String local,
            String prop,
            String buyer,
            String resp,
            String emisor,
            String state,
            String assign
    ) {
        Double total = productoRepo.sumarTotalFiltradoGlobal(
                normalizar(search),
                normalizar(local),
                normalizar(prop),
                normalizar(buyer),
                normalizar(resp),
                normalizar(emisor),
                normalizar(state),
                normalizar(assign)
        );
        return total != null ? total : 0.0;
    }

    /**
     * Combos de filtros.
     */
    public List<String> obtenerLocales() {
        return productoRepo.obtenerLocales();
    }

    public List<String> obtenerPropiedades() {
        return productoRepo.obtenerPropiedades();
    }

    public List<String> obtenerCompradores() {
        return productoRepo.obtenerCompradores();
    }

    public List<String> obtenerResponsables() {
        return productoRepo.obtenerResponsables();
    }

    public List<String> obtenerEmisores() {
        return productoRepo.obtenerEmisores();
    }

    /**
     * Exportar todo sin filtros.
     */
    public List<Producto> listarTodoParaExportar() {
        return productoRepo.listarFiltradosGlobal(null, null, null, null, null, null, null, null);
    }

    private String normalizar(String valor) {
        if (valor == null) return null;
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}