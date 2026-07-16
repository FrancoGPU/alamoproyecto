# Plan de Implementación: Sistema de Notificaciones Reales

Este documento detalla el plan de diseño técnico para convertir las notificaciones ficticias del frontend en un sistema funcional en tiempo real de notificaciones internas del sistema (ej. alertar cuando se crea un contrato de alquiler o cuando se abre un ticket de soporte).

---

## 📐 1. Diseño de la Base de Datos (Modelo JPA)

Para persistir las notificaciones y rastrear si un usuario las ha leído, crearemos la entidad `Notificacion` en el backend.

### Entidad `Notificacion.java`
```java
package com.alamo.alquiler.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Integer idNotificacion;

    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    @Column(name = "mensaje", nullable = false, length = 300)
    private String mensaje;

    @Column(name = "leido", nullable = false)
    private boolean leido; // true = leída, false = no leída

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo; // "INFO", "ALERTA", "CONTRATO", "SOPORTE"

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.leido = false;
    }
}
```

---

## ⚙️ 2. Lógica del Servidor (Backend)

### Repositorio `NotificacionRepository.java`
Agregar consultas personalizadas para recuperar solo las notificaciones no leídas o las últimas 10 creadas:
```java
package com.alamo.alquiler.repository;

import com.alamo.alquiler.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
    
    // Obtener notificaciones no leídas ordenadas por fecha reciente
    List<Notificacion> findByLeidoFalseOrderByFechaCreacionDesc();

    // Obtener las últimas 10 notificaciones (leídas o no)
    List<Notificacion> findFirst10ByOrderByFechaCreacionDesc();
}
```

### Controlador `NotificacionController.java`
Definir los endpoints para el consumo del frontend y la gestión de marcado de lectura:
```java
package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.Notificacion;
import com.alamo.alquiler.repository.NotificacionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionRepository repo;

    public NotificacionController(NotificacionRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Notificacion> obtenerNotificaciones() {
        return repo.findFirst10ByOrderByFechaCreacionDesc();
    }

    @GetMapping("/no-leidas")
    public List<Notificacion> obtenerNoLeidas() {
        return repo.findByLeidoFalseOrderByFechaCreacionDesc();
    }

    @PutMapping("/{id}/leer")
    public ResponseEntity<Notificacion> marcarComoLeida(@PathVariable Integer id) {
        return repo.findById(id)
            .map(n -> {
                n.setLeido(true);
                return ResponseEntity.ok(repo.save(n));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/leer-todas")
    public ResponseEntity<Void> marcarTodasComoLeidas() {
        List<Notificacion> noLeidas = repo.findByLeidoFalseOrderByFechaCreacionDesc();
        noLeidas.forEach(n -> n.setLeido(true));
        repo.saveAll(noLeidas);
        return ResponseEntity.ok().build();
    }
}
```

### 🔔 3. Generación Automática de Notificaciones (Eventos)
Para que el sistema sea dinámico, agregaremos lógica en los controladores transaccionales del backend para registrar una notificación cada vez que ocurra un evento importante:

1. **Creación de Contrato (`ContratoAlquilerController.java`):**
   ```java
   // Al guardar con éxito un contrato:
   Notificacion notif = Notificacion.builder()
       .titulo("Nuevo Contrato Registrado")
       .mensaje("Se generó el contrato " + contrato.getCodigo() + " para el cliente " + contrato.getCliente().getNombres())
       .tipo("CONTRATO")
       .build();
   notificacionRepository.save(notif);
   ```
2. **Apertura de Ticket (`SoporteTicketController.java`):**
   ```java
   // Al registrar un ticket de ayuda de un agente:
   Notificacion notif = Notificacion.builder()
       .titulo("Nuevo Ticket de Soporte")
       .mensaje("El usuario " + ticket.getCliente() + " reportó la incidencia: " + ticket.getAsunto())
       .tipo("SOPORTE")
       .build();
   notificacionRepository.save(notif);
   ```

---

## 🌐 4. Consumo en Tiempo Real (Frontend)

Para reflejar los cambios en el frontend sin recargar la página, se sugieren dos opciones técnicas:

### Opción A: Polling de Corto Intervalo (Recomendada por su Simplicidad)
Consiste en realizar peticiones GET en segundo plano a la API cada 30 segundos utilizando un intervalo de React en el componente `Navbar.tsx`:

```typescript
// En Navbar.tsx
const [notificaciones, setNotificaciones] = useState<Notificacion[]>([]);

const cargarNotificaciones = async () => {
  try {
    const res = await api.get('/notificaciones/no-leidas');
    setNotificaciones(res.data);
  } catch (e) {
    console.error("Error al cargar alertas", e);
  }
};

useEffect(() => {
  cargarNotificaciones();
  
  // Consultar notificaciones en background cada 30 segundos
  const interval = setInterval(cargarNotificaciones, 30000);
  return () => clearInterval(interval);
}, []);
```

### Opción B: Server-Sent Events (SSE) o WebSockets (Tiempo Real Puro)
Configurar un canal de eventos en el backend mediante `SseEmitter` en Spring, permitiendo al servidor enviar una señal inmediata al frontend en el momento exacto en que se realiza la inserción en la base de datos, sin necesidad de que el frontend realice peticiones repetitivas.
