#FROM ubuntu:latest
#LABEL authors="alina"
#
#ENTRYPOINT ["top", "-b"]

# base image for the app
# all instructions will be executed in that (graalvm) environment
# AS builder meains that it's a building stage
# jdk is needed tp compile code
FROM eclipse-temurin:25-jdk AS builder

# setting working dir inside the container dir
# also, switches to /app dir inside the container.
# all subsequent commands (layers) will be run in that dir
WORKDIR /app

# copy needed gradle files and folders from the proj on my computer
# to the docker container
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

# downloading dependencies
RUN ./gradlew dependencies --no-daemon

# copying app files and folders that are needed
COPY src src

# running command like in a cmd
# building the app
RUN ./gradlew bootJar --no-daemon

# jre needed for runtime to run the app
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# copying everything builded above
COPY --from=builder /app/build/libs/*.jar app.jar

# will be needed later to set ENV variables like in the .env file
# ENV ...

# describes the port where my app will be running
# so it tells Docker to make port 8080 available
# to access app outside the comtaoner
EXPOSE 8080

# specifies default commands that run when container starts
ENTRYPOINT ["java", "-jar", "app.jar"]