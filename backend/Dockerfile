# Stage 1: Build JAR
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
# 👇 ตรงนี้เปลี่ยนเป็น maven.test.skip=true
#RUN mvn clean package -Dmaven.test.skip=true && ls -l /app/target
RUN mvn clean package -DskipTests && ls -l /app/target

# Stage 2: Run JAR
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]