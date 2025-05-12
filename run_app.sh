#!/usr/bin/env bash
set -euo pipefail

# Jump to the directory where this script resides
cd "$(dirname "$0")"

# Export your Spring Boot env vars
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/postgres"
export SPRING_DATASOURCE_USERNAME="workreport_user"
export SPRING_DATASOURCE_PASSWORD="userpass"
export SPRING_FLYWAY_URL="jdbc:postgresql://localhost:5432/postgres"
export SPRING_FLYWAY_USER="workreport_user"
export SPRING_FLYWAY_PASSWORD="userpass"

# Run the JAR (they’re in the same folder now)
java -jar WorkReportPlus-0.0.1-SNAPSHOT.jar

# Pause so you can see log output before the window closes
read -n1 -r -p "Press any key to exit…"
