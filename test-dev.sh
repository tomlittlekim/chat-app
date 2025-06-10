#!/bin/bash

echo "🚀 개발 환경 테스트 시작..."

# 기존 컨테이너 정리
echo "🧹 기존 컨테이너 정리 중..."
docker compose down

# 개발 환경 시작
echo "📦 개발 환경 컨테이너 시작 중..."
docker compose -f docker-compose-dev.yml up -d

# 서비스 준비 대기
echo "⏳ 서비스 준비 중..."
sleep 10

# 서비스 상태 확인
echo "📊 서비스 상태 확인:"
docker compose -f docker-compose-dev.yml ps

# MongoDB 연결 테스트
echo ""
echo "🍃 MongoDB 연결 테스트:"
if docker exec chat-mongodb-dev mongosh --eval "db.adminCommand('ping')" > /dev/null 2>&1; then
    echo "✅ MongoDB 연결 성공"
else
    echo "❌ MongoDB 연결 실패"
fi

# Redis 연결 테스트  
echo ""
echo "🔴 Redis 연결 테스트:"
if docker exec chat-redis-dev redis-cli ping > /dev/null 2>&1; then
    echo "✅ Redis 연결 성공"
else
    echo "❌ Redis 연결 실패"
fi

echo ""
echo "🌐 서비스 URL:"
echo "  - Spring Boot 애플리케이션: http://localhost:8080"
echo "  - Socket.io 서버: http://localhost:9092"
echo "  - 웹 클라이언트: http://localhost:8080/index.html"
echo ""
echo "💡 애플리케이션을 dev 프로파일로 실행하세요:"
echo "  ./gradlew bootRun --args='--spring.profiles.active=dev'" 