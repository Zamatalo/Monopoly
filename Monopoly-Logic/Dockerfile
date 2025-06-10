FROM openjdk:24-jdk-bookworm

WORKDIR /app

COPY target/Monopoly-Logic-1.0-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
