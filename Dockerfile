# Build stage
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
RUN gradle build -x test --no-daemon

# Runtime stage
FROM openjdk:21-jdk-slim
WORKDIR /app

# 애플리케이션 사용자 생성
RUN useradd -r -s /bin/false chatapp

# 필요한 패키지 설치
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 소유권 변경
RUN chown chatapp:chatapp app.jar

# 사용자 전환
USER chatapp

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 포트 노출
EXPOSE 8080 9092

# JVM 최적화 옵션
ENV JAVA_OPTS="-server -Xmx2g -Xms1g -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+OptimizeStringConcat"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 