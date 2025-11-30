FROM maven:3-eclipse-temurin-17 AS build
COPY . .
# Fixed: Added 'D' to the flag
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-alpine
# Ensure the path matches where Maven outputs the JAR (usually /target)
COPY --from=build /target/*.jar app_bonotech.jar
EXPOSE 8080
# Fixed: Changed {} to []
ENTRYPOINT ["java", "-jar", "app_bonotech.jar"]