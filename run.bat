@echo off

REM Set environment variables
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres
set SPRING_DATASOURCE_USERNAME=workreport_user
set SPRING_DATASOURCE_PASSWORD=userpass
set SPRING_FLYWAY_URL=jdbc:postgresql://localhost:5432/postgres
set SPRING_FLYWAY_USER=workreport_user
set SPRING_FLYWAY_PASSWORD=userpass

REM Run the Spring Boot JAR
java -jar C:\Users\Alex\IdeaProjects\WorkReportPlus\build\libs\WorkReportPlus-0.0.1-SNAPSHOT.jar

pause
