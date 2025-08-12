# ========================
# 1) Build Stage
# ========================
FROM gradle:8.8-jdk21 AS build
WORKDIR /app

# 의존성 캐시 레이어
COPY gradle ./gradle
COPY gradlew settings.gradle build.gradle ./
RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon --build-cache dependencies

# 소스 빌드
COPY src ./src
# 필요 시 테스트 스킵:  --exclude-task test 또는 -x test
RUN ./gradlew --no-daemon --build-cache bootJar

# 레이어 추출
RUN java -Djarmode=layertools -jar build/libs/*.jar extract

# ========================
# 2) Runtime Stage
# ========================
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 보안: non-root
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

# 레이어 복사 (캐시 효율 극대화)
COPY --from=build --chown=appuser:appgroup /app/dependencies/ ./
COPY --from=build --chown=appuser:appgroup /app/spring-boot-loader/ ./
COPY --from=build --chown=appuser:appgroup /app/snapshot-dependencies/ ./
COPY --from=build --chown=appuser:appgroup /app/application/ ./

# 컨테이너 런타임 최적화(메모리 안전장치)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport"
# 로그 디렉터리(호스트 볼륨 마운트 예정)
ENV APP_LOG_DIR=/app/logs

USER appuser
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
