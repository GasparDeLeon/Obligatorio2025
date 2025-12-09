# 1. Usar una imagen base con Maven para construir el proyecto
FROM maven:3.8.5-openjdk-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Usar una imagen ligera de Java para ejecutarlo
FROM openjdk:21-jdk-slim
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]