# Multi-stage build để tối ưu kích thước image
# Stage 1: Build application
FROM maven:3.8.1-openjdk-17 AS builder

WORKDIR /app

# Copy pom.xml và download dependencies
COPY eCommersApp/pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY eCommersApp/src ./src

# Build ứng dụng
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM amazoncorretto:17-alpine

WORKDIR /app

# Copy JAR từ stage 1
COPY --from=builder /app/target/*.jar app.jar

# Expose port (mặc định Spring Boot chạy ở 8080)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD java -cp app.jar -Dspring.profiles.active=production \
  -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false \
  org.springframework.boot.loader.JarLauncher

# Run ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
