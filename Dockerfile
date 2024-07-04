#
# Build stage
#
FROM maven:3.9.8-eclipse-temurin-21 AS build
COPY src /home/app/src
COPY pom.xml /home/app
EXPOSE 8080
EXPOSE 3306
RUN mvn -f /home/app/pom.xml clean package
ENTRYPOINT ["java","-jar","/home/app/target/todoapp-0.0.1-SNAPSHOT.jar"]