# Multi-stage build để tối ưu kích thước image
# Stage 1: Build application
FROM maven:3.9.0-openjdk-17 AS builder

WORKDIR /app

# Copy pom.xml và download dependencies
COPY eCommersApp/pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY eCommersApp/src ./src

# Build ứng dụng
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM openjdk:17.0.1-jdk-slim-bullseye

WORKDIR /app

# Copy JAR từ stage 1
COPY --from=builder /app/target/*.jar app.jar

# Expose port (mặc định Spring Boot chạy ở 8080)
EXPOSE 8080

# Run ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
