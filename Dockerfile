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

EXPOSE 8001
ENV SPRING_PROFILES_ACTIVE=local
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
