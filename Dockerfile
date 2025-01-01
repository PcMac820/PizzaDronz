FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/ILP_cw1-0.0.1-SNAPSHOT.jar /app/ILP_cw1.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/ILP_cw1.jar"]