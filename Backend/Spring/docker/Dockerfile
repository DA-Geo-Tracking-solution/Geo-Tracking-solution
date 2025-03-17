# Use Maven with JDK 17 for building the application
FROM maven:3.8.4-openjdk-17 AS builder
WORKDIR /app
# Copy only the pom.xml first and download dependencies (for caching)
COPY ../pom.xml .
RUN mvn dependency:go-offline -B
# Copy the source code and build the application
COPY ../src ./src
RUN mvn clean package -DskipTests

# Use a lightweight JDK 17 runtime for running the application
FROM openjdk:17-jdk-slim
WORKDIR /app
# Copy the built JAR file from the previous stage
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
