FROM openjdk:24-jdk-bookworm

WORKDIR /app

COPY target/Monopoly-API-Gateway-1.0-SNAPSHOT.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java","-jar","app.jar","--server.port=8083"]
