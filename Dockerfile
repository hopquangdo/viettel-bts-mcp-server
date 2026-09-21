FROM eclipse-temurin:17-jdk AS build

WORKDIR /workspace
COPY services/business-api/gradlew services/business-api/gradlew
COPY services/business-api/gradle services/business-api/gradle
COPY services/mcp-server/build.gradle services/mcp-server/settings.gradle services/mcp-server/
COPY services/mcp-server/src services/mcp-server/src
COPY services/business-api/src/main/java services/business-api/src/main/java
RUN chmod +x services/business-api/gradlew
RUN services/business-api/gradlew -p services/mcp-server bootJar --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /workspace/services/mcp-server/build/libs/mcp-server.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]