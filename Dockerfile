FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the pre-built JAR from host
COPY target/*.jar app.jar

# Expose port (adjust if needed)
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
