#!/bin/bash

# Docker Compose 개발 환경 관리 스크립트

set -e

function usage() {
    echo "사용법: $0 [COMMAND]"
    echo ""
    echo "명령어:"
    echo "  up        MongoDB와 Redis 서비스 시작"
    echo "  down      모든 서비스 중지"
    echo "  restart   서비스 재시작"
    echo "  logs      서비스 로그 확인"
    echo "  status    서비스 상태 확인"
    echo "  clean     모든 데이터와 볼륨 삭제"
    echo "  gui       관리 GUI 실행 (MongoDB Express + Redis Commander)"
    echo ""
    echo "포트 정보:"
    echo "  - MongoDB: 27017"
    echo "  - Redis: 6379"
    echo "  - MongoDB Express: http://localhost:8081"
    echo "  - Redis Commander: http://localhost:8082"
    exit 1
}

function up() {
    echo "🚀 MongoDB와 Redis 서비스를 시작합니다..."
    docker-compose up -d mongodb redis
    
    echo "⏳ 서비스가 준비될 때까지 대기 중..."
    docker-compose exec mongodb mongosh --eval "db.adminCommand('ping')" > /dev/null 2>&1
    docker-compose exec redis redis-cli ping > /dev/null 2>&1
    
    echo "✅ 서비스가 성공적으로 시작되었습니다!"
    status
}

function up_with_gui() {
    echo "🚀 모든 서비스를 시작합니다 (GUI 포함)..."
    docker-compose up -d
    
    echo "⏳ 서비스가 준비될 때까지 대기 중..."
    sleep 10
    
    echo "✅ 모든 서비스가 성공적으로 시작되었습니다!"
    echo ""
    echo "🌐 관리 인터페이스:"
    echo "  - MongoDB Express: http://localhost:8081"
    echo "  - Redis Commander: http://localhost:8082"
    status
}

function down() {
    echo "🛑 서비스를 중지합니다..."
    docker-compose down
    echo "✅ 모든 서비스가 중지되었습니다!"
}

function restart() {
    echo "🔄 서비스를 재시작합니다..."
    down
    up
}

function logs() {
    echo "📋 서비스 로그를 확인합니다..."
    docker-compose logs -f
}

function status() {
    echo "📊 서비스 상태:"
    docker-compose ps
    echo ""
    echo "💾 볼륨 정보:"
    docker volume ls | grep chat
}

function clean() {
    echo "🗑️  모든 데이터와 볼륨을 삭제합니다..."
    read -p "정말로 모든 데이터를 삭제하시겠습니까? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose down -v
        docker volume prune -f
        echo "✅ 모든 데이터가 삭제되었습니다!"
    else
        echo "❌ 작업이 취소되었습니다."
    fi
}

function check_docker() {
    if ! command -v docker &> /dev/null; then
        echo "❌ Docker가 설치되어 있지 않습니다."
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        echo "❌ Docker Compose가 설치되어 있지 않습니다."
        exit 1
    fi
}

# 메인 로직
check_docker

case "${1:-}" in
    up)
        up
        ;;
    gui)
        up_with_gui
        ;;
    down)
        down
        ;;
    restart)
        restart
        ;;
    logs)
        logs
        ;;
    status)
        status
        ;;
    clean)
        clean
        ;;
    *)
        usage
        ;;
esac 