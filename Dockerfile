# Multi-stage Dockerfile for the Spring Boot backend.
# Build:
#   docker build -t sparkgadget-backend .
#
# Run:
#   docker run --rm -p 8081:8081 ^
#     -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/electronicStore?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" ^
#     -e MYSQL_USER="root" ^
#     -e MYSQL_PASSWORD="your_mysql_password" ^
#     sparkgadget-backend

FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw && ./mvnw -B -DskipTests dependency:go-offline

COPY src/ src/

RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

ENV JAVA_OPTS=""

RUN mkdir -p images/products

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
