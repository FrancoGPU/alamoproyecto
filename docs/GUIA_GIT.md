# 📖 GUÍA DE GIT - TRABAJO COLABORATIVO

Guía para trabajar con Git de forma colaborativa en las 4 partes del proyecto.

---

## 🔧 Configuración Inicial de Git

Cada integrante debe identificarse:

```bash
# Configurar nombre y email (hacer UNA vez)
git config --global user.name "Tu Nombre"
git config --global user.email "tu.email@ejemplo.com"

# Verificar configuración
git config --list
```

---

## 🚀 Clonar y Descargar el Proyecto

```bash
# Clonar el repositorio
git clone <URL-del-repositorio>

# Entrar al directorio
cd asistencia

# Ver las ramas disponibles
git branch -a

# Actualizar (en caso de cambios remotos)
git pull origin main
```

---

## 🌿 Crear Rama para tu Parte

Cada integrante debe trabajar en su propia rama:

```bash
# Crear rama para tu parte (desde main)
git checkout -b feat/parte-1-infraestructura        # Integrante 1
git checkout -b feat/parte-2-base-datos             # Integrante 2
git checkout -b feat/parte-3-backend                # Integrante 3
git checkout -b feat/parte-4-frontend               # Integrante 4

# Verificar que estás en la rama correcta
git branch
git status
```

---

## 📝 Workflow de Commits

### 1. Ver estado actual

```bash
git status
```

### 2. Agregar archivos para commit

```bash
# Agregar archivo específico
git add PARTE-X-NOMBRE/archivo.java

# Agregar múltiples archivos
git add PARTE-X-NOMBRE/src/main/java/.../

# Agregar todo (NO recomendado)
git add .
```

### 3. Ver cambios antes de confirmar

```bash
# Ver cambios no staged
git diff

# Ver cambios staged
git diff --staged
```

### 4. Hacer commit con mensaje descriptivo

```bash
# Formato: git commit -m "tipo: descripción"

# Ejemplos válidos:
git commit -m "feat: add Usuario entity with JPA annotations"
git commit -m "feat: create UsuarioService with CRUD methods"
git commit -m "feat: add login template and styling"
git commit -m "fix: correct cascade delete in relationships"
git commit -m "docs: update README with configuration steps"
```

### 5. Tipos de commits comunes

```
feat:    Nueva funcionalidad
fix:     Corrección de error
docs:    Cambios en documentación
style:   Cambios de formato (espacios, llaves, etc.)
refactor: Refactorización de código
test:    Agregar o actualizar tests
chore:   Cambios en configuración o build
```

### 6. Subir commits al remoto

```bash
# Subir rama por primera vez
git push -u origin feat/parte-X-nombre

# Subir cambios posteriores
git push
```

---

## 📋 Ejemplo Completo - Integrante 1

```bash
# 1. Descargar últimos cambios
git pull origin main

# 2. Crear rama para trabajar
git checkout -b feat/parte-1-infraestructura

# 3. Copiar archivos a PARTE-1-INFRAESTRUCTURA/
# (pom.xml, Dockerfile, application.properties, etc.)

# 4. Ver qué cambió
git status
git diff

# 5. Agregar archivos
git add PARTE-1-INFRAESTRUCTURA/pom.xml
git add PARTE-1-INFRAESTRUCTURA/Dockerfile
git add PARTE-1-INFRAESTRUCTURA/docker-compose.yml

# 6. Hacer commit
git commit -m "feat: setup pom.xml and Docker configuration"

# 7. Agregar más cambios
git add PARTE-1-INFRAESTRUCTURA/src/main/resources/
git commit -m "feat: configure application.properties"

git add PARTE-1-INFRAESTRUCTURA/src/main/java/.../config/
git commit -m "feat: add Spring Boot configuration beans"

# 8. Subir cambios
git push -u origin feat/parte-1-infraestructura

# 9. Crear Pull Request en GitHub/GitLab para review
```

---

## 🔄 Pull Requests y Code Review

### Integrante: Crear PR

```bash
# 1. Asegúrate de que todo está subido
git push

# 2. Ir a GitHub/GitLab/Bitbucket
# 3. Crear Pull Request de tu rama a main
# 4. Agregar descripción de cambios
# 5. Solicitar review
```

### Otros: Revisar PR

```bash
# Descargar rama del PR
git fetch origin feat/parte-X-nombre
git checkout feat/parte-X-nombre

# Revisar cambios
git log --oneline
git diff main..HEAD

# Si todo está bien, hacer merge
```

### Hacer Merge

```bash
# Opción 1: Merge desde GitHub/GitLab (Merge button)

# Opción 2: Merge local
git checkout main
git merge feat/parte-X-nombre
git push origin main

# Opción 3: Rebase + Fast-forward
git checkout main
git rebase feat/parte-X-nombre
git push origin main
```

---

## 🔀 Actualizar tu Rama con Cambios de main

Si otros ya hicieron merge a main:

```bash
# Opción 1: Merge (crea commit de merge)
git fetch origin
git merge origin/main

# Opción 2: Rebase (más limpio)
git fetch origin
git rebase origin/main

# Resolver conflictos si hay
# Editar archivos conflictivos
# Luego:
git add .
git rebase --continue
```

---

## 📊 Ver Historial de Commits

```bash
# Ver commits en tu rama
git log --oneline

# Ver commits con diferencias
git log -p

# Ver commits de una persona
git log --author="Nombre"

# Ver commits visualizados
git log --graph --oneline --all

# Ver cambios en archivo específico
git log -- PARTE-1-INFRAESTRUCTURA/pom.xml
```

---

## 🐛 Deshacer Cambios

### Si no hiciste commit

```bash
# Ver cambios
git status
git diff

# Descartar cambios en archivo
git checkout -- PARTE-1-INFRAESTRUCTURA/archivo.java

# Descartar todos los cambios
git reset --hard
```

### Si ya hiciste commit (pero no push)

```bash
# Revertir último commit (mantiene cambios)
git reset --soft HEAD~1

# Revertir último commit (descarta cambios)
git reset --hard HEAD~1

# Enmendar último commit
git add archivo-olvidado.java
git commit --amend --no-edit
```

### Si ya hiciste push

```bash
# Revertir con nuevo commit
git revert <hash-del-commit>
git push

# Forzar reescritura (⚠️ solo si nadie más está trabajando en esa rama)
git reset --hard HEAD~1
git push --force
```

---

## ⚡ Comandos Útiles Rápidos

```bash
# Ver estado actual
git status

# Ver rama actual
git branch

# Ver cambios
git diff

# Crear y cambiar a rama
git checkout -b nueva-rama

# Cambiar a otra rama
git checkout nombre-rama

# Listar ramas
git branch -a

# Eliminar rama local
git branch -d nombre-rama

# Eliminar rama remota
git push origin --delete nombre-rama

# Ver último commit
git log -1

# Buscar cambios que contienen una palabra
git log -S "palabra"

# Ver gráfico de ramas
git log --graph --oneline --all
```

---

## ✅ Checklist Antes de Hacer Push

- [ ] ¿Está todo compilando sin errores?
- [ ] ¿Los cambios están en la rama correcta?
- [ ] ¿El mensaje de commit es descriptivo?
- [ ] ¿Incluí todos los archivos necesarios?
- [ ] ¿Revisé los cambios con `git diff`?
- [ ] ¿No incluí archivos no deseados?
- [ ] ¿La funcionalidad está completa?

---

## 🆘 Problemas Comunes

### "Your branch is ahead of 'origin/main' by X commits"

```bash
# Subir cambios
git push
```

### "Conflict in merge"

```bash
# Ver conflictos
git status

# Editar archivos conflictivos (busca <<<<<<, ======, >>>>>>>)
# Eliminar markers y decidir qué versión mantener

# Resolver
git add archivo-resuelto.java
git commit -m "fix: resolve merge conflict"
```

### "Branch diverged and has X commits ahead, Y commits behind"

```bash
# Actualizar desde main
git fetch origin
git rebase origin/main

# Si hay conflictos, resolverlos
git rebase --continue

# Subir cambios
git push --force
```

### "Error: Permission denied (publickey)"

```bash
# Generar nueva llave SSH
ssh-keygen -t rsa -b 4096

# Agregar a GitHub/GitLab en Settings > SSH Keys
cat ~/.ssh/id_rsa.pub
```

---

## 📚 Recursos

- [Documentación oficial de Git](https://git-scm.com/doc)
- [GitHub Guides](https://guides.github.com/)
- [Atlassian Git Tutorials](https://www.atlassian.com/git/tutorials)

---

## 🎯 Resumen Rápido

```bash
# Cada vez que trabajes:
git pull origin main                                 # Actualizar
git checkout -b feat/mi-parte                        # Crear rama
# ... hacer cambios ...
git add PARTE-X/.../archivo.java                     # Agregar
git commit -m "feat: descripción de cambios"         # Commit
git push -u origin feat/mi-parte                     # Subir

# Cuando termines:
# Crear Pull Request en GitHub
# Esperar aprobación
# Hacer merge a main
```

¡Buena suerte! 🚀
