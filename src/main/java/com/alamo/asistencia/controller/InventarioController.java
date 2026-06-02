package com.alamo.asistencia.controller;

import com.alamo.asistencia.model.Ingreso;
import com.alamo.asistencia.model.Producto;
import com.alamo.asistencia.model.Servicio;
import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.repository.IIngresoRepository;
import com.alamo.asistencia.repository.IProductoRepository;
import com.alamo.asistencia.repository.IServicioRepository;
import com.alamo.asistencia.service.InventarioService;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/producto")
public class InventarioController {

    @Autowired private InventarioService inventarioService;
    @Autowired private IIngresoRepository ingresoRepository;
    @Autowired private IProductoRepository productoRepository;
    @Autowired private IServicioRepository servicioRepository;
    @Autowired private HttpSession session;

    private static final String FOLDER_PATH = "/root/Asistencia/uploads/Inventario/";

    private Usuario obtenerUsuarioSesion() {
        return (Usuario) session.getAttribute("usuarioLogueado");
    }

    private String generarTiempoUso(LocalDate fechaCompra) {
        if (fechaCompra == null) return "Sin fecha";

        LocalDate hoy = LocalDate.now();
        if (fechaCompra.isAfter(hoy)) return "Nuevo (Adquisición Futura)";

        Period periodo = Period.between(fechaCompra, hoy);
        int anios = periodo.getYears();
        int meses = periodo.getMonths();

        if (anios == 0 && meses == 0) return "Nuevo (Menos de 1 mes)";

        StringBuilder resultado = new StringBuilder();
        if (anios > 0) {
            resultado.append(anios).append(anios == 1 ? " año" : " años");
        }
        if (meses > 0) {
            if (anios > 0) resultado.append(" y ");
            resultado.append(meses).append(meses == 1 ? " mes" : " meses");
        }

        return resultado.toString();
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private int normalizarSize(int size) {
        if (size == 50 || size == 100) return size;
        return 20;
    }

    // =======================================================
    // ✅ VISTA GLOBAL DE PRODUCTOS CON FILTROS + PAGINACIÓN
    // =======================================================
    @GetMapping("/global")
    public String verInventarioGlobal(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") String local,
            @RequestParam(required = false, defaultValue = "") String prop,
            @RequestParam(required = false, defaultValue = "") String buyer,
            @RequestParam(required = false, defaultValue = "") String resp,
            @RequestParam(required = false, defaultValue = "") String emisor,
            @RequestParam(required = false, defaultValue = "") String state,
            @RequestParam(required = false, defaultValue = "") String assign,
            Model model
    ) {
        Usuario usuarioActual = obtenerUsuarioSesion();
        if (usuarioActual == null) return "redirect:/usuarios/cargarLogin";

        page = Math.max(page, 0);
        size = normalizarSize(size);

        search = limpiar(search);
        local = limpiar(local);
        prop = limpiar(prop);
        buyer = limpiar(buyer);
        resp = limpiar(resp);
        emisor = limpiar(emisor);
        state = limpiar(state);
        assign = limpiar(assign);

        Pageable pageable = PageRequest.of(page, size);

        Page<Producto> productosPage = productoRepository.buscarFiltradosGlobal(
                search, local, prop, buyer, resp, emisor, state, assign, pageable
        );

        Double totalInventarioGlobal = productoRepository.sumarTotalFiltradoGlobal(
                search, local, prop, buyer, resp, emisor, state, assign
        );

        model.addAttribute("u", usuarioActual);
        model.addAttribute("moduloActivo", "historial-global");

        model.addAttribute("productos", productosPage.getContent());
        model.addAttribute("currentPage", productosPage.getNumber());
        model.addAttribute("totalPages", productosPage.getTotalPages());
        model.addAttribute("totalItems", productosPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("totalInventarioGlobal", totalInventarioGlobal != null ? totalInventarioGlobal : 0.0);

        model.addAttribute("search", search);
        model.addAttribute("local", local);
        model.addAttribute("prop", prop);
        model.addAttribute("buyer", buyer);
        model.addAttribute("resp", resp);
        model.addAttribute("emisor", emisor);
        model.addAttribute("state", state);
        model.addAttribute("assign", assign);

        model.addAttribute("locales", productoRepository.obtenerLocales());
        model.addAttribute("propiedades", productoRepository.obtenerPropiedades());
        model.addAttribute("compradores", productoRepository.obtenerCompradores());
        model.addAttribute("responsables", productoRepository.obtenerResponsables());
        model.addAttribute("emisores", productoRepository.obtenerEmisores());

        return "listarproductos";
    }

    // =======================================================
    // ✅ EXPORTAR TODO A EXCEL
    // =======================================================
    @GetMapping("/global/exportar")
    public ResponseEntity<byte[]> exportarTodoExcel() throws IOException {
        Usuario usuarioActual = obtenerUsuarioSesion();
        if (usuarioActual == null) {
            return ResponseEntity.status(401).build();
        }

        List<Producto> productos = productoRepository.listarFiltradosGlobal(
                null, null, null, null, null, null, null, null
        );

        byte[] excel = generarExcelProductos(productos);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        ));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("inventario_global.xlsx")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(excel);
    }

    // =======================================================
    // ✅ EXPORTAR FILTRADO A EXCEL
    // =======================================================
    @GetMapping("/global/exportar-filtrado")
    public ResponseEntity<byte[]> exportarFiltradoExcel(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") String local,
            @RequestParam(required = false, defaultValue = "") String prop,
            @RequestParam(required = false, defaultValue = "") String buyer,
            @RequestParam(required = false, defaultValue = "") String resp,
            @RequestParam(required = false, defaultValue = "") String emisor,
            @RequestParam(required = false, defaultValue = "") String state,
            @RequestParam(required = false, defaultValue = "") String assign
    ) throws IOException {
        Usuario usuarioActual = obtenerUsuarioSesion();
        if (usuarioActual == null) {
            return ResponseEntity.status(401).build();
        }

        search = limpiar(search);
        local = limpiar(local);
        prop = limpiar(prop);
        buyer = limpiar(buyer);
        resp = limpiar(resp);
        emisor = limpiar(emisor);
        state = limpiar(state);
        assign = limpiar(assign);

        List<Producto> productos = productoRepository.listarFiltradosGlobal(
                search, local, prop, buyer, resp, emisor, state, assign
        );

        byte[] excel = generarExcelProductos(productos);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        ));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("inventario_filtrado.xlsx")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(excel);
    }

    private byte[] generarExcelProductos(List<Producto> productos) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Inventario");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Nombre");
            header.createCell(2).setCellValue("Código Factura");
            header.createCell(3).setCellValue("Propiedad");
            header.createCell(4).setCellValue("Local");
            header.createCell(5).setCellValue("Estado");
            header.createCell(6).setCellValue("Cantidad");
            header.createCell(7).setCellValue("Precio Unitario");
            header.createCell(8).setCellValue("Total");
            header.createCell(9).setCellValue("Serie / IMEI");
            header.createCell(10).setCellValue("Responsable");
            header.createCell(11).setCellValue("Comprador");
            header.createCell(12).setCellValue("Emisor");
            header.createCell(13).setCellValue("Cliente");
            header.createCell(14).setCellValue("RUC");
            header.createCell(15).setCellValue("Fecha Compra");
            header.createCell(16).setCellValue("Tiempo Uso");
            header.createCell(17).setCellValue("Tipo Transferencia");
            header.createCell(18).setCellValue("Garantía");
            header.createCell(19).setCellValue("Descripción Breve");
            header.createCell(20).setCellValue("Descripción Producto");
            header.createCell(21).setCellValue("Aplica Asignación");

            int rowIdx = 1;
            for (Producto p : productos) {
                Row row = sheet.createRow(rowIdx++);

                double precio = p.getPrecio() != null ? p.getPrecio() : 0.0;
                double cantidad = p.getCantidad() != null ? p.getCantidad() : 0.0;
                double total = precio * cantidad;

                row.createCell(0).setCellValue(p.getIdProducto() != null ? p.getIdProducto() : 0);
                row.createCell(1).setCellValue(p.getNombre() != null ? p.getNombre() : "");
                row.createCell(2).setCellValue(p.getCodigoFactura() != null ? p.getCodigoFactura() : "");
                row.createCell(3).setCellValue(p.getPropiedad() != null ? p.getPropiedad() : "");
                row.createCell(4).setCellValue(p.getLocalDestino() != null ? p.getLocalDestino() : "");
                row.createCell(5).setCellValue(p.getEstadoProducto() != null ? p.getEstadoProducto() : "");
                row.createCell(6).setCellValue(cantidad);
                row.createCell(7).setCellValue(precio);
                row.createCell(8).setCellValue(total);
                row.createCell(9).setCellValue(p.getNumeroSerie() != null ? p.getNumeroSerie() : "");
                row.createCell(10).setCellValue(p.getResponsable() != null ? p.getResponsable() : "");
                row.createCell(11).setCellValue(p.getComprador() != null ? p.getComprador() : "");
                row.createCell(12).setCellValue(p.getEmisor() != null ? p.getEmisor() : "");
                row.createCell(13).setCellValue(p.getCliente() != null ? p.getCliente() : "");
                row.createCell(14).setCellValue(p.getNumeroRuc() != null ? p.getNumeroRuc() : "");
                row.createCell(15).setCellValue(p.getFechaCompra() != null ? p.getFechaCompra().toString() : "");
                row.createCell(16).setCellValue(p.getTiempoUso() != null ? p.getTiempoUso() : "");
                row.createCell(17).setCellValue(p.getTipoTransferencia() != null ? p.getTipoTransferencia() : "");
                row.createCell(18).setCellValue(p.getGarantia() != null ? p.getGarantia() : "");
                row.createCell(19).setCellValue(p.getDescripcionBreve() != null ? p.getDescripcionBreve() : "");
                row.createCell(20).setCellValue(p.getDescripcionProducto() != null ? p.getDescripcionProducto() : "");
                row.createCell(21).setCellValue(p.getAplicaAsignacion() != null ? p.getAplicaAsignacion() : "");
            }

            for (int i = 0; i <= 21; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // =======================================================
    // ✅ VISTA GLOBAL DE SERVICIOS
    // =======================================================
    @GetMapping("/servicios/global")
    public String verServiciosGlobal(Model model) {
        Usuario usuarioActual = obtenerUsuarioSesion();
        if (usuarioActual == null) return "redirect:/usuarios/cargarLogin";

        model.addAttribute("u", usuarioActual);
        model.addAttribute("moduloActivo", "servicios-global");

        List<Servicio> servicios = servicioRepository.findAll();
        servicios.sort((s1, s2) -> {
            if (s1.getFecha() == null || s2.getFecha() == null) return 0;
            return s2.getFecha().compareTo(s1.getFecha());
        });

        model.addAttribute("servicios", servicios);
        return "listarservicios";
    }

    // =======================================================
    // ✅ VISTA DE GESTIÓN CONTABLE (CARGA Y EDICIÓN)
    // =======================================================
    @GetMapping({ "", "/", "/{idIngreso}" })
    public String verInventario(
            @PathVariable(value = "idIngreso", required = false) Integer idIngresoPath,
            @RequestParam(value = "idIngreso", required = false) Integer idIngresoParam,
            @RequestParam(value = "idProducto", required = false) Integer idProducto,
            @RequestParam(value = "idServicio", required = false) Integer idServicio,
            Model model
    ) {
        Usuario usuarioActual = obtenerUsuarioSesion();
        if (usuarioActual == null) return "redirect:/usuarios/cargarLogin";

        model.addAttribute("u", usuarioActual);
        model.addAttribute("moduloActivo", "inventario");

        Integer idCalculado = idIngresoPath != null ? idIngresoPath : idIngresoParam;

        List<Ingreso> ingresos = ingresoRepository.findAll();
        model.addAttribute("ingresos", ingresos);

        if (idCalculado == null && !ingresos.isEmpty()) {
            ingresos.sort(Comparator.comparing(Ingreso::getIdIngreso).reversed());
            idCalculado = ingresos.get(0).getIdIngreso();
        }

        final Integer idIngresoActual = idCalculado;
        model.addAttribute("idIngreso", idIngresoActual);

        if (idIngresoActual != null) {
            ingresoRepository.findById(idIngresoActual).ifPresent(ingreso -> {
                model.addAttribute("ingreso", ingreso);
                model.addAttribute("gastoProductos", inventarioService.calcularGastoProductos(idIngresoActual));
                model.addAttribute("gastoServicios", inventarioService.calcularGastoServicios(idIngresoActual));
                model.addAttribute("saldoDisponible", inventarioService.calcularSaldoDisponible(idIngresoActual));
            });
        }

        if (idProducto != null) {
            productoRepository.findById(idProducto).ifPresent(p -> model.addAttribute("producto", p));
        } else {
            model.addAttribute("producto", new Producto());
        }

        if (idServicio != null) {
            servicioRepository.findById(idServicio).ifPresent(s -> model.addAttribute("servicio", s));
        } else {
            model.addAttribute("servicio", new Servicio());
        }

        return "producto";
    }

    // =======================================================
    // ✅ GUARDAR / ACTUALIZAR PRODUCTO
    // =======================================================
    @PostMapping("/agregar")
    public String guardarProducto(
            @ModelAttribute Producto producto,
            @RequestParam(value = "fotoFacturaFile", required = false) MultipartFile fotoFacturaFile,
            @RequestParam(value = "fotoProductoFile", required = false) MultipartFile fotoProductoFile,
            @RequestParam(value = "eliminarFotoFactura", required = false, defaultValue = "false") boolean eliminarFactura,
            @RequestParam(value = "eliminarFotoProducto", required = false, defaultValue = "false") boolean eliminarActivo,
            RedirectAttributes ra
    ) {
        if (producto.getIngreso() == null || producto.getIngreso().getIdIngreso() == null) {
            ra.addFlashAttribute("error", "Error: El ID del ingreso no puede ser nulo.");
            return "redirect:/producto";
        }

        Ingreso ingreso = ingresoRepository.findById(producto.getIngreso().getIdIngreso()).orElse(null);
        if (ingreso == null) return "redirect:/producto";

        producto.setTiempoUso(generarTiempoUso(producto.getFechaCompra()));

        if (producto.getIdProducto() != null) {
            productoRepository.findById(producto.getIdProducto()).ifPresent(pOrig -> {
                if (fotoFacturaFile == null || fotoFacturaFile.isEmpty()) {
                    producto.setFotoFactura(pOrig.getFotoFactura());
                }
                if (fotoProductoFile == null || fotoProductoFile.isEmpty()) {
                    producto.setFotoProducto(pOrig.getFotoProducto());
                }
            });
        }

        if (eliminarFactura && producto.getFotoFactura() != null) {
            inventarioService.eliminarFotoFactura(producto.getFotoFactura(), FOLDER_PATH);
            producto.setFotoFactura(null);
        }

        if (eliminarActivo && producto.getFotoProducto() != null) {
            inventarioService.eliminarFotoFactura(producto.getFotoProducto(), FOLDER_PATH);
            producto.setFotoProducto(null);
        }

        try {
            if (fotoFacturaFile != null && !fotoFacturaFile.isEmpty()) {
                producto.setFotoFactura(inventarioService.guardarFotoFactura(fotoFacturaFile, FOLDER_PATH));
            }
            if (fotoProductoFile != null && !fotoProductoFile.isEmpty()) {
                producto.setFotoProducto(inventarioService.guardarFotoFactura(fotoProductoFile, FOLDER_PATH));
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al procesar imágenes: " + e.getMessage());
            return "redirect:/producto/" + ingreso.getIdIngreso();
        }

        producto.setIngreso(ingreso);
        inventarioService.guardarProducto(producto);

        ra.addFlashAttribute("success", "Producto guardado correctamente.");
        return "redirect:/producto/" + ingreso.getIdIngreso();
    }

    // =======================================================
    // ✅ GESTIÓN DE INGRESOS MENSUALES
    // =======================================================
    @PostMapping("/ingreso/agregar")
    public String agregarIngreso(
            @RequestParam("mes") String mesStr,
            @RequestParam("monto") Double monto,
            RedirectAttributes ra
    ) {
        LocalDate mes = YearMonth.parse(mesStr, DateTimeFormatter.ofPattern("yyyy-MM")).atDay(1);

        if (inventarioService.obtenerIngresoPorMes(mes) != null) {
            ra.addFlashAttribute("error", "Ya existe este mes registrado.");
            return "redirect:/producto";
        }

        Ingreso nuevo = new Ingreso();
        nuevo.setMes(mes);
        nuevo.setMonto(monto);
        ingresoRepository.save(nuevo);

        return "redirect:/producto/" + nuevo.getIdIngreso();
    }

    @PostMapping("/monto/editar")
    public String editarMonto(
            @RequestParam("idIngreso") Integer id,
            @RequestParam("monto") Double monto,
            @RequestParam("mes") String mesStr
    ) {
        ingresoRepository.findById(id).ifPresent(i -> {
            i.setMonto(monto);
            i.setMes(YearMonth.parse(mesStr, DateTimeFormatter.ofPattern("yyyy-MM")).atDay(1));
            ingresoRepository.save(i);
        });
        return "redirect:/producto/" + id;
    }

    // =======================================================
    // ✅ GESTIÓN DE SERVICIOS
    // =======================================================
    @PostMapping("/servicio/agregar")
    public String guardarServicio(
            @ModelAttribute Servicio servicio,
            @RequestParam(value = "fotoComprobanteFile", required = false) MultipartFile fotoComprobanteFile,
            @RequestParam(value = "eliminarFotoComprobante", required = false, defaultValue = "false") boolean eliminarFotoComprobante,
            RedirectAttributes ra
    ) {
        if (servicio.getIngreso() == null || servicio.getIngreso().getIdIngreso() == null) {
            ra.addFlashAttribute("error", "Error: El ID del ingreso no puede ser nulo.");
            return "redirect:/producto";
        }

        Ingreso ingreso = ingresoRepository.findById(servicio.getIngreso().getIdIngreso()).orElse(null);
        if (ingreso == null) return "redirect:/producto";

        if (servicio.getIdServicio() != null) {
            servicioRepository.findById(servicio.getIdServicio()).ifPresent(orig -> {
                if (fotoComprobanteFile == null || fotoComprobanteFile.isEmpty()) {
                    servicio.setFotoComprobante(orig.getFotoComprobante());
                }
            });
        }

        if (eliminarFotoComprobante && servicio.getFotoComprobante() != null) {
            inventarioService.eliminarFotoFactura(servicio.getFotoComprobante(), FOLDER_PATH);
            servicio.setFotoComprobante(null);
        }

        try {
            if (fotoComprobanteFile != null && !fotoComprobanteFile.isEmpty()) {
                servicio.setFotoComprobante(inventarioService.guardarFotoFactura(fotoComprobanteFile, FOLDER_PATH));
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al procesar comprobante: " + e.getMessage());
            return "redirect:/producto/" + ingreso.getIdIngreso();
        }

        servicio.setIngreso(ingreso);
        inventarioService.guardarServicio(servicio);

        ra.addFlashAttribute("success", "Servicio guardado correctamente.");
        return "redirect:/producto/" + ingreso.getIdIngreso();
    }

    // =======================================================
    // ✅ ELIMINAR SERVICIO
    // =======================================================
    @PostMapping("/servicio/eliminar/{id}")
    public String eliminarServicio(@PathVariable("id") Integer id) {
        Optional<Servicio> s = servicioRepository.findById(id);
        if (s.isPresent()) {
            Integer idIng = s.get().getIngreso().getIdIngreso();
            inventarioService.eliminarServicio(id);
            return "redirect:/producto/" + idIng;
        }
        return "redirect:/producto";
    }

    // =======================================================
    // ✅ ELIMINAR PRODUCTO
    // =======================================================
    @PostMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Integer id) {
        Optional<Producto> p = productoRepository.findById(id);
        if (p.isPresent()) {
            Integer idIng = p.get().getIngreso().getIdIngreso();
            inventarioService.eliminarProducto(id);
            return "redirect:/producto/" + idIng;
        }
        return "redirect:/producto";
    }
}