FROM gradle:7.5.1-jdk11-alpine

RUN mkdir -p /app
WORKDIR /app

EXPOSE 8080

COPY src /app/src
COPY build.gradle.kts /app
COPY settings.gradle.kts /app

RUN gradle clean assemble

ENV PROFILE=DOCKER
CMD java -Dspring.profiles.active=$PROFILE $JVM_OPTIONS -jar /app/build/libs/SummerShoe.jar

HEALTHCHECK CMD wget --quiet -O- http://localhost:8080/health || exit 1
