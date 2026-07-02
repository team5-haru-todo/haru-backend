# 빌드 전용 스테이지: Gradle로 실행 가능한 jar를 만든다.
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /workspace

# 의존성 관련 파일을 먼저 복사해 Docker layer cache를 활용한다.
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

COPY src ./src

# Docker 이미지 빌드 중에는 테스트를 제외하고 bootJar만 생성한다.
RUN ./gradlew clean bootJar -x test --no-daemon

# 실행 전용 스테이지: 빌드 도구 없이 JRE와 jar만 포함한다.
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

ENV TZ=Asia/Seoul

# 컨테이너 내부에서 root가 아닌 전용 사용자로 앱을 실행한다.
RUN groupadd --system haru && useradd --system --gid haru --home-dir /app --shell /usr/sbin/nologin haru

COPY --from=builder /workspace/build/libs/app.jar app.jar

USER haru

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
