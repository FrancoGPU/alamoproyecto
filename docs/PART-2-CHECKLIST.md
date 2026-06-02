# ✅ PARTE 2 - BASE DE DATOS

**Integrante:** (Tu nombre aquí)

## ✓ Archivos Incluidos en Esta Carpeta

```
PARTE-2-BASE-DE-DATOS/
├── README.md
├── PART-2-CHECKLIST.md             (este archivo)
├── pom.xml                         ✅ (heredado de PARTE-1)
│
└── src/main/java/com/alamo/asistencia/
    ├── model/                      ✅ 25 Entidades JPA
    │   ├── Usuario.java
    │   ├── Asistencia.java
    │   └── ... (23 más)
    │
    ├── repository/                 ✅ Interfaces Repositorio
    │   ├── UsuarioRepository.java
    │   ├── AsistenciaRepository.java
    │   └── ... (19 más)
    │
    └── dto/                        ✅ Data Transfer Objects
        └── ... (tus DTOs)
```

## 📋 Checklist Antes de Hacer Push

- [ ] Todos los archivos model/ están presentes
- [ ] Todos los archivos repository/ están presentes
- [ ] DTOs están en la carpeta dto/
- [ ] Entidades tienen anotaciones @Entity y @Table
- [ ] Repositorios extienden JpaRepository
- [ ] Relaciones entre entidades están configuradas
- [ ] No hay errores de compilación

## 🚀 Pasos para Push a Repositorio

```bash
# 1. Posicionarse en el directorio del proyecto
cd "c:\Users\Franco\Desktop\asistencia (16--03-26)"

# 2. Actualizar desde main (si PARTE-1 ya fue mergeada)
git pull origin main

# 3. Crear rama
git checkout -b feat/parte-2-base-datos

# 4. Agregar solo ESTA carpeta
git add PARTE-2-BASE-DE-DATOS/

# 5. Hacer commits organizados
git commit -m "feat: create Usuario and related entities"
git commit -m "feat: create Asistencia and audit entities"
git commit -m "feat: create Tarea, Horario and schedule entities"
git commit -m "feat: create repository interfaces"
git commit -m "feat: create DTOs for API responses"

# 6. Subir cambios
git push -u origin feat/parte-2-base-datos

# 7. Crear Pull Request en GitHub
```

## 📝 Notas

- Depende de: PARTE-1 (compilar después que PARTE-1 esté en main)
- NO modifiques archivos de otras PARTES
- Todos los cambios deben ser dentro de PARTE-2-BASE-DE-DATOS/
- Verifica que pom.xml tenga todas las dependencias

---

*Checklist para PARTE 2 - Base de Datos*
