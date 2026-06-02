# ✅ PARTE 4 - FRONTEND Y INTERFAZ

**Integrante:** (Tu nombre aquí)

## ✓ Archivos Incluidos en Esta Carpeta

```
PARTE-4-FRONTEND-UI/
├── README.md
├── PART-4-CHECKLIST.md             (este archivo)
├── pom.xml                         ✅ (heredado de PARTE-1)
│
└── src/main/resources/
    ├── templates/                  ✅ HTML Templates (19 + fragmentos)
    │   ├── login.html
    │   ├── registrar.html
    │   ├── menu.html
    │   ├── perfil.html
    │   ├── usuarios.html
    │   └── ... (14 más)
    │
    └── static/
        ├── index.html              ✅ Página inicio
        ├── css/                    ✅ Estilos
        │   ├── style.css
        │   └── asignartareas.css
        ├── js/                     ✅ Scripts JavaScript
        │   └── ... (scripts)
        └── img/                    ✅ Imágenes
            └── ... (logos, iconos)
```

## 📋 Checklist Antes de Hacer Push

- [ ] Todos los templates HTML están en templates/
- [ ] Archivos CSS están en static/css/
- [ ] Archivos JavaScript están en static/js/
- [ ] Imágenes están en static/img/
- [ ] index.html está en static/
- [ ] Fragmentos reutilizables están en templates/fragmentos/
- [ ] Templates usan Thymeleaf correctamente
- [ ] Estilos son responsive (mobile-friendly)
- [ ] No hay errores JavaScript en consola

## 🚀 Pasos para Push a Repositorio

```bash
# 1. Posicionarse en el directorio del proyecto
cd "c:\Users\Franco\Desktop\asistencia (16--03-26)"

# 2. Actualizar desde main
git pull origin main

# 3. Crear rama
git checkout -b feat/parte-4-frontend

# 4. Agregar solo ESTA carpeta
git add PARTE-4-FRONTEND-UI/

# 5. Hacer commits organizados
git commit -m "feat: create login and registration templates"
git commit -m "feat: create main menu and navigation"
git commit -m "feat: create usuario management UI"
git commit -m "feat: create asistencia tracking interface"
git commit -m "feat: add CSS styling and responsive design"
git commit -m "feat: add JavaScript functionality"
git commit -m "feat: create reportes and informes views"
git commit -m "feat: add mobile-friendly layout"

# 6. Subir cambios
git push -u origin feat/parte-4-frontend

# 7. Crear Pull Request en GitHub
```

## 📝 Notas

- Puede trabajar en paralelo con PARTE-3
- Se compila con el resto en main
- NO modifiques archivos de otras PARTES
- Todos los cambios deben ser dentro de PARTE-4-FRONTEND-UI/
- Prueba los templates en: http://localhost:8080/

---

*Checklist para PARTE 4 - Frontend y Interfaz*
