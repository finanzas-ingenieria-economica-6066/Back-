from openjdk:21-jdk-slim
ARG JAR_FILE=target/bonotech-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app_bonotech.jar
EXPOSE 8080
ENTRYPOINT {"java", "-jar", "app_bonotech.jar"}