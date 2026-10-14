FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
# Wrapper del proyecto: Spring Boot 4 necesita Gradle 9
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew --version --no-daemon
COPY src ./src
RUN ./gradlew clean bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8085
# preferIPv4Stack evita el error "Network unreachable" al conectar con Azure
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-Djava.net.preferIPv4Stack=true", "-jar", "app.jar"]
