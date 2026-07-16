package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.ContratoAlquiler;
import com.alamo.alquiler.model.Usuario;
import com.alamo.alquiler.model.Vehiculo;
import com.alamo.alquiler.repository.ContratoAlquilerRepository;
import com.alamo.alquiler.repository.UsuarioRepository;
import com.alamo.alquiler.repository.VehiculoRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ContratoAlquilerRepository contratoRepository;

    public ReporteController(UsuarioRepository usuarioRepository, 
                             VehiculoRepository vehiculoRepository, 
                             ContratoAlquilerRepository contratoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.contratoRepository = contratoRepository;
    }

    // --- USUARIOS ---

    @GetMapping("/usuarios/excel")
    public ResponseEntity<byte[]> exportarUsuariosExcel() throws IOException {
        List<Usuario> lista = usuarioRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Usuarios");

            // Cabecera
            Row headerRow = sheet.createRow(0);
            String[] cabeceras = {"ID", "Nombres", "Apellidos", "Correo", "Rol"};
            for (int i = 0; i < cabeceras.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(cabeceras[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Datos
            int rowIdx = 1;
            for (Usuario u : lista) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(u.getIdUsuario() != null ? u.getIdUsuario() : 0);
                row.createCell(1).setCellValue(u.getNombres() != null ? u.getNombres() : "");
                row.createCell(2).setCellValue((u.getApellidoPaterno() != null ? u.getApellidoPaterno() : "") + " " + (u.getApellidoMaterno() != null ? u.getApellidoMaterno() : ""));
                row.createCell(3).setCellValue(u.getCorreo() != null ? u.getCorreo() : "");
                row.createCell(4).setCellValue(u.getRol() != null && u.getRol().getNombre() != null ? u.getRol().getNombre() : "");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_usuarios.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(out.toByteArray());
        }
    }

    @GetMapping("/usuarios/pdf")
    public ResponseEntity<byte[]> exportarUsuariosPdf() {
        List<Usuario> lista = usuarioRepository.findAll();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc);

            doc.add(new Paragraph("Reporte de Usuarios - Álamo Rent-A-Car").setBold().setFontSize(16));
            doc.add(new Paragraph(" "));

            float[] columnWidths = {1f, 3f, 4f, 4f, 2f};
            Table table = new Table(columnWidths);

            table.addHeaderCell("ID");
            table.addHeaderCell("Nombres");
            table.addHeaderCell("Apellidos");
            table.addHeaderCell("Correo");
            table.addHeaderCell("Rol");

            for (Usuario u : lista) {
                table.addCell(String.valueOf(u.getIdUsuario() != null ? u.getIdUsuario() : ""));
                table.addCell(u.getNombres() != null ? u.getNombres() : "");
                table.addCell((u.getApellidoPaterno() != null ? u.getApellidoPaterno() : "") + " " + (u.getApellidoMaterno() != null ? u.getApellidoMaterno() : ""));
                table.addCell(u.getCorreo() != null ? u.getCorreo() : "");
                table.addCell(u.getRol() != null ? u.getRol().getNombre() : "");
            }

            doc.add(table);
            doc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_usuarios.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }

    // --- VEHICULOS ---

    @GetMapping("/vehiculos/excel")
    public ResponseEntity<byte[]> exportarVehiculosExcel() throws IOException {
        List<Vehiculo> lista = vehiculoRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Vehículos");

            Row headerRow = sheet.createRow(0);
            String[] cabeceras = {"ID", "Placa", "Marca", "Modelo", "Año", "Categoría"};
            for (int i = 0; i < cabeceras.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(cabeceras[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowIdx = 1;
            for (Vehiculo v : lista) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(v.getIdVehiculo() != null ? v.getIdVehiculo() : 0);
                row.createCell(1).setCellValue(v.getPlaca() != null ? v.getPlaca() : "");
                row.createCell(2).setCellValue(v.getMarca() != null ? v.getMarca() : "");
                row.createCell(3).setCellValue(v.getModelo() != null ? v.getModelo() : "");
                row.createCell(4).setCellValue(v.getAnio() != null ? v.getAnio() : 0);
                row.createCell(5).setCellValue(v.getCategoria() != null && v.getCategoria().getTipo() != null ? v.getCategoria().getTipo() : "");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_vehiculos.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(out.toByteArray());
        }
    }

    @GetMapping("/vehiculos/pdf")
    public ResponseEntity<byte[]> exportarVehiculosPdf() {
        List<Vehiculo> lista = vehiculoRepository.findAll();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc);

            doc.add(new Paragraph("Reporte de Vehículos - Álamo Rent-A-Car").setBold().setFontSize(16));
            doc.add(new Paragraph(" "));

            float[] columnWidths = {1f, 2f, 3f, 3f, 2f, 3f};
            Table table = new Table(columnWidths);

            table.addHeaderCell("ID");
            table.addHeaderCell("Placa");
            table.addHeaderCell("Marca");
            table.addHeaderCell("Modelo");
            table.addHeaderCell("Año");
            table.addHeaderCell("Categoría");

            for (Vehiculo v : lista) {
                table.addCell(String.valueOf(v.getIdVehiculo() != null ? v.getIdVehiculo() : ""));
                table.addCell(v.getPlaca() != null ? v.getPlaca() : "");
                table.addCell(v.getMarca() != null ? v.getMarca() : "");
                table.addCell(v.getModelo() != null ? v.getModelo() : "");
                table.addCell(v.getAnio() != null ? String.valueOf(v.getAnio()) : "");
                table.addCell(v.getCategoria() != null ? v.getCategoria().getTipo() : "");
            }

            doc.add(table);
            doc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_vehiculos.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }

    // --- CONTRATOS ---

    @GetMapping("/contratos/excel")
    public ResponseEntity<byte[]> exportarContratosExcel() throws IOException {
        List<ContratoAlquiler> lista = contratoRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Contratos");

            Row headerRow = sheet.createRow(0);
            String[] cabeceras = {"ID", "Código", "Fecha Inicio", "Fecha Fin", "Vehículo", "Cliente", "Monto Total"};
            for (int i = 0; i < cabeceras.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(cabeceras[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowIdx = 1;
            for (ContratoAlquiler c : lista) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getIdContrato() != null ? c.getIdContrato() : 0);
                row.createCell(1).setCellValue(c.getCodigo() != null ? c.getCodigo() : "");
                row.createCell(2).setCellValue(c.getFechaInicio() != null ? c.getFechaInicio().toString() : "");
                row.createCell(3).setCellValue(c.getFechaFin() != null ? c.getFechaFin().toString() : "");
                row.createCell(4).setCellValue(c.getVehiculo() != null ? (c.getVehiculo().getMarca() + " " + c.getVehiculo().getModelo() + " [" + c.getVehiculo().getPlaca() + "]") : "");
                row.createCell(5).setCellValue(c.getCliente() != null ? (c.getCliente().getNombres() + " " + c.getCliente().getApellidoPaterno()) : "");
                row.createCell(6).setCellValue(c.getMontoTotal() != null ? c.getMontoTotal().doubleValue() : 0.0);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_contratos.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(out.toByteArray());
        }
    }

    @GetMapping("/contratos/pdf")
    public ResponseEntity<byte[]> exportarContratosPdf() {
        List<ContratoAlquiler> lista = contratoRepository.findAll();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc);

            doc.add(new Paragraph("Reporte de Contratos - Álamo Rent-A-Car").setBold().setFontSize(16));
            doc.add(new Paragraph(" "));

            float[] columnWidths = {1f, 3f, 2.5f, 2.5f, 4f, 4f, 2.5f};
            Table table = new Table(columnWidths);

            table.addHeaderCell("ID");
            table.addHeaderCell("Código");
            table.addHeaderCell("F. Inicio");
            table.addHeaderCell("F. Fin");
            table.addHeaderCell("Vehículo");
            table.addHeaderCell("Cliente");
            table.addHeaderCell("Total");

            for (ContratoAlquiler c : lista) {
                table.addCell(String.valueOf(c.getIdContrato() != null ? c.getIdContrato() : ""));
                table.addCell(c.getCodigo() != null ? c.getCodigo() : "");
                table.addCell(c.getFechaInicio() != null ? c.getFechaInicio().toString() : "");
                table.addCell(c.getFechaFin() != null ? c.getFechaFin().toString() : "");
                table.addCell(c.getVehiculo() != null ? (c.getVehiculo().getMarca() + " " + c.getVehiculo().getModelo()) : "");
                table.addCell(c.getCliente() != null ? (c.getCliente().getNombres() + " " + c.getCliente().getApellidoPaterno()) : "");
                table.addCell(c.getMontoTotal() != null ? String.valueOf(c.getMontoTotal()) : "");
            }

            doc.add(table);
            doc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_contratos.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }
}
