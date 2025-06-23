# syntax=docker/dockerfile:1

# 1) Build stage (якщо ви хочете збирати jar всередині контейнера)
# FROM gradle:7.6-jdk17-alpine AS builder
# WORKDIR /app
# COPY --chown=gradle:gradle . .
# RUN gradle bootJar --no-daemon

# 2) Runtime stage
FROM openjdk:17-jdk-slim

ARG JAR_FILE=build/libs/WorkReportPlus-0.0.1-SNAPSHOT.jar

WORKDIR /app

# Копіюємо ззовні зібраний .jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
