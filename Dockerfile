# Imagen del backend: compila con Maven y ejecuta con un JRE 21 liviano.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S judisa && adduser -S judisa -G judisa
COPY --from=build /app/target/judisa-backend-*.jar app.jar
USER judisa
ENV TZ=America/Bogota
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
