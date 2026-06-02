# 🗂️ MAPEO DE ARCHIVOS POR PARTE

Este documento muestra exactamente qué archivos del proyecto original van en cada PARTE.

---

## 📦 PARTE-1: INFRAESTRUCTURA

### Archivos de Configuración (Raíz)
```
✓ pom.xml                                   → PARTE-1-INFRAESTRUCTURA/
✓ Dockerfile                                → PARTE-1-INFRAESTRUCTURA/
✓ docker-compose.yml                        → PARTE-1-INFRAESTRUCTURA/
✓ HELP.md                                   → PARTE-1-INFRAESTRUCTURA/
✓ mvnw (opcional)                           → PARTE-1-INFRAESTRUCTURA/
✓ mvnw.cmd (opcional)                       → PARTE-1-INFRAESTRUCTURA/
```

### Código Java - Configuración
```
✓ src/main/java/com/alamo/asistencia/
  ├── AsistenciaAlamoApplication.java       → PARTE-1-INFRAESTRUCTURA/src/main/java/.../
  ├── config/                               → PARTE-1-INFRAESTRUCTURA/src/main/java/.../config/
  │   └── (todas las clases de config)
  └── filter/                               → PARTE-1-INFRAESTRUCTURA/src/main/java/.../filter/
      └── (todas las clases de filter)
```

### Recursos
```
✓ src/main/resources/
  └── application.properties                → PARTE-1-INFRAESTRUCTURA/src/main/resources/
```

### Backup Database
```
✓ asistencia-backup-13-03.sql              → PARTE-1-INFRAESTRUCTURA/ (o PARTE-2)
```

---

## 🗄️ PARTE-2: BASE DE DATOS

### Modelos (Entidades JPA)
```
✓ src/main/java/com/alamo/asistencia/model/
  ├── Actividad.java
  ├── Asistencia.java
  ├── AsistenciaAudit.java
  ├── Contacto.java
  ├── ContactoEtiqueta.java
  ├── ContactoEtiquetaId.java
  ├── Etiqueta.java
  ├── Horario.java
  ├── Informe.java
  ├── Ingreso.java
  ├── PermisoExtra.java
  ├── Producto.java
  ├── RevisionInforme.java
  ├── Rol.java
  ├── Saludo.java
  ├── Servicio.java
  ├── Tarea.java
  ├── Turno.java
  ├── Usuario.java
  ├── UsuarioCertificacion.java
  ├── UsuarioExperiencia.java
  ├── UsuarioFormacion.java
  └── UsuarioIdioma.java
  
  → Todas a: PARTE-2-BASE-DE-DATOS/src/main/java/.../model/
```

### Repositorios
```
✓ src/main/java/com/alamo/asistencia/repository/
  ├── ActividadRepository.java
  ├── AsistenciaAuditRepository.java
  ├── AsistenciaRepository.java
  ├── ContactoEtiquetaRepository.java
  ├── ContactoRepository.java
  ├── EtiquetaRepository.java
  ├── HorarioRepository.java
  ├── InformeRepository.java
  ├── PermisoExtraRepository.java
  ├── ProductoRepository.java
  ├── RevisionInformeRepository.java
  ├── RolRepository.java
  ├── SaludoRepository.java
  ├── ServicioRepository.java
  ├── TareaRepository.java
  ├── TurnoRepository.java
  ├── UbigeoRepository.java
  ├── UsuarioCertificacionRepository.java
  ├── UsuarioExperienciaRepository.java
  ├── UsuarioFormacionRepository.java
  ├── UsuarioIdiomaRepository.java
  └── UsuarioRepository.java
  
  → Todas a: PARTE-2-BASE-DE-DATOS/src/main/java/.../repository/
```

### DTOs
```
✓ src/main/java/com/alamo/asistencia/dto/
  └── (todas las clases DTO encontradas)
  
  → Todas a: PARTE-2-BASE-DE-DATOS/src/main/java/.../dto/
```

---

## 🎯 PARTE-3: BACKEND Y SERVICIOS

### Servicios (Lógica de Negocio)
```
✓ src/main/java/com/alamo/asistencia/service/
  └── (todas las clases Service)
  
  → Todas a: PARTE-3-BACKEND-SERVICIOS/src/main/java/.../service/
```

### Controladores (Endpoints)
```
✓ src/main/java/com/alamo/asistencia/controller/
  ├── AgendaController.java
  ├── AsistenciaAuditController.java
  ├── AsistenciaController.java
  ├── ContactoRestController.java
  ├── ContraseñaController.java
  ├── CVController.java
  ├── DescargaReporteController.java
  ├── EtiquetaRestController.java
  ├── HorarioController.java
  ├── InformeController.java
  ├── InventarioController.java
  ├── LoginController.java
  ├── PerfilController.java
  ├── PermisoExtraController.java
  ├── ProductoController.java
  ├── ReporteController.java
  ├── RevisionInformeController.java
  ├── SaludoController.java
  ├── TareaController.java
  ├── UbigeoController.java
  ├── UsuarioController.java
  └── VistaController.java
  
  → Todas a: PARTE-3-BACKEND-SERVICIOS/src/main/java/.../controller/
```

---

## 🎨 PARTE-4: FRONTEND Y UI

### Templates HTML
```
✓ src/main/resources/templates/
  ├── agenda.html                           → PARTE-4-FRONTEND-UI/...
  ├── asignartareas.html                    → PARTE-4-FRONTEND-UI/...
  ├── contraseña.html                       → PARTE-4-FRONTEND-UI/...
  ├── historialdia.html                     → PARTE-4-FRONTEND-UI/...
  ├── historialgeneral.html                 → PARTE-4-FRONTEND-UI/...
  ├── Horarios.html                         → PARTE-4-FRONTEND-UI/...
  ├── informes.html                         → PARTE-4-FRONTEND-UI/...
  ├── listarproductos.html                  → PARTE-4-FRONTEND-UI/...
  ├── listarservicios.html                  → PARTE-4-FRONTEND-UI/...
  ├── login.html                            → PARTE-4-FRONTEND-UI/...
  ├── menu.html                             → PARTE-4-FRONTEND-UI/...
  ├── mistareas.html                        → PARTE-4-FRONTEND-UI/...
  ├── mobile.html                           → PARTE-4-FRONTEND-UI/...
  ├── perfil.html                           → PARTE-4-FRONTEND-UI/...
  ├── producto.html                         → PARTE-4-FRONTEND-UI/...
  ├── registrar.html                        → PARTE-4-FRONTEND-UI/...
  ├── reportes.html                         → PARTE-4-FRONTEND-UI/...
  ├── revisionusuarios.html                 → PARTE-4-FRONTEND-UI/...
  ├── usuarios.html                         → PARTE-4-FRONTEND-UI/...
  └── fragmentos/
      └── sidebar.html                      → PARTE-4-FRONTEND-UI/...
```

### Static Assets - CSS
```
✓ src/main/resources/static/css/
  ├── asignartareas.css                     → PARTE-4-FRONTEND-UI/...
  ├── style.css                             → PARTE-4-FRONTEND-UI/...
  └── (otros CSS)                           → PARTE-4-FRONTEND-UI/...
```

### Static Assets - JavaScript
```
✓ src/main/resources/static/js/
  └── (todos los archivos JS)               → PARTE-4-FRONTEND-UI/...
```

### Static Assets - Imágenes
```
✓ src/main/resources/static/img/
  └── (todas las imágenes)                  → PARTE-4-FRONTEND-UI/...
```

### Index y otros
```
✓ src/main/resources/static/index.html      → PARTE-4-FRONTEND-UI/...
```

---

## 🗂️ Directorios a Ignorar / No Mover

```
✗ target/                                   (generado por Maven, se ignora)
✗ .git/                                     (Git, no tocar)
✗ uploads/                                  (archivos de usuario, se mantiene en raíz)
✗ mvnw, mvnw.cmd                            (opcional mover o dejar)
```

---

## 📋 Checklist - Copia de Archivos

### Integrante 1 - PARTE-1
- [ ] Copié pom.xml
- [ ] Copié Dockerfile y docker-compose.yml
- [ ] Copié application.properties
- [ ] Copié AsistenciaAlamoApplication.java
- [ ] Copié clases de config/
- [ ] Copié clases de filter/

### Integrante 2 - PARTE-2
- [ ] Copié todas las clases de model/ (25 archivos)
- [ ] Copié todas las interfaces de repository/
- [ ] Copié DTOs si existen

### Integrante 3 - PARTE-3
- [ ] Copié clases de service/
- [ ] Copié todas las clases de controller/ (23 archivos)

### Integrante 4 - PARTE-4
- [ ] Copié todos los HTML de templates/
- [ ] Copié todos los CSS de static/css/
- [ ] Copié todos los JS de static/js/
- [ ] Copié imágenes de static/img/
- [ ] Copié index.html
- [ ] Copié sidebar.html

---

## 🔄 Proceso de Copia Recomendado

```bash
# Para cada integrante:

# 1. Ir a su carpeta PARTE-X-NOMBRE
cd PARTE-X-NOMBRE

# 2. Crear estructura src
mkdir -p src/main/java/com/alamo/asistencia/{...}
mkdir -p src/main/resources/{templates,static/{css,js,img}}

# 3. Copiar archivos (ejemplo)
cp -r ../src/main/java/com/alamo/asistencia/model/* src/main/java/com/alamo/asistencia/model/
cp -r ../src/main/resources/templates/* src/main/resources/templates/

# 4. Verificar en Git
git status

# 5. Hacer commits
git add .
git commit -m "Initial copy of PARTE-X files"
```

---

## ✅ Después de Copiar

1. ✓ Verificar que no hay errores de compilación
2. ✓ Hacer commits organizados
3. ✓ Hacer push a tu rama
4. ✓ Crear Pull Request

---

*Guía de mapeo de archivos - Proyecto Asistencia Alamo*
