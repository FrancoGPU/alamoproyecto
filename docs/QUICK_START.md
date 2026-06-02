# ⚡ QUICK START - Comienza en 5 Minutos

## 🎯 Para Cada Integrante

### 1️⃣ SETUP INICIAL (haz esto UNA sola vez)

```bash
# Clonar repositorio
git clone <URL>
cd asistencia

# Configura Git (UNA sola vez)
git config --global user.name "Tu Nombre"
git config --global user.email "tu@email.com"
```

### 2️⃣ CREAR TU RAMA

Elige según tu rol:

```bash
# Si eres Integrante 1 (Infraestructura)
git checkout -b feat/parte-1-infraestructura

# Si eres Integrante 2 (Base de Datos)
git checkout -b feat/parte-2-base-datos

# Si eres Integrante 3 (Backend)
git checkout -b feat/parte-3-backend

# Si eres Integrante 4 (Frontend)
git checkout -b feat/parte-4-frontend
```

### 3️⃣ TRABAJA EN TU CARPETA

```bash
# Solo toca tu carpeta PARTE-X-NOMBRE
# Copia archivos del proyecto original

# Ver qué cambió
git status

# Ver diferencias
git diff
```

### 4️⃣ HACER COMMITS

```bash
# Agregar cambios
git add PARTE-X-NOMBRE/

# Hacer commit con mensaje claro
git commit -m "feat: descripción breve de qué hiciste"

# Puedes hacer múltiples commits
git commit -m "feat: crear clase X"
git commit -m "feat: agregar método Y"
git commit -m "feat: configurar propiedad Z"
```

### 5️⃣ SUBIR CAMBIOS

```bash
# Primera vez (subir rama)
git push -u origin feat/parte-X-nombre

# Siguiente veces
git push
```

### 6️⃣ CREAR PULL REQUEST

```
1. Ir a GitHub/GitLab
2. Click en "New Pull Request" o "Merge Request"
3. Describir qué hiciste
4. Esperar aprobación
5. Click en "Merge"
```

---

## 📝 Ejemplos de Commits Según tu Rol

### Integrante 1 - Infraestructura

```bash
git add PARTE-1-INFRAESTRUCTURA/pom.xml
git commit -m "feat: setup pom.xml with Spring Boot dependencies"
git push

git add PARTE-1-INFRAESTRUCTURA/Dockerfile
git commit -m "feat: add Docker configuration"
git push

git add PARTE-1-INFRAESTRUCTURA/src/main/resources/
git commit -m "feat: configure application.properties for MySQL"
git push

git add PARTE-1-INFRAESTRUCTURA/src/main/java/.../config/
git commit -m "feat: create Spring Boot configuration beans"
git push

git add PARTE-1-INFRAESTRUCTURA/src/main/java/.../filter/
git commit -m "feat: implement security filters"
git push
```

### Integrante 2 - Base de Datos

```bash
git add PARTE-2-BASE-DE-DATOS/src/main/java/.../model/
git commit -m "feat: create Usuario and related entities"
git push

git add PARTE-2-BASE-DE-DATOS/src/main/java/.../model/
git commit -m "feat: create Asistencia and audit entities"
git push

git add PARTE-2-BASE-DE-DATOS/src/main/java/.../repository/
git commit -m "feat: create repository interfaces"
git push

git add PARTE-2-BASE-DE-DATOS/src/main/java/.../dto/
git commit -m "feat: create DTOs for API responses"
git push
```

### Integrante 3 - Backend

```bash
git add PARTE-3-BACKEND-SERVICIOS/src/main/java/.../service/
git commit -m "feat: create UsuarioService with business logic"
git push

git add PARTE-3-BACKEND-SERVICIOS/src/main/java/.../controller/
git commit -m "feat: create UsuarioController with endpoints"
git push

git add PARTE-3-BACKEND-SERVICIOS/src/main/java/.../service/
git commit -m "feat: create AsistenciaService"
git push

git add PARTE-3-BACKEND-SERVICIOS/src/main/java/.../controller/
git commit -m "feat: create AsistenciaController"
git push
```

### Integrante 4 - Frontend

```bash
git add PARTE-4-FRONTEND-UI/src/main/resources/templates/
git commit -m "feat: create login and registration templates"
git push

git add PARTE-4-FRONTEND-UI/src/main/resources/static/css/
git commit -m "feat: add styling and CSS"
git push

git add PARTE-4-FRONTEND-UI/src/main/resources/templates/
git commit -m "feat: create dashboard and user templates"
git push

git add PARTE-4-FRONTEND-UI/src/main/resources/static/js/
git commit -m "feat: add JavaScript functionality"
git push
```

---

## 🆘 Problemas Comunes

### "Error: fatal: not a git repository"
```bash
cd asistencia  # Entrar a la carpeta del proyecto
```

### "Error: permission denied"
```bash
# Configurar SSH en GitHub si aún no lo hiciste
# O usar HTTPS en lugar de SSH
git clone https://github.com/...
```

### "Cambios sin hacer commit"
```bash
# Ver qué cambió
git status

# Si son cambios accidentales, descartar
git checkout -- archivo.java

# O si son cambios válidos, hacer commit
git add PARTE-X/
git commit -m "fix: ..."
```

### "Rama está atrás de main"
```bash
# Actualizar desde main
git fetch origin
git rebase origin/main
git push
```

---

## ✅ Checklist Antes de Terminarte Turnos

- [ ] ¿Hiciste commits descriptivos?
- [ ] ¿Subiste todo con `git push`?
- [ ] ¿Todos los cambios están en tu carpeta PARTE-X?
- [ ] ¿El código compila sin errores?
- [ ] ¿Creaste el Pull Request?

---

## 🔗 Recursos Rápidos

| Necesitas... | Busca en... |
|---|---|
| Comandos Git | [GUIA_GIT.md](GUIA_GIT.md) |
| Cómo integrar todo | [INTEGRACION.md](INTEGRACION.md) |
| Detalles de tu parte | `PARTE-X-NOMBRE/README.md` |
| Resumen general | [README_ORGANIZACION.md](README_ORGANIZACION.md) |

---

## 💡 Pro Tips

✨ **Haz commits pequeños y frecuentes**  
→ Facilita el review y entender qué cambió

✨ **Mensajes de commit claros**  
→ Otros compañeros entienden qué hiciste

✨ **Push frecuentemente**  
→ Evita perder trabajo y facilita colaboración

✨ **Communica con tu equipo**  
→ Avisa si necesitas cambios de otros

---

## 🎯 Ya Estás Listo!

Ahora:
1. Abre tu carpeta `PARTE-X-NOMBRE`
2. Lee el `README.md` dentro
3. ¡Comienza a trabajar! 🚀

*¿Preguntas? Revisa los documentos de referencia o pregunta a tu equipo.*
