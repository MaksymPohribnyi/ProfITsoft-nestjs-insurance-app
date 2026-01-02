FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew build.gradle settings.gradle ./
COPY gradle gradle

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew clean build -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]