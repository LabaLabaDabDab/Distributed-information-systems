FROM eclipse-temurin:17-jdk-alpine

ARG JAR_FILE=target/*.jar

COPY ./build/libs/worker-0.0.1-SNAPSHOT.jar  app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","/app.jar"]