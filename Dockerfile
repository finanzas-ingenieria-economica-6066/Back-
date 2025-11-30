FROM maven:3.9-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-alpine
COPY --from=build /target/*.jar app_bonotech.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app_bonotech.jar"]