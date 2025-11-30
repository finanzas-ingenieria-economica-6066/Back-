# Build Stage
#FROM maven:3.9-eclipse-temurin-21 AS build
#COPY . .
# FIX: Added encoding flag to handle special characters in your properties file
#RUN mvn clean package -DskipTests -Dproject.build.sourceEncoding=ISO-8859-1

# Run Stage
#FROM eclipse-temurin:21-alpine
#COPY --from=build /target/*.jar app_bonotech.jar
#EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "app_bonotech.jar"]

# UPDATEE
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests -Dproject.build.sourceEncoding=ISO-8859-1

FROM eclipse-temurin:21-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app_bonotech.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app_bonotech.jar"]