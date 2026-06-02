# ============================
# Etapa 1: Construcción del JAR con Maven (Java 21)
# ============================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos pom.xml y descargamos dependencias primero (cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el resto del proyecto y compilamos
COPY src ./src
RUN mvn clean package -DskipTests

# ============================
# Etapa 2: Imagen ligera para ejecutar la app (Java 21)
# ============================
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiamos el .jar generado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Spring Boot por defecto levanta en 8080 dentro del contenedor
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
