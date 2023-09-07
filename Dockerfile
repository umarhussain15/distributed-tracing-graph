FROM maven:3-eclipse-temurin-17 AS artifactory
LABEL authors="umar"


ADD pom.xml ./

ADD src/ src/

RUN ls

RUN mvn install

FROM openjdk:17-alpine

COPY --from=artifactory target/distributed-tracing-graph-1.0-SNAPSHOT.jar run.jar

ADD input.txt /input.txt

ENTRYPOINT ["java", "-jar" ,"run.jar"]
CMD ["/input.txt"]