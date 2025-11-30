FROM maven:3-eclipse-temurin-17 AS build
COPY . .
RUN mvn clean package -skipTests

FROM eclipse-temurin:17-alpine
COPY --from=build /target/*.jar app_bonotech.jar
EXPOSE 8080
ENTRYPOINT {"java", "-jar", "app_bonotech.jar"}