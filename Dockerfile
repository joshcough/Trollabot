FROM openjdk:8-jdk-slim as builder

WORKDIR /opt

ARG SBT_VERSION=1.6.2

# Install sbt
RUN apt update && \
    apt install -yqq curl && \
    curl -L -o sbt-$SBT_VERSION.deb https://repo.scala-sbt.org/scalasbt/debian/sbt-$SBT_VERSION.deb && \
    dpkg -i sbt-$SBT_VERSION.deb && \
    rm sbt-$SBT_VERSION.deb && \
    apt-get update && \
    apt-get install sbt && \
    sbt sbtVersion

WORKDIR /src

COPY . .

RUN sbt compile && \
    sbt publishLocal && \
    sbt assembly

FROM openjdk:8-jre as development

WORKDIR /app

COPY ./target/scala-2.13/trollabot-scala-assembly-0.1.0.jar ./

ENTRYPOINT ["java", "-jar", "/app/trollabot-scala-assembly-0.1.0.jar"]

FROM openjdk:8-jre

WORKDIR /app

COPY --from=builder /src/target/scala-2.13/trollabot-scala-assembly-0.1.0.jar /app/trollabot-scala-assembly-0.1.0.jar

ENTRYPOINT ["java", "-jar", "/app/trollabot-scala-assembly-0.1.0.jar"]
