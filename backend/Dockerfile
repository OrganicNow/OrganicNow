# Stage 1: Build JAR
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
# 👇 ตรงนี้เปลี่ยนเป็น maven.test.skip=true
RUN mvn clean package -DskipTests && ls -l /app/target

# Stage 2: Run JAR
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]


# =========================
# Stage 1: Build JAR
# =========================
#FROM maven:3.9.6-eclipse-temurin-21 AS build
#WORKDIR /app
#
## cache dependencies
#COPY pom.xml .
#RUN mvn -B -q -DskipTests dependency:go-offline
#
## build (ข้ามทั้งคอมไพล์และรันเทส)
#COPY src ./src
#RUN mvn -B -q clean package -Dmaven.test.skip=true
#
## =========================
## Stage 2: Runtime
## =========================
#FROM eclipse-temurin:21-jre-alpine
#WORKDIR /app
#
## ก็อป jar แบบ wildcard กันพลาดชื่อไฟล์
#COPY --from=build /app/target/*-SNAPSHOT.jar /app/app.jar
#
## (ทางเลือก) ปรับ JVM flag ผ่าน JAVA_OPTS
#ENV JAVA_OPTS=""
#
#EXPOSE 8080
#ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
