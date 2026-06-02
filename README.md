# 🏢 ALAMO - Sistema de Gestión de Asistencia

**Versión:** 1.0  
**Estado:** Proyecto Consolidado y Reorganizado  
**Tecnología:** Java 11+ | Spring Boot | MySQL | Thymeleaf | HTML/CSS/JS

---

## 📋 Descripción General

**ALAMO** es una aplicación web completa de **gestión de asistencia y recursos humanos** que permite:

✅ **Gestión de Usuarios** - Registro, roles, permisos  
✅ **Control de Asistencia** - Registro de entrada/salida, auditoría  
✅ **Administración de Tareas** - Asignación, seguimiento, reportes  
✅ **Gestión de Horarios** - Turnos, disponibilidad  
✅ **Inventario** - Productos y servicios  
✅ **Informes y Reportes** - Generación de reportes en Excel  
✅ **Sistema de Contactos** - CRM básico con etiquetado  
✅ **Permisos Especiales** - Ausencias, permisos extra  
✅ **Dashboard** - Vistas consolidadas y KPIs  

---

## 🏗️ Estructura del Proyecto

```
alamo/
├── src/
│   ├── main/
│   │   ├── java/com/alamo/asistencia/
│   │   │   ├── 📁 config/                  # Configuración de Spring (beans, properties)
│   │   │   ├── 📁 filter/                  # Filtros de seguridad y autenticación
│   │   │   ├── 📁 model/                   # Entidades JPA (Usuario, Asistencia, etc.)
│   │   │   ├── 📁 dto/                     # Data Transfer Objects
│   │   │   ├── 📁 repository/              # Interfaces de acceso a datos
│   │   │   ├── 📁 service/                 # Lógica de negocio
│   │   │   ├── 📁 controller/              # Controladores REST/MVC
│   │   │   ├── 📁 exception/               # Excepciones personalizadas
│   │   │   ├── 📁 util/                    # Clases utilitarias
│   │   │   └── AsistenciaAlamoApplication.java  # Clase principal
│   │   │
│   │   └── resources/
│   │       ├── application.properties      # Propiedades de la aplicación
│   │       ├── 📁 static/                  # Recursos estáticos
│   │       │   ├── css/                    # Hojas de estilo
│   │       │   ├── js/                     # Scripts JavaScript
│   │       │   ├── img/                    # Imágenes
│   │       │   └── index.html              # Página de inicio
│   │       └── 📁 templates/               # Templates Thymeleaf
│   │           ├── login.html              # Login
│   │           ├── menu.html               # Menú principal
│   │           ├── perfil.html             # Perfil de usuario
│   │           ├── usuarios.html           # Gestión de usuarios
│   │           ├── asignartareas.html      # Asignación de tareas
│   │           ├── mistareas.html          # Mis tareas
│   │           ├── agenda.html             # Agenda
│   │           ├── reportes.html           # Reportes
│   │           ├── informes.html           # Informes
│   │           ├── Horarios.html           # Gestión de horarios
│   │           ├── listarproductos.html    # Inventario
│   │           ├── fragmentos/
│   │           │   └── sidebar.html        # Componente reutilizable
│   │           └── ... (otros templates)
│   │
│   └── test/                               # Tests unitarios e integración
│       └── java/com/alamo/asistencia/
│
├── docker/                                 # Configuración Docker
│   ├── Dockerfile                          # Imagen de la aplicación
│   └── docker-compose.yml                  # Orquestación MySQL + App
│
├── database/                               # Scripts de base de datos
│   └── *.sql                               # Esquemas, backups, migraciones
│
├── docs/                                   # Documentación del proyecto
│   ├── README_ORGANIZACION.md              # Guía de organización
│   ├── QUICK_START.md                      # Inicio rápido
│   ├── INTEGRACION.md                      # Guía de integración
│   ├── GUIA_GIT.md                         # Workflow Git
│   └── ... (otros documentos)
│
├── pom.xml                                 # Configuración Maven
└── README.md                               # Este archivo

```

---

## 🗂️ Capas de la Aplicación

### **1. Capa de Presentación (Frontend)**
- **Ubicación:** `src/main/resources/templates/` y `src/main/resources/static/`
- **Tecnología:** HTML, CSS, JavaScript, Thymeleaf
- **Responsabilidades:** 
  - Interfaces de usuario
  - Formularios interactivos
  - Reportes visuales

### **2. Capa de Controladores (REST/MVC)**
- **Ubicación:** `src/main/java/.../controller/`
- **Ejemplos:** `UsuarioController.java`, `AsistenciaController.java`
- **Responsabilidades:** 
  - Rutas HTTP
  - Validación de entrada
  - Comunicación con servicios

### **3. Capa de Servicios (Lógica de Negocio)**
- **Ubicación:** `src/main/java/.../service/`
- **Ejemplos:** `UsuarioService.java`, `AsistenciaService.java`
- **Responsabilidades:**
  - Lógica de negocio
  - Orquestación de operaciones
  - Transformación de datos

### **4. Capa de Datos (Persistencia)**
- **Ubicación:** `src/main/java/.../repository/`, `model/`, `dto/`
- **Responsabilidades:**
  - Acceso a base de datos
  - Consultas optimizadas
  - Mapeo ORM con JPA

### **5. Capa de Configuración**
- **Ubicación:** `src/main/java/.../config/`
- **Responsabilidades:**
  - Beans de Spring
  - Configuración CORS
  - Configuración de seguridad

---

## 📊 Entidades Principales

| Entidad | Descripción |
|---------|-------------|
| **Usuario** | Usuarios del sistema con roles |
| **Asistencia** | Registros de entrada/salida |
| **AsistenciaAudit** | Auditoría de cambios |
| **Tarea** | Tareas asignadas a usuarios |
| **Horario** | Horarios de trabajo |
| **Informe** | Informes generados |
| **Contacto** | Directorio de contactos |
| **Producto** | Inventario de productos |
| **Servicio** | Servicios ofrecidos |
| **Permiso Extra** | Permisos especiales |
| **Rol** | Roles y permisos |
| **Etiqueta** | Etiquetas para clasificar |

---

## 🚀 Inicio Rápido

### Prerequisitos
- Java 11 o superior
- Maven 3.6+
- MySQL 5.7+
- Docker (opcional)

### Instalación Local

```bash
# 1. Clonar el repositorio
git clone <repo-url>
cd alamo

# 2. Construir el proyecto
mvn clean install

# 3. Configurar la base de datos
# Editar src/main/resources/application.properties
# Establecer credenciales de MySQL

# 4. Ejecutar la aplicación
mvn spring-boot:run

# 5. Acceder a la aplicación
# http://localhost:8080
```

### Con Docker

```bash
# Construir y ejecutar con docker-compose
cd docker
docker-compose up -d

# Acceder a la aplicación
# http://localhost:8080
```

---

## 🔧 Configuración

### application.properties

```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/asistencia_alamo
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update

# Puerto
server.port=8080

# Thymeleaf
spring.thymeleaf.cache=false
```

---

## 📦 Dependencias Principales

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

---

## 📝 Workflow de Desarrollo

### Crear una Nueva Funcionalidad

1. **Modelo (JPA Entity)**
   ```
   src/main/java/.../model/MiEntidad.java
   ```

2. **Repositorio**
   ```
   src/main/java/.../repository/IMiEntidadRepository.java
   ```

3. **DTO** (si es necesario)
   ```
   src/main/java/.../dto/MiEntidadDTO.java
   ```

4. **Servicio**
   ```
   src/main/java/.../service/MiEntidadService.java
   ```

5. **Controlador**
   ```
   src/main/java/.../controller/MiEntidadController.java
   ```

6. **Template/Vistas**
   ```
   src/main/resources/templates/mientidad.html
   ```

---

## 🐛 Solución de Problemas

### Puerto 8080 en uso
```bash
# Cambiar en application.properties
server.port=8081
```

### Error de conexión a BD
```bash
# Verificar credenciales en application.properties
# Asegurar que MySQL está corriendo
mysql -u root -p
```

### Limpiar caché de Maven
```bash
mvn clean
rm -rf ~/.m2/repository
mvn install
```

---

## 👥 Contribuyentes

- **Parte 1 (Infraestructura):** Integrante 1
- **Parte 2 (Base de Datos):** Integrante 2
- **Parte 3 (Backend):** Integrante 3
- **Parte 4 (Frontend):** Integrante 4

---

## 📚 Documentación Adicional

Consulta la carpeta `docs/` para:
- `QUICK_START.md` - Inicio rápido
- `README_ORGANIZACION.md` - Detalles de organización
- `INTEGRACION.md` - Integración de componentes
- `GUIA_GIT.md` - Workflow Git

---

## 📄 Licencia

Proyecto educativo de ALAMO

---

## ✨ Cambios Recientes (Reorganización)

**Consolidación en estructura Maven estándar:**
- ✅ Unificación de 4 partes separadas en un único proyecto
- ✅ Estructura clara por capas
- ✅ Separación de responsabilidades mejorada
- ✅ Mejor mantenibilidad del código
- ✅ Facilita CI/CD e integración

**Nuevas carpetas:**
- `docker/` - Configuración de contenedores
- `database/` - Scripts SQL
- `docs/` - Documentación centralizada
- `exception/` - Excepciones personalizadas
- `util/` - Clases utilitarias

