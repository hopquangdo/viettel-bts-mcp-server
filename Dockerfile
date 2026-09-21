FROM eclipse-temurin:17-jdk AS build

WORKDIR /workspace
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src
RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /workspace/build/libs/mcp-server.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]