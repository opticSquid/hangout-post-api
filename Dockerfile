# Stage 1: Build the JAR
FROM maven:3-amazoncorretto-21 AS builder
WORKDIR /usr/src/app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests=true

# Stage 2: Create the runtime image
FROM amazoncorretto:21-alpine

WORKDIR /app

COPY --from=builder /usr/src/app/target/hangout-post-api-1.5.9.jar .

CMD ["java", "-jar", "hangout-post-api-1.5.9.jar"]
