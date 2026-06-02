package com.alamo.asistencia.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files; 
import java.nio.file.Path;  
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter; 
import java.time.temporal.IsoFields; 
import java.time.DayOfWeek;           
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alamo.asistencia.model.Asistencia;
import com.alamo.asistencia.model.Informe;
import com.alamo.asistencia.model.Usuario;
import com.alamo.asistencia.repository.IInformeRepository;

@Service
public class InformeService {

    private final IInformeRepository informeRepo;

    // Ruta base para almacenamiento físico
    private static final String RUTA_BASE = "/root/Asistencia/uploads/Reportes"; 

    public InformeService(IInformeRepository informeRepo) {
        this.informeRepo = informeRepo;
    }

    /**
     * Guarda el archivo físicamente y genera el objeto Informe para la BD.
     */
    public Informe guardarArchivo(MultipartFile archivo, Usuario usuario) throws IOException {
        
        // 1. Validaciones
        if (archivo.isEmpty()) {
            throw new IOException("El archivo está vacío");
        }
        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null || !nombreOriginal.toLowerCase().endsWith(".pdf")) {
            throw new IOException("Solo se permiten archivos PDF");
        }
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new IOException("El archivo debe ser un PDF válido");
        }

        // 2. Preparar variables para la ruta y el nombre
        String nombreUsuario = usuario.getNombreCompletoParaRuta(); 
        String fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String extension = ".pdf"; 

        // 3. Definir la ruta de la carpeta del usuario
        Path rutaUsuarioDir = Paths.get(RUTA_BASE, nombreUsuario);

        // 4. Crear la carpeta si no existe
        if (!Files.exists(rutaUsuarioDir)) {
            Files.createDirectories(rutaUsuarioDir); 
        }

        // 5. Generar el nombre de archivo único
        String nombreBase = nombreUsuario + "_" + fechaHoy;
        String nombreArchivoFinal = nombreBase + extension;
        int contador = 0;
        
        Path rutaCompleta = rutaUsuarioDir.resolve(nombreArchivoFinal);

        while (Files.exists(rutaCompleta)) {
            contador++;
            nombreArchivoFinal = nombreBase + " (" + contador + ")" + extension;
            rutaCompleta = rutaUsuarioDir.resolve(nombreArchivoFinal);
        }

        // 6. Guardar el archivo físicamente
        archivo.transferTo(rutaCompleta.toFile());

        // 7. Crear el objeto Informe
        Informe informe = new Informe();
        informe.setNombreArchivo(nombreArchivoFinal);
        informe.setRutaArchivo(rutaCompleta.toString()); 

        return informe;
    }
    
    // --- MÉTODOS DE PERSISTENCIA Y CONSULTA ---

    public Informe guardarInforme(Informe informe) {
        return informeRepo.save(informe);
    }

    public Informe obtenerPorId(Integer id) {
        return informeRepo.findById(id).orElse(null);
    }

    public List<Informe> listarPorUsuario(Usuario usuario) {
        return informeRepo.findByAsistencia_Usuario(usuario);
    }

    /**
     * Filtra informes por un mes y año específicos.
     */
    public List<Informe> listarPorMesAno(int mes, int anio) {
        LocalDate inicio = LocalDate.of(anio, mes, 1);
        LocalDate fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
        return listarPorRangoFechas(inicio, fin);
    }

    /**
     * Filtra informes por una semana específica del año.
     * Si semana es -1, se puede interpretar como "Últimos 7 días" desde hoy.
     */
    public List<Informe> listarPorSemanaAno(int semana, int anio) {
        LocalDate inicio;
        LocalDate fin;

        if (semana == 777) { // Código especial para "Últimos 7 días"
            fin = LocalDate.now();
            inicio = fin.minusDays(7);
        } else {
            // Cálculo estándar ISO
            inicio = LocalDate.of(anio, 1, 4) 
                    .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, semana)
                    .with(DayOfWeek.MONDAY);
            fin = inicio.plusDays(6); 
        }
        
        return listarPorRangoFechas(inicio, fin);
    }

    /**
     * Método genérico para listar informes en cualquier rango de fechas.
     * Este es el método que garantiza que la consulta sea consistente.
     */
    public List<Informe> listarPorRangoFechas(LocalDate inicio, LocalDate fin) {
        return informeRepo.findByAsistencia_FechaBetween(inicio, fin);
    }

    // --- MÉTODOS DE CÁLCULO ---

    public double calcularHorasTotales(Asistencia asistencia) {
        if (asistencia.getHoraEntrada() != null && asistencia.getHoraSalida() != null) {
            long minutos = java.time.Duration.between(asistencia.getHoraEntrada(), asistencia.getHoraSalida()).toMinutes();
            return minutos / 60.0;
        }
        return 0;
    }
}