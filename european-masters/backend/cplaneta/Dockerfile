### Multi-stage Dockerfile para el microservicio cplaneta (Spring Boot / Maven)

# Etapa 1: build con Maven
FROM docker.io/library/maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiamos los descriptores de Maven primero para aprovechar la cache
COPY pom.xml .

# Descargar dependencias sin compilar todavía (mejora tiempos en cambios pequeños)
RUN mvn -B -q dependency:go-offline

# Copiamos el código fuente
COPY src ./src

# Construimos el jar (sin tests para acelerar la imagen; cambiar si quieres ejecutarlos)
RUN mvn -B -q clean package -DskipTests

# Etapa 2: runtime con JDK ligero
FROM docker.io/library/eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Variables por defecto (pueden ser sobreescritas por docker-compose)
ENV JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE=default

# Copiamos el jar generado desde la etapa de build
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
