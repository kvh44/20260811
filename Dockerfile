FROM eclipse-temurin:26-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src ./src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:26-jre
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar
COPY --from=build /app/target/otel/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

EXPOSE 8001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
