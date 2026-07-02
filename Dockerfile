FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

COPY src ./src

RUN ./gradlew clean bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

ENV TZ=Asia/Seoul

RUN groupadd --system haru && useradd --system --gid haru --home-dir /app --shell /usr/sbin/nologin haru

COPY --from=builder /workspace/build/libs/app.jar app.jar

USER haru

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
