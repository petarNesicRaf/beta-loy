# Stage 1 — build the jar
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Download dependencies first (cached layer if pom.xml doesn't change)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN mvn package -Dmaven.test.skip=true -q

# Stage 2 — run with a minimal JRE image
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
