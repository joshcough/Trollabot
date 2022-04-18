FROM openjdk:8-jre
ARG TROLLABOT_TOKEN
ENV TROLLABOT_TOKEN="oauth:tg0jy8cmw01h5v5r1tmac5yd9hjdk1"
ADD ./target/scala-2.13/trollabot-scala-assembly-0.1.0.jar /app/trollabot-scala-assembly-0.1.0.jar
ENTRYPOINT ["java", "-jar", "\/app\/trollabot-scala-assembly-0.1.0.jar"]
