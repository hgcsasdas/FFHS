FROM eclipse-temurin:21-jdk-jammy AS builder 

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# --- INSTALL MAVEN ---
RUN apt-get update && \
    apt-get install -y --no-install-recommends maven && \
    rm -rf /var/lib/apt/lists/*

COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

COPY src ./src

RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENV TZ=Europe/Madrid

EXPOSE 8081 8080

ENTRYPOINT ["java", "-jar", "app.jar"]