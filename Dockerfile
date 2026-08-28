FROM container-registry.oracle.com/java/openjdk:25-oraclelinux9 AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

USER root

ARG UID=10001
RUN useradd -u ${UID} -r -s /sbin/nologin -M appuser || true

RUN mkdir -p /home/appuser/.gradle && chown -R appuser:appuser /home/appuser

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew
RUN chown -R appuser:appuser /app

COPY src src

USER appuser

ENV GRADLE_USER_HOME=/home/appuser/.gradle

EXPOSE 8080

ENTRYPOINT [ "./gradlew", "bootRun", "--no-daemon" ]