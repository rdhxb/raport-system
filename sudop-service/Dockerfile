# Build context = root of the monorepo (needs the parent pom.xml)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY krs-service/pom.xml krs-service/
COPY raport-service/pom.xml raport-service/
COPY whiteList-service/pom.xml whiteList-service/
COPY crbr-service/pom.xml crbr-service/
COPY sudop-service/pom.xml sudop-service/
COPY common/pom.xml common/
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -pl sudop-service -am dependency:go-offline
COPY common/src common/src
COPY sudop-service/src sudop-service/src
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -pl sudop-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/sudop-service/target/sudop-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
