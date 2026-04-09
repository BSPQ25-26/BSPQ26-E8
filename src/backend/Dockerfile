FROM gradle:8.10.2-jdk21-alpine AS builder
WORKDIR /app
COPY . .
RUN gradle clean bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-10000} -Dserver.address=0.0.0.0 -jar /app/app.jar"]
