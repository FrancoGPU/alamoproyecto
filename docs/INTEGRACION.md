# 📚 GUÍA DE INTEGRACIÓN DEL PROYECTO

Este documento explica cómo se integran las 4 partes del proyecto y cómo trabajar de forma simultánea.

---

## 📦 Estructura General del Proyecto

```
asistencia/                          # Raíz del proyecto
├── PARTE-1-INFRAESTRUCTURA/
│   ├── README.md
│   ├── pom.xml                      # ⭐ Configuración Maven
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── src/main/
│       ├── java/.../config/         # Configuraciones Spring
│       ├── java/.../filter/         # Filtros de seguridad
│       └── resources/               # application.properties
│
├── PARTE-2-BASE-DE-DATOS/
│   ├── README.md
│   └── src/main/java/.../
│       ├── model/                   # Entidades JPA
│       ├── repository/              # Interfaces de repositorio
│       └── dto/                     # Data Transfer Objects
│
├── PARTE-3-BACKEND-SERVICIOS/
│   ├── README.md
│   └── src/main/java/.../
│       ├── service/                 # Servicios de negocio
│       └── controller/              # Controladores
│
├── PARTE-4-FRONTEND-UI/
│   ├── README.md
│   └── src/main/resources/
│       ├── templates/               # HTML con Thymeleaf
│       └── static/                  # CSS, JS, imágenes
│
├── INTEGRACION.md                   # 👈 Este archivo
├── GUIA_GIT.md                      # Cómo trabajar con Git
└── pom-integration.xml              # (A crear) POM integrado
```

---

## 🔗 Dependencias Entre Partes

```
┌─────────────────────────────────────────┐
│   PARTE-1: INFRAESTRUCTURA              │  ← PRIMERO
│   (pom.xml, docker-compose, config)     │
└──────────────────┬──────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────┐
│   PARTE-2: BASE DE DATOS                │  ← SEGUNDO
│   (Modelos, Repositorios)               │
└──────────────────┬──────────────────────┘
                   │
        ┌──────────┴──────────┐
        ↓                     ↓
┌──────────────────┐   ┌────────────────────┐
│  PARTE-3         │   │   PARTE-4          │  ← TERCERO
│  BACKEND         │   │   FRONTEND         │
│  (Servicios,     │   │   (HTML, CSS,      │
│   Controllers)   │   │    JavaScript)     │
└──────────────────┘   └────────────────────┘
        ↓                     ↓
        └──────────┬──────────┘
                   ↓
        APLICACIÓN FUNCIONAL
```

---

## 🚀 Pasos de Trabajo Colaborativo

### 1️⃣ **PARTE-1: Infraestructura (Integrante 1)**

**Primer paso:** Configurar la base del proyecto

```bash
# Tareas:
- Copiar pom.xml original a PARTE-1-INFRAESTRUCTURA/
- Copiar Dockerfile, docker-compose.yml
- Copiar application.properties
- Crear clase principal (AsistenciaAlamoApplication.java)
- Copiar filtros de seguridad

# Commits sugeridos:
git add PARTE-1-INFRAESTRUCTURA/pom.xml
git commit -m "feat: setup pom.xml with dependencies"

git add PARTE-1-INFRAESTRUCTURA/Dockerfile
git commit -m "feat: add Docker configuration"

git add PARTE-1-INFRAESTRUCTURA/src/main/resources/application.properties
git commit -m "feat: configure application.properties"

git add PARTE-1-INFRAESTRUCTURA/src/main/java/.../config/
git commit -m "feat: add Spring Boot configuration"

git add PARTE-1-INFRAESTRUCTURA/src/main/java/.../filter/
git commit -m "feat: add security filters"
```

### 2️⃣ **PARTE-2: Base de Datos (Integrante 2)**

**Segundo paso:** Definir las entidades y repositorios

```bash
# Depende de: PARTE-1 (para dependencias del pom.xml)
# Tareas:
- Copiar todas las clases de model/
- Copiar todas las interfaces de repository/
- Crear DTOs principales
- Asegurar anotaciones JPA correctas

# Commits sugeridos:
git add PARTE-2-BASE-DE-DATOS/src/main/java/.../model/
git commit -m "feat: create Usuario and related entities"

git add PARTE-2-BASE-DE-DATOS/src/main/java/.../repository/
git commit -m "feat: create repository interfaces"

git add PARTE-2-BASE-DE-DATOS/src/main/java/.../dto/
git commit -m "feat: create DTOs for API responses"
```

### 3️⃣ **PARTE-3: Backend (Integrante 3)** 
### y **PARTE-4: Frontend (Integrante 4)**

**Tercer paso:** Implementar lógica y UI (pueden trabajar en paralelo)

```bash
# PARTE-3: Backend
git add PARTE-3-BACKEND-SERVICIOS/src/main/java/.../service/
git commit -m "feat: create business logic services"

git add PARTE-3-BACKEND-SERVICIOS/src/main/java/.../controller/
git commit -m "feat: create REST/MVC controllers"

# PARTE-4: Frontend
git add PARTE-4-FRONTEND-UI/src/main/resources/templates/
git commit -m "feat: create HTML templates"

git add PARTE-4-FRONTEND-UI/src/main/resources/static/css/
git commit -m "feat: add styling and CSS"

git add PARTE-4-FRONTEND-UI/src/main/resources/static/js/
git commit -m "feat: add JavaScript functionality"
```

---

## 🔨 Integración Final

Cuando todas las partes estén listas, hacer merge e integración:

### 1. Consolidar en estructura estándar de Spring Boot

```bash
# Crear estructura consolidada
mkdir -p integrated/src/main/java/com/alamo/asistencia/{model,repository,dto,service,controller,config,filter}
mkdir -p integrated/src/main/resources/{templates,static}

# Copiar todos los archivos
cp -r PARTE-1-INFRAESTRUCTURA/pom.xml integrated/
cp -r PARTE-1-INFRAESTRUCTURA/Dockerfile integrated/
cp -r PARTE-2-BASE-DE-DATOS/src/main/java/.../model/* integrated/src/main/java/.../model/
cp -r PARTE-2-BASE-DE-DATOS/src/main/java/.../repository/* integrated/src/main/java/.../repository/
cp -r PARTE-3-BACKEND-SERVICIOS/src/main/java/.../service/* integrated/src/main/java/.../service/
cp -r PARTE-3-BACKEND-SERVICIOS/src/main/java/.../controller/* integrated/src/main/java/.../controller/
cp -r PARTE-4-FRONTEND-UI/src/main/resources/* integrated/src/main/resources/
```

### 2. Hacer merge en Git

```bash
# Agregar todo consolidado
git add integrated/

# Commit de integración
git commit -m "feat: integrate all project parts into unified structure"

# Tag de versión
git tag -a v1.0-integrated -m "Project parts integrated"
```

### 3. Verificar que funciona

```bash
cd integrated/
mvn clean install
mvn spring-boot:run
```

---

## 📋 Checklist de Integración

- [ ] PARTE-1: Infraestructura completada y testeada
- [ ] PARTE-2: Entidades y repositorios compilan sin errores
- [ ] PARTE-3: Servicios y controladores funcionan
- [ ] PARTE-4: Todas las vistas HTML cargan correctamente
- [ ] Todas las dependencias en pom.xml resueltas
- [ ] MySQL corriendo y conectado
- [ ] Aplicación inicia sin errores
- [ ] Endpoints principales responden
- [ ] UI carga y es navegable

---

## 🐛 Posibles Problemas y Soluciones

### Problema: "Class not found" en compile time

**Causa:** Una parte usa clases de otra que no está compilada
**Solución:** Asegurar que las partes se compilan en orden: 1 → 2 → 3 y 4

### Problema: "Cannot find servlet dispatcher"

**Causa:** La clase principal o configuración falta
**Solución:** Verificar que PARTE-1 tiene AsistenciaAlamoApplication.java correctamente

### Problema: "Table doesn't exist"

**Causa:** Las entidades de PARTE-2 no crearon las tablas
**Solución:** Verificar `hibernate.ddl-auto=update` en application.properties

### Problema: "Bean not found"

**Causa:** Servicios o controladores no están siendo detectados por Spring
**Solución:** Verificar anotaciones @Service, @Controller, @RestController

---

## 📚 Referencias y Documentos

- [README PARTE-1](PARTE-1-INFRAESTRUCTURA/README.md) - Infraestructura
- [README PARTE-2](PARTE-2-BASE-DE-DATOS/README.md) - Base de Datos
- [README PARTE-3](PARTE-3-BACKEND-SERVICIOS/README.md) - Backend
- [README PARTE-4](PARTE-4-FRONTEND-UI/README.md) - Frontend
- [GUIA_GIT.md](GUIA_GIT.md) - Estrategia de commits

---

## 🎓 Conclusión

Esta separación permite que:
✅ Cada integrante trabaje de forma independiente
✅ Los commits reflejen la participación real de cada uno
✅ El historial de Git sea claro y organizado
✅ La integración sea limpia y ordenada
✅ Se facilite el mantenimiento futuro

¡Éxito con el proyecto! 🚀
