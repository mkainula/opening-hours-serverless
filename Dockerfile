FROM openjdk:8u121-jdk-alpine

WORKDIR /home

COPY target/opening-hours.jar opening-hours.jar

CMD ["java", "-jar", "opening-hours.jar", "input.json"]