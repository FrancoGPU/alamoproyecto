package com.alamo.asistencia.service;

import com.alamo.asistencia.model.Asistencia;
import com.alamo.asistencia.model.Informe;
import com.alamo.asistencia.model.Usuario;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InformesExcelService {

    private final AsistenciaService asistenciaService;
    private final InformeService informeService;
    private final UsuarioService usuarioService;

    public InformesExcelService(AsistenciaService asistenciaService,
                                InformeService informeService,
                                UsuarioService usuarioService) {
        this.asistenciaService = asistenciaService;
        this.informeService = informeService;
        this.usuarioService = usuarioService;
    }

    private static final DateTimeFormatter F_DDMMYYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter F_HHMM = DateTimeFormatter.ofPattern("HH:mm");

    private int horasBigDecimalAMinutos(BigDecimal horas) {
        if (horas == null) return 0;
        return horas.multiply(BigDecimal.valueOf(60))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    private String fmtTime(LocalTime t) {
        return t == null ? "" : t.format(F_HHMM);
    }

    private String fmtMinToHm(int min) {
        int h = min / 60;
        int m = Math.abs(min % 60);
        return h + "h " + m + "m";
    }

    private String safe(String s) { return s == null ? "" : s; }

    public byte[] buildExcel(LocalDate inicio,
                             LocalDate fin,
                             Integer usuarioId,
                             Map<Integer, Boolean> revisados) throws Exception {

        // ===== 1) DATA =====
        List<Asistencia> asistencias;
        if (usuarioId != null) {
            Usuario u = usuarioService.obtenerUsuario(usuarioId).orElse(null);
            asistencias = (u == null)
                    ? new ArrayList<>()
                    : asistenciaService.obtenerHistorialPorRangoUsuario(u, inicio, fin);
        } else {
            asistencias = asistenciaService.obtenerHistorialPorRango(inicio, fin);
        }

        // null safety
        for (Asistencia a : asistencias) {
            if (a.getHorasTrabajadas() == null) a.setHorasTrabajadas(BigDecimal.ZERO);
            if (a.getMinutosExtra() == null) a.setMinutosExtra(0);
            if (a.getMinutosTardanza() == null) a.setMinutosTardanza(0);
        }

        List<Informe> informes = informeService.listarPorRangoFechas(inicio, fin);
        if (usuarioId != null) {
            Integer uid = usuarioId;
            informes = informes.stream()
                    .filter(inf -> inf.getAsistencia() != null
                            && inf.getAsistencia().getUsuario() != null
                            && Objects.equals(inf.getAsistencia().getUsuario().getIdUsuario(), uid))
                    .collect(Collectors.toList());
        }

        Map<Integer, List<Informe>> informesPorAsistenciaId = informes.stream()
                .filter(i -> i.getAsistencia() != null && i.getAsistencia().getIdAsistencia() != null)
                .collect(Collectors.groupingBy(i -> i.getAsistencia().getIdAsistencia()));

        Map<Integer, Long> informesCountPorUsuario = informes.stream()
                .filter(i -> i.getAsistencia() != null && i.getAsistencia().getUsuario() != null)
                .collect(Collectors.groupingBy(i -> i.getAsistencia().getUsuario().getIdUsuario(), Collectors.counting()));

        Map<Integer, List<Asistencia>> asistPorUsuario = asistencias.stream()
                .filter(a -> a.getUsuario() != null)
                .collect(Collectors.groupingBy(a -> a.getUsuario().getIdUsuario(), LinkedHashMap::new, Collectors.toList()));

        Map<Integer, Long> diasUnicos = asistencias.stream()
                .filter(a -> a.getUsuario() != null)
                .collect(Collectors.groupingBy(a -> a.getUsuario().getIdUsuario(),
                        Collectors.mapping(Asistencia::getFecha, Collectors.toSet())))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (long) e.getValue().size()));

        Map<Integer, Integer> baseMin = new HashMap<>();
        Map<Integer, Integer> extraMin = new HashMap<>();
        Map<Integer, Integer> tardMin = new HashMap<>();
        Map<Integer, Integer> diasTarde = new HashMap<>();

        for (var e : asistPorUsuario.entrySet()) {
            Integer uid = e.getKey();
            List<Asistencia> list = e.getValue();

            int b = list.stream().mapToInt(x -> horasBigDecimalAMinutos(x.getHorasTrabajadas())).sum();
            int ex = list.stream().mapToInt(x -> x.getMinutosExtra() != null ? x.getMinutosExtra() : 0).sum();
            int ta = list.stream().mapToInt(x -> x.getMinutosTardanza() != null ? x.getMinutosTardanza() : 0).sum();

            Map<LocalDate, Integer> tardPorDia = list.stream()
                    .collect(Collectors.groupingBy(Asistencia::getFecha,
                            Collectors.summingInt(x -> x.getMinutosTardanza() != null ? x.getMinutosTardanza() : 0)));
            int dT = (int) tardPorDia.values().stream().filter(v -> v != null && v > 0).count();

            baseMin.put(uid, b);
            extraMin.put(uid, ex);
            tardMin.put(uid, ta);
            diasTarde.put(uid, dT);
        }

        // ===== 2) EXCEL =====
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            CellStyle stHeader = headerStyle(wb);
            CellStyle stCell = normalStyle(wb);
            CellStyle stOk = okStyle(wb);
            CellStyle stWarn = warnStyle(wb);

            // ---------- Sheet 1: Resumen ----------
            Sheet s1 = wb.createSheet("Resumen");
            int r = 0;

            Row h = s1.createRow(r++);
            String[] cols = {"Usuario","Días únicos","Horas base","Horas extra","Horas totales","Tardanza","Días tarde","Informes","Revisado"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(stHeader);
            }

            List<Integer> usuariosOrden = new ArrayList<>(asistPorUsuario.keySet());
            usuariosOrden.sort(Comparator.naturalOrder());

            for (Integer uid : usuariosOrden) {
                List<Asistencia> list = asistPorUsuario.get(uid);
                if (list == null || list.isEmpty()) continue;

                Usuario u = list.get(0).getUsuario();
                String nombre = (u != null) ? (safe(u.getNombres()) + " " + safe(u.getApellido_paterno())).trim() : ("ID " + uid);

                int b = baseMin.getOrDefault(uid, 0);
                int ex = extraMin.getOrDefault(uid, 0);
                int tot = b + ex;
                int ta = tardMin.getOrDefault(uid, 0);
                int dT = diasTarde.getOrDefault(uid, 0);
                long dU = diasUnicos.getOrDefault(uid, 0L);
                long infCount = informesCountPorUsuario.getOrDefault(uid, 0L);
                boolean rev = revisados != null && Boolean.TRUE.equals(revisados.get(uid));

                Row row = s1.createRow(r++);
                int c = 0;

                Cell c0 = row.createCell(c++); c0.setCellValue(nombre); c0.setCellStyle(stCell);
                Cell c1 = row.createCell(c++); c1.setCellValue(dU); c1.setCellStyle(stCell);
                Cell c2 = row.createCell(c++); c2.setCellValue(fmtMinToHm(b)); c2.setCellStyle(stCell);
                Cell c3 = row.createCell(c++); c3.setCellValue(fmtMinToHm(ex)); c3.setCellStyle(stCell);
                Cell c4 = row.createCell(c++); c4.setCellValue(fmtMinToHm(tot)); c4.setCellStyle(stCell);
                Cell c5 = row.createCell(c++); c5.setCellValue(fmtMinToHm(ta)); c5.setCellStyle(stCell);
                Cell c6 = row.createCell(c++); c6.setCellValue(dT); c6.setCellStyle(stCell);
                Cell c7 = row.createCell(c++); c7.setCellValue(infCount); c7.setCellStyle(stCell);

                Cell c8 = row.createCell(c);
                c8.setCellValue(rev ? "SI" : "NO");
                c8.setCellStyle(rev ? stOk : stWarn);
            }

            for (int i = 0; i < cols.length; i++) s1.autoSizeColumn(i);

            // ---------- Sheet 2: Detalle ----------
            Sheet s2 = wb.createSheet("Detalle");
            int rd = 0;

            Row hd = s2.createRow(rd++);
            String[] cols2 = {
                    "Usuario","Fecha","Entrada","Salida",
                    "Horas base (horasTrabajadas)","Extra (min)","Total (base+extra)",
                    "Tardanza (min)","Ubicación",
                    "Informes (cantidad)","Informes (nombres)"
            };
            for (int i = 0; i < cols2.length; i++) {
                Cell c = hd.createCell(i);
                c.setCellValue(cols2[i]);
                c.setCellStyle(stHeader);
            }

            asistencias.sort(Comparator
                    .comparing((Asistencia a) -> a.getUsuario() != null ? a.getUsuario().getIdUsuario() : 0)
                    .thenComparing(Asistencia::getFecha));

            for (Asistencia a : asistencias) {
                Usuario u = a.getUsuario();
                String nombre = (u != null) ? (safe(u.getNombres()) + " " + safe(u.getApellido_paterno())).trim() : "";

                int bMin = horasBigDecimalAMinutos(a.getHorasTrabajadas());
                int exMin = a.getMinutosExtra() != null ? a.getMinutosExtra() : 0;
                int totMin = bMin + exMin;

                List<Informe> infs = (a.getIdAsistencia() != null)
                        ? informesPorAsistenciaId.getOrDefault(a.getIdAsistencia(), List.of())
                        : List.of();

                String nombresInf = infs.stream()
                        .map(i -> i.getNombreArchivo() != null ? i.getNombreArchivo() : "")
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.joining(" | "));

                Row row = s2.createRow(rd++);
                int c = 0;

                Cell c0 = row.createCell(c++); c0.setCellValue(nombre); c0.setCellStyle(stCell);
                Cell c1 = row.createCell(c++); c1.setCellValue(a.getFecha() != null ? a.getFecha().format(F_DDMMYYYY) : ""); c1.setCellStyle(stCell);
                Cell c2 = row.createCell(c++); c2.setCellValue(fmtTime(a.getHoraEntrada())); c2.setCellStyle(stCell);
                Cell c3 = row.createCell(c++); c3.setCellValue(fmtTime(a.getHoraSalida())); c3.setCellStyle(stCell);

                Cell c4 = row.createCell(c++); c4.setCellValue(a.getHorasTrabajadas() != null ? a.getHorasTrabajadas().toString() : "0"); c4.setCellStyle(stCell);
                Cell c5 = row.createCell(c++); c5.setCellValue(exMin); c5.setCellStyle(stCell);
                Cell c6 = row.createCell(c++); c6.setCellValue(fmtMinToHm(totMin)); c6.setCellStyle(stCell);

                Cell c7 = row.createCell(c++); c7.setCellValue(a.getMinutosTardanza() != null ? a.getMinutosTardanza() : 0); c7.setCellStyle(stCell);
                Cell c8 = row.createCell(c++); c8.setCellValue(a.getUbicacion() != null ? a.getUbicacion() : ""); c8.setCellStyle(stCell);

                Cell c9 = row.createCell(c++); c9.setCellValue(infs.size()); c9.setCellStyle(stCell);
                Cell c10 = row.createCell(c); c10.setCellValue(nombresInf); c10.setCellStyle(stCell);
            }

            for (int i = 0; i < cols2.length; i++) s2.autoSizeColumn(i);

            wb.write(bos);
            return bos.toByteArray();
        }
    }

    private CellStyle headerStyle(Workbook wb) {
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());

        CellStyle st = wb.createCellStyle();
        st.setFont(f);
        st.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        st.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        st.setAlignment(HorizontalAlignment.CENTER);
        st.setVerticalAlignment(VerticalAlignment.CENTER);
        st.setBorderBottom(BorderStyle.THIN);
        st.setBorderTop(BorderStyle.THIN);
        st.setBorderLeft(BorderStyle.THIN);
        st.setBorderRight(BorderStyle.THIN);
        return st;
    }

    private CellStyle normalStyle(Workbook wb) {
        CellStyle st = wb.createCellStyle();
        st.setVerticalAlignment(VerticalAlignment.CENTER);
        st.setBorderBottom(BorderStyle.THIN);
        st.setBorderTop(BorderStyle.THIN);
        st.setBorderLeft(BorderStyle.THIN);
        st.setBorderRight(BorderStyle.THIN);
        st.setWrapText(true);
        return st;
    }

    private CellStyle okStyle(Workbook wb) {
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.DARK_GREEN.getIndex());
        CellStyle st = normalStyle(wb);
        st.setFont(f);
        st.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        st.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        st.setAlignment(HorizontalAlignment.CENTER);
        return st;
    }

    private CellStyle warnStyle(Workbook wb) {
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.DARK_RED.getIndex());
        CellStyle st = normalStyle(wb);
        st.setFont(f);
        st.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        st.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        st.setAlignment(HorizontalAlignment.CENTER);
        return st;
    }
}
